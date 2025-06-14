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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@SuppressWarnings("deprecation")
public class Query4 {

    private static final Logger logger = LoggerFactory.getLogger(CsvComplaintParser.class);

    public static void main(String[] args) {
        logger.info("Query4 Client Starting ...");

        // Initialize connection to hazelcast
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
        initConfigurator.parseCsv(elem -> complainsMap.put(elem.getNeighborhood(), elem), typesMap);
        KeyValueSource<String, Complaint> complaintsVS = KeyValueSource.fromMultiMap(complainsMap);

        // Job remove duplicates (first map-reduce)
        JobTracker jobTrackerDup = hazelcastInstance.getJobTracker(
                groupCode + "-remove-duplicates-" + city
        );
        Job<String, Complaint> removeDuplicates = jobTrackerDup.newJob(complaintsVS);
        ICompletableFuture<Map<StreetComplaintTypePair, String>> futureDup = removeDuplicates
                .keyPredicate(new FilterForNeighborhoodKeyPred(neighborhood))
                .mapper(new StreetComplaintTypeMapper())
                .reducer(new StreetComplaintTypeReducerFactory())
                .submit();
        try {
            Map<StreetComplaintTypePair, String> resultDup = futureDup.get();

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
            ICompletableFuture<Map<String, String>> futureCountDif = countDifComplaintsTypes
                    .mapper(new CountComplaintTypeMapper())
                    .reducer(new CountComplaintTypeReducerFactory())
                    .submit(new CountComplaintTypePercentageCollator(typesMap.size()));
            Map<String, String> resultComplaintTypePercentage = futureCountDif.get();

            // Parse output to csv
            Path csvPath = Paths.get(initConfigurator.getOutDirectory(), "query4.csv");

            List<String> lines = new ArrayList<>();
            lines.add("street;typePercentage"); // header

            for (Map.Entry<String, String> elem : resultComplaintTypePercentage.entrySet()) {
                String line = elem.getKey() + ";" + elem.getValue();
                lines.add(line);
            }

            Files.write(csvPath, lines);
        }
        catch (InterruptedException | ExecutionException | IOException e) {
            System.err.println(e.getMessage());
        }
        finally {
            HazelcastClient.shutdownAll();
        }
    }
}
