package ar.edu.itba.pod.client;

import ar.edu.itba.pod.model.Complaint;
import ar.edu.itba.pod.util.City;
import ar.edu.itba.pod.util.CsvComplaintParser;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.*;
import com.hazelcast.mapreduce.JobTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;

public class Query4 {

    private static final Logger logger = LoggerFactory.getLogger(CsvComplaintParser.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public static void main(String[] args) {
        logger.info("Query4 Client Starting ...");


        final String addressesRawString = System.getProperty("addresses");
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
            for (String address : addressesRawString.split(";"))
                clientNetworkConfig.addAddress(address);

            // Client Config
            ClientConfig clientConfig = new ClientConfig().setGroupConfig(groupConfig).setNetworkConfig(clientNetworkConfig);

            // Node Client
            HazelcastInstance hazelcastInstance = HazelcastClient.newHazelcastClient(clientConfig);

            // Job Tracker
            JobTracker jobTracker = hazelcastInstance.getJobTracker("totalClaimsPerAgencyAndClaimType");

            String complainsPath = inPath + "/serviceRequests" + city + ".csv";
            String typesPath = inPath + "/serviceTypes" + city + ".csv";
            City cityFormat = CsvComplaintParser.getCityFormat(complainsPath);

            String complainsMapName = groupCode + "-complains-" + city;
            String typesMapName = groupCode + "-types-" + city;

            IMap<String, String> typesMap = hazelcastInstance.getMap(typesMapName);
            IMap<String, Complaint> complainsMap = hazelcastInstance.getMap(complainsMapName);
            CsvComplaintParser.parseCsv(complainsPath, typesPath, cityFormat, elem -> complainsMap.put(elem.getId(), elem), typesMap);



        } finally {
            HazelcastClient.shutdownAll();
        }
    }
}
