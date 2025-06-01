package ar.edu.itba.pod.client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MultiMap;
import com.hazelcast.mapreduce.JobTracker;
import com.hazelcast.mapreduce.KeyValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

public class Query1 {

    private static final Logger logger = LoggerFactory.getLogger(Query1.class);
    private static final List<String> csvFormat1 = List.of(
            "Unique Key", "Created Date", "Agency Name",
            "Complaint Type", "Incident Address", "Status",
            "Borough", "Latitude", "Longitude"
    );
    private static final List<String> csvFormat2 = List.of(
            "SR_NUMBER", "SR_SHORT_CODE", "OWNER_DEPARTMENT",
            "STATUS", "CREATED_DATE", "STREET_NUMBER",
            "STREET_DIRECTION", "STREET_NAME", "STREET_TYPE",
            "COMMUNITY_AREA", "LATITUDE", "LONGITUDE"
    );

    public static void main(String[] args) throws InterruptedException, IOException, ExecutionException {
        logger.info("Query1 Client Starting ...");

/*
        // TODO parsear addressesRawString para clientNetworkConfig más abajo
        final String addressesRawString = System.getProperty("addresses");
        final String city = System.getProperty("city");
        final String inPath = System.getProperty("inPath");
        final String outPath = System.getProperty("outPath");

        if (addressesRawString == null || city == null || inPath == null || outPath == null) {
            System.err.println("Missing argument (Query1 Client)");
            return;
        }
*/
        // TODO valores hardcodeados, usar parsing de arriba luego
        final String city = "NYC";
        final String inPath = "./data";
        final String outPath = ".";



        try {
            // Group Config
            GroupConfig groupConfig = new GroupConfig().setName("g3").setPassword("g3-pass");

            // Client Network Config
            ClientNetworkConfig clientNetworkConfig = new ClientNetworkConfig();
            clientNetworkConfig.addAddress("127.0.0.1");

            // Client Config
            ClientConfig clientConfig = new ClientConfig().setGroupConfig(groupConfig).setNetworkConfig(clientNetworkConfig);

            // Node Client
            HazelcastInstance hazelcastInstance = HazelcastClient.newHazelcastClient(clientConfig);

            // Job Tracker
            JobTracker jobTracker = hazelcastInstance.getJobTracker("totalClaimsPerAgencyAndClaimType");

            String claimsDate = "serviceRequests" + city;
            Stream<String> lines = Files.lines(Paths.get(args[0]), StandardCharsets.UTF_8);
            String firstCol = Arrays.stream(lines.findFirst().orElseThrow().split(";")).findFirst().orElseThrow();

            List<String> colFormat;

            if (firstCol.equals("Unique Key")) {
                colFormat = csvFormat1;
//                MultiMap<Integer, AgencyClaimTypePair> rentalsMultiMap = hazelcastInstance.getMultiMap("rentalsByStartStation");
//                KeyValueSource<Integer, AgencyClaimTypePair> rentalsKeyValueSource = KeyValueSource.fromMultiMap(rentalsMultiMap);

                // TODO parsear líneas del csv al mapa
            }
            else if (firstCol.equals("SR_NUMBER")) {
                colFormat = csvFormat2;
//                MultiMap<String, AgencyClaimTypePair> rentalsMultiMap = hazelcastInstance.getMultiMap("rentalsByStartStation");
//                KeyValueSource<String, AgencyClaimTypePair> rentalsKeyValueSource = KeyValueSource.fromMultiMap(rentalsMultiMap);

                // TODO parsear líneas del csv al mapa
            }
            else {
                System.err.println("Invalid CSV format");
            }

        } finally {
            HazelcastClient.shutdownAll();
        }
    }
}
