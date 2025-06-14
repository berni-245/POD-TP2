package ar.edu.itba.pod.client;

import ar.edu.itba.pod.collator.CountAgencyComplaintTypeCollator;
import ar.edu.itba.pod.common.AgencyComplaintTypePair;
import ar.edu.itba.pod.common.StreetComplaintTypePair;
import ar.edu.itba.pod.key_predicate.FilterForNeighborhoodKeyPred;
import ar.edu.itba.pod.mapper.CountAgencyComplaintTypeMapper;
import ar.edu.itba.pod.mapper.StreetComplaintTypeMapper;
import ar.edu.itba.pod.model.Complaint;
import ar.edu.itba.pod.reducer.CountAgencyComplaintTypeReducerFactory;
import ar.edu.itba.pod.reducer.StreetComplaintTypeReducerFactory;
import ar.edu.itba.pod.util.City;
import ar.edu.itba.pod.util.CsvComplaintParser;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICompletableFuture;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MultiMap;
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

public class Query1 {

    private static final Logger logger = LoggerFactory.getLogger(Query1.class);

    @SuppressWarnings("deprecation")
    public static void main(String[] args) {
        logger.info("Query1 Client Starting ...");


        String addressesRawString = System.getProperty("addresses");
        String city = System.getProperty("city");
        final String inPath = System.getProperty("inPath");
        final String outPath = System.getProperty("outPath");

        if (addressesRawString == null || city == null || inPath == null || outPath == null) {
            System.err.println("Missing argument (Query1 Client)");
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
            IMap<String, Complaint> complainsMap = hazelcastInstance.getMap(
                    groupCode + "-complains-" + city
            );
            CsvComplaintParser.parseCsv(complainsPath, typesPath, cityFormat, elem -> complainsMap.put(elem.getId(), elem), typesMap);
            KeyValueSource<String, Complaint> source = KeyValueSource.fromMap(complainsMap);

            // Job Tracker
            JobTracker jobTracker = hazelcastInstance.getJobTracker(
                    groupCode + "-count-agency-and-complaint-type-pair-" + city
            );

            Job<String, Complaint> jobCount = jobTracker.newJob(source);
            ICompletableFuture<Map<AgencyComplaintTypePair, Integer>> futureCount = jobCount
                    .mapper(new CountAgencyComplaintTypeMapper())
                    .reducer(new CountAgencyComplaintTypeReducerFactory())
                    .submit(new CountAgencyComplaintTypeCollator());
            Map<AgencyComplaintTypePair, Integer> resultAgencyComplaintTypeCount = futureCount.get();

            // Parse output to csv
            Path csvPath = Paths.get(outPath, "query1.csv");

            List<String> lines = new ArrayList<>();
            lines.add("type;agency;requests"); // header

            for (Map.Entry<AgencyComplaintTypePair, Integer> elem : resultAgencyComplaintTypeCount.entrySet()) {
                AgencyComplaintTypePair pair = elem.getKey();
                String line = pair.getAgency() + ";" + pair.getClaimType() + ";" + elem.getValue();
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
