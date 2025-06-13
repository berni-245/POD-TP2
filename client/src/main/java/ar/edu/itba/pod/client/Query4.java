package ar.edu.itba.pod.client;

import ar.edu.itba.pod.collator.CountComplaintTypePercentageCollator;
import ar.edu.itba.pod.common.StreetClaimTypePair;
import ar.edu.itba.pod.key_predicate.FilterForNeighborhoodKeyPred;
import ar.edu.itba.pod.mapper.CountComplaintTypeMapper;
import ar.edu.itba.pod.mapper.StreetComplaintTypeMapper;
import ar.edu.itba.pod.model.Complaint;
import ar.edu.itba.pod.reducer.CountComplaintTypeReducerFactory;
import ar.edu.itba.pod.reducer.StreetComplaintTypeReducerFactory;
import ar.edu.itba.pod.util.City;
import ar.edu.itba.pod.util.CsvComplaintParser;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.GroupConfig;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@SuppressWarnings("deprecation")
public class Query4 {

    private static final Logger logger = LoggerFactory.getLogger(CsvComplaintParser.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public static void main(String[] args) {
        logger.info("Query4 Client Starting ...");


        String addressesRawString = System.getProperty("addresses");
        String city = System.getProperty("city");
        final String inPath = System.getProperty("inPath");
        final String outPath = System.getProperty("outPath");
        final String neighborhood = System.getProperty("neighbourhood");

        if (addressesRawString == null || city == null || inPath == null || outPath == null) {
            System.err.println("Missing argument (Query1 Client)");
            return;
        }

        if (neighborhood == null) {
            System.err.println("Missing argument (Neighbourhood)");
            return;
        }

        city = city.toUpperCase();

        try {
            // Group Config
            String groupCode = "g3";
            GroupConfig groupConfig = new GroupConfig().setName(groupCode).setPassword(groupCode + "-pass");

            // Client Network Config
            ClientNetworkConfig clientNetworkConfig = new ClientNetworkConfig();
            addressesRawString = addressesRawString.replace("'", "");
            for (String address : addressesRawString.split(";"))
                clientNetworkConfig.addAddress(address);

            // Client Config
            ClientConfig clientConfig = new ClientConfig().setGroupConfig(groupConfig).setNetworkConfig(clientNetworkConfig);

            // Node Client
            HazelcastInstance hazelcastInstance = HazelcastClient.newHazelcastClient(clientConfig);

            // Path files
            String complainsPath = inPath + "/serviceRequests" + city + ".csv";
            String typesPath = inPath + "/serviceTypes" + city + ".csv";

            City cityFormat = CsvComplaintParser.getCityFormat(complainsPath);

            // Initialize KeyValueSource
            IMap<String, String> typesMap = hazelcastInstance.getMap(
                    groupCode + "-types-" + city
            );
            MultiMap<String, Complaint> complainsMap = hazelcastInstance.getMultiMap(
                    groupCode + "-neighborhood-complains-" + city
            );
            CsvComplaintParser.parseCsv(complainsPath, typesPath, cityFormat, elem -> complainsMap.put(elem.getNeighborhood(), elem), typesMap);
            KeyValueSource<String, Complaint> complaintsVS = KeyValueSource.fromMultiMap(complainsMap);

            // Job remove duplicates (first map-reduce)
            JobTracker jobTrackerDup = hazelcastInstance.getJobTracker(groupCode + "-remove-duplicates-" + city);
            Job<String, Complaint> removeDuplicates = jobTrackerDup.newJob(complaintsVS);
            ICompletableFuture<Map<StreetClaimTypePair, String>> futureDup = removeDuplicates
                    .keyPredicate(new FilterForNeighborhoodKeyPred(neighborhood))
                    .mapper(new StreetComplaintTypeMapper())
                    .reducer(new StreetComplaintTypeReducerFactory())
                    .submit();
            Map<StreetClaimTypePair, String> resultDup = futureDup.get();

            // Parse for next job
            MultiMap<String, String> complaintsTypesPerStreetMap = hazelcastInstance.getMultiMap(
                    groupCode + "-unique-street-and-complaints-" + city
            );
            for (StreetClaimTypePair elem : resultDup.keySet()) {
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
            Path csvPath = Paths.get(outPath, "query4.csv");

            List<String> lines = new ArrayList<>();
            lines.add("street;typePercentage"); // encabezado CSV

            for (Map.Entry<String, String> elem : resultComplaintTypePercentage.entrySet()) {
                String line = elem.getKey() + ";" + elem.getValue();
                lines.add(line);
            }

            try {
                Files.write(csvPath, lines);
            } catch (IOException e) {
                System.out.println("Error writing the output file");
            }


        }
        catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        finally {
            HazelcastClient.shutdownAll();
        }
    }
}
