package ar.edu.itba.pod.client;

import ar.edu.itba.pod.collator.AverageOpenComplaintsCollator;
import ar.edu.itba.pod.mapper.AverageOpenComplaintsMapper;
import ar.edu.itba.pod.model.Complaint;
import ar.edu.itba.pod.reducer.AverageOpenComplaintsReducerFactory;
import ar.edu.itba.pod.util.*;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
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
import java.util.concurrent.ExecutionException;

@SuppressWarnings("deprecation")
public class Query3 {
    private static final Logger logger = LoggerFactory.getLogger(Query3.class);

    public static void main(String[] args) {
        logger.info("Query3 Client Starting ...");

        try {

            // Initialize arguments and connection to hazelcast
            AppInit initConfigurator = new AppInit();
            HazelcastInstance hazelcastInstance = initConfigurator.getHazelcastInstance();
            String groupCode = AppInit.groupCode;
            String city = initConfigurator.getCity();

            final String windowString = System.getProperty("w");
            if (windowString == null) {
                System.err.println("Missing argument (Query3 Client)");
                return;
            }
            final int window = Integer.parseInt(windowString);
            if (window < 1 || window > 12) {
                System.err.println("Invalid window number: " + window);
                return;
            }

            // Initialize KeyValueSource
            IMap<String, String> typesMap = hazelcastInstance.getMap(
                    groupCode + "-types-" + city
            );
            IMap<String, Complaint> complainsMap = hazelcastInstance.getMap(
                    groupCode + "-complains-" + city
            );
            logger.info("Inicio de la lectura del archivo");
            Instant parseStart = Instant.now();
            initConfigurator.parseCsv(elem -> complainsMap.put(elem.getNeighborhood(), elem), typesMap);
            Instant parseEnd = Instant.now();
            logger.info("Fin de la lectura del archivo");

            KeyValueSource<String, Complaint> source = KeyValueSource.fromMap(complainsMap);

            // Job Tracker
            JobTracker jobTracker = hazelcastInstance.getJobTracker(
                    groupCode + "-average-open-complaints-" + city
            );
            logger.info("Inicio del trabajo map/reduce");
            Instant mapReduceStart = Instant.now();
            List<String> result = jobTracker.newJob(source)
                    .mapper(new AverageOpenComplaintsMapper())
                    .reducer(new AverageOpenComplaintsReducerFactory())
                    .submit(new AverageOpenComplaintsCollator(window))
                    .get();
            Instant mapReduceEnd = Instant.now();
            logger.info("Fin del trabajo map/reduce");

            result.forEach(System.out::println);

            // Parse output to csv
            Path csvPath = Paths.get(initConfigurator.getOutDirectory(), "query3.csv");

            List<String> lines = new ArrayList<>();
            lines.add("agency;year;month;movingAvg"); // header

            lines.addAll(result);

            Files.write(csvPath, lines);

            // Parse times to csv
            Path timesCsvPath = Paths.get(initConfigurator.getOutDirectory(), "times3.csv");
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