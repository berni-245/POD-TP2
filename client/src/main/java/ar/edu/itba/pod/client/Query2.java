package ar.edu.itba.pod.client;

import ar.edu.itba.pod.common.CoordinateNeighborhood;
import ar.edu.itba.pod.mapper.QuadrantTypeMapper;
import ar.edu.itba.pod.model.Complaint;
import ar.edu.itba.pod.model.ComplaintChicago;
import ar.edu.itba.pod.model.ComplaintNYC;
import ar.edu.itba.pod.util.City;
import ar.edu.itba.pod.util.CsvComplaintParser;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IList;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MultiMap;
import com.hazelcast.mapreduce.JobTracker;
import com.hazelcast.mapreduce.KeyValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

@SuppressWarnings("deprecation")
public class Query2 {
    private static final Logger logger = LoggerFactory.getLogger(CsvComplaintParser.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public static void main(String[] args) {
        logger.info("Query2 Client Starting ...");


        final String addressesRawString = System.getProperty("addresses");
        String city = System.getProperty("city");
        final String inPath = System.getProperty("inPath");
        final String outPath = System.getProperty("outPath");
        final String qSize = System.getProperty("qSize");

        if (addressesRawString == null || city == null || inPath == null || outPath == null || qSize == null) {
            System.err.println("Missing argument (Query1 Client)");
            return;
        }

        Float quadrantSize = Float.parseFloat(qSize);

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

            String complaintsPath = inPath + "/serviceRequests" + city + ".csv";
            String typesPath = inPath + "/serviceTypes" + city + ".csv";
            City cityFormat = CsvComplaintParser.getCityFormat(complaintsPath);

            String complaintsMapName = groupCode + "-complaints-" + city;
            String typesMapName = groupCode + "-types-" + city;

            IMap<String, String> typesMap = hazelcastInstance.getMap(typesMapName);
            IMap<String, Complaint> complaintsMap = hazelcastInstance.getMap(complaintsMapName);
            CsvComplaintParser.parseCsv(complaintsPath, typesPath, cityFormat, elem -> complaintsMap.put(elem.getId(), elem), typesMap);

            MultiMap<CoordinateNeighborhood,Complaint> complaintCount = hazelcastInstance.getMultiMap(complaintsMapName);

            for(Complaint complaint : complaintsMap.values()) {
                CoordinateNeighborhood cn = new CoordinateNeighborhood(complaint.getNeighborhood(), (int) (complaint.getLongitude()/quadrantSize), (int) (complaint.getLatitude()/quadrantSize));
                complaintCount.put(cn, complaint);
            }

            KeyValueSource<CoordinateNeighborhood,Complaint> source = KeyValueSource.fromMultiMap(complaintCount);
            JobTracker jt = hazelcastInstance.getJobTracker("complaintTypeTracker");
            jt.newJob(source).mapper(new QuadrantTypeMapper());

        } finally {
            HazelcastClient.shutdownAll();
        }
    }

}


//MultiMap<Neighboorhood,Map.Entry<Type,Count>>
//Map<Neightborhood,Type>