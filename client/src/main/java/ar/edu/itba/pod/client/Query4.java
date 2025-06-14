package ar.edu.itba.pod.client;

import ar.edu.itba.pod.collator.CountComplaintTypePercentageCollator;
import ar.edu.itba.pod.common.StreetComplaintTypePair;
import ar.edu.itba.pod.key_predicate.FilterForNeighborhoodKeyPred;
import ar.edu.itba.pod.mapper.CountComplaintTypeMapper;
import ar.edu.itba.pod.mapper.StreetComplaintTypeMapper;
import ar.edu.itba.pod.model.Complaint;
import ar.edu.itba.pod.reducer.CountComplaintTypeReducerFactory;
import ar.edu.itba.pod.reducer.StreetComplaintTypeReducerFactory;
import ar.edu.itba.pod.util.CsvComplaintParser;
import ar.edu.itba.pod.util.AppInit;
import ar.edu.itba.pod.util.TimeInterval;
import ar.edu.itba.pod.util.WriteTimesCsv;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.*;
import com.hazelcast.mapreduce.Job;
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
public class Query4 {

    private static final Logger logger = LoggerFactory.getLogger(Query4.class);

    public static void main(String[] args) {
        logger.info("Query4 Client Starting ...");

        try {

            // Initialize arguments and connection to hazelcast
            AppInit initConfigurator = new AppInit();
            HazelcastInstance hazelcastInstance = initConfigurator.getHazelcastInstance();
            String groupCode = AppInit.groupCode;
            String city = initConfigurator.getCity();

            String neighborhood = System.getProperty("neighbourhood");

            if (neighborhood == null) {
                System.err.println("Missing argument (Neighbourhood)");
                return;
            }
            neighborhood = neighborhood.replace('_', ' ');

            // Initialize KeyValueSource
            IMap<String, String> typesMap = hazelcastInstance.getMap(
                    groupCode + "-types-" + city
            );
            MultiMap<String, Complaint> complainsMap = hazelcastInstance.getMultiMap(
                    groupCode + "-neighborhood-complains-" + city
            );
            logger.info("Inicio de la lectura del archivo");
            Instant parseStart = Instant.now();
            initConfigurator.parseCsv(elem -> complainsMap.put(elem.getNeighborhood(), elem), typesMap);
            Instant parseEnd = Instant.now();
            logger.info("Fin de la lectura del archivo");

            KeyValueSource<String, Complaint> complaintsVS = KeyValueSource.fromMultiMap(complainsMap);

            // Job remove duplicates (first map-reduce)
            JobTracker jobTrackerDup = hazelcastInstance.getJobTracker(
                    groupCode + "-remove-duplicates-" + city
            );
            Job<String, Complaint> removeDuplicates = jobTrackerDup.newJob(complaintsVS);
            logger.info("Inicio del trabajo 1 map/reduce");
            Instant mapReduce1Start = Instant.now();
            Map<StreetComplaintTypePair, String> resultDup = removeDuplicates
                    .keyPredicate(new FilterForNeighborhoodKeyPred(neighborhood))
                    .mapper(new StreetComplaintTypeMapper())
                    .reducer(new StreetComplaintTypeReducerFactory())
                    .submit()
                    .get();
            Instant mapReduce1End = Instant.now();
            logger.info("Fin del trabajo 1 map/reduce");

            // Parse for next job
            MultiMap<String, String> complaintsTypesPerStreetMap = hazelcastInstance.getMultiMap(
                    groupCode + "-unique-street-and-complaints-" + city
            );
            for (StreetComplaintTypePair elem : resultDup.keySet()) {
                complaintsTypesPerStreetMap.put(elem.getStreet(), elem.getClaimType());
            }
            KeyValueSource<String, String> complaintsTypesPerStreetVS = KeyValueSource.fromMultiMap(complaintsTypesPerStreetMap);

            // Job count different complaints types (second map-reduce)
            JobTracker jobTrackerDifTypes = hazelcastInstance.getJobTracker(
                    groupCode + "-count-different-complaints-types-" + city
            );
            Job<String, String> countDifComplaintsTypes = jobTrackerDifTypes.newJob(
                    complaintsTypesPerStreetVS
            );

            logger.info("Inicio del trabajo 2 map/reduce");
            Instant mapReduce2Start = Instant.now();
            Map<String, String> resultComplaintTypePercentage = countDifComplaintsTypes
                    .mapper(new CountComplaintTypeMapper())
                    .reducer(new CountComplaintTypeReducerFactory())
                    .submit(new CountComplaintTypePercentageCollator(typesMap.size()))
                    .get();
            Instant mapReduce2End = Instant.now();
            logger.info("Fin del trabajo 2 map/reduce");

            // Parse output to csv
            Path csvPath = Paths.get(initConfigurator.getOutDirectory(), "query4.csv");

            List<String> lines = new ArrayList<>();
            lines.add("street;typePercentage"); // header

            for (Map.Entry<String, String> elem : resultComplaintTypePercentage.entrySet()) {
                String line = elem.getKey() + ";" + elem.getValue();
                lines.add(line);
            }

            Files.write(csvPath, lines);

            // Parse times to csv
            Path timesCsvPath = Paths.get(initConfigurator.getOutDirectory(), "times4.csv");
            WriteTimesCsv.write(
                    timesCsvPath,
                    new TimeInterval(parseStart, parseEnd),
                    new TimeInterval(mapReduce1Start, mapReduce1End),
                    new TimeInterval(mapReduce2Start, mapReduce2End)
            );
        }
        catch (InterruptedException | ExecutionException | IOException e) {
            System.err.println(e.getMessage());
        }
        finally {
            HazelcastClient.shutdownAll();
        }
    }
}
