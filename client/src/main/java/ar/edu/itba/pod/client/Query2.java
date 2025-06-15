package ar.edu.itba.pod.client;

import ar.edu.itba.pod.collator.CommonTypeCollator;
import ar.edu.itba.pod.combiner.TypeCountCombinerFactory;
import ar.edu.itba.pod.common.CoordinateNeighborhood;
import ar.edu.itba.pod.mapper.QuadrantTypeMapper;
import ar.edu.itba.pod.model.Complaint;
import ar.edu.itba.pod.reducer.TypeQuadrantReducerFactory;
import ar.edu.itba.pod.util.*;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MultiMap;
import com.hazelcast.mapreduce.JobTracker;
import com.hazelcast.mapreduce.KeyValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@SuppressWarnings("deprecation")
public class Query2 {
    private static final Logger logger = LoggerFactory.getLogger(Query2.class);

    public static void main(String[] args) {
        logger.info("Query2 Client Starting ...");

        try {

            // Initialize arguments and connection to hazelcast
            AppInit initConfigurator = new AppInit();
            HazelcastInstance hazelcastInstance = initConfigurator.getHazelcastInstance();
            String groupCode = AppInit.groupCode;
            String city = initConfigurator.getCity();

            final String qSize = System.getProperty("q");

            if (qSize == null) {
                System.err.println("Missing argument q (Query1 Client)");
                return;
            }
            double quadrantSize = Double.parseDouble(qSize);

            // Initialize KeyValueSource
            IMap<String, String> typesMap = hazelcastInstance.getMap(
                    groupCode + "-types-" + city
            );
            MultiMap<CoordinateNeighborhood,Complaint> complaintCount = hazelcastInstance.getMultiMap(
                    groupCode + "-complaints-count-" + city
            );

            logger.info("Inicio de la lectura del archivo");
            Instant parseStart = Instant.now();
            initConfigurator.parseCsv(
                    elem -> complaintCount.put(
                            new CoordinateNeighborhood(
                                    elem.getNeighborhood(), elem.getLatitude(), elem.getLongitude(), quadrantSize
                            ),
                            elem
                    ),
                    typesMap
            );
            Instant parseEnd = Instant.now();
            logger.info("Fin de la lectura del archivo");

            KeyValueSource<CoordinateNeighborhood,Complaint> source = KeyValueSource.fromMultiMap(complaintCount);

            // Job Tracker
            JobTracker jt = hazelcastInstance.getJobTracker(
                    groupCode + "complaint-type-tracker" + city
            );
            logger.info("Inicio del trabajo map/reduce");
            Instant mapReduceStart = Instant.now();
            Map<CoordinateNeighborhood,String> mostCommonByQuadrant = jt.newJob(source)
                    .mapper(new QuadrantTypeMapper())
                    .combiner(new TypeCountCombinerFactory())
                    .reducer(new TypeQuadrantReducerFactory())
                    .submit(new CommonTypeCollator())
                    .get();
            Instant mapReduceEnd = Instant.now();
            logger.info("Fin del trabajo map/reduce");

            // Parse output to csv
            Path csvPath = Paths.get(initConfigurator.getOutDirectory(), "query2.csv");

            List<String> lines = new ArrayList<>();
            lines.add("neighbourhood;quadLat;quadLon;topType"); // header

            for (Map.Entry<CoordinateNeighborhood, String> elem : mostCommonByQuadrant.entrySet()) {
                CoordinateNeighborhood key = elem.getKey();
                String line = "%s;%d;%d;%s".formatted(
                        key.getNeighborhood(), key.getXCoordinate(), key.getYCoordinate(), elem.getValue()
                );
                lines.add(line);
            }

            Files.write(csvPath, lines);

            // Parse times to csv
            Path timesCsvPath = Paths.get(initConfigurator.getOutDirectory(), "time2.csv");
            WriteTimesCsv.write(
                    timesCsvPath,
                    new TimeInterval(parseStart, parseEnd),
                    new TimeInterval(mapReduceStart, mapReduceEnd)
            );

        } catch (ExecutionException | InterruptedException | IOException e) {
            throw new RuntimeException(e);
        } finally {
            HazelcastClient.shutdownAll();
        }
    }

}