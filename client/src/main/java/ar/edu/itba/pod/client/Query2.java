package ar.edu.itba.pod.client;

import ar.edu.itba.pod.collator.CommonTypeCollator;
import ar.edu.itba.pod.common.CoordinateNeighborhood;
import ar.edu.itba.pod.mapper.QuadrantTypeMapper;
import ar.edu.itba.pod.model.Complaint;
import ar.edu.itba.pod.reducer.TypeQuadrantReducerFactory;
import ar.edu.itba.pod.util.City;
import ar.edu.itba.pod.util.CsvComplaintParser;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MultiMap;
import com.hazelcast.mapreduce.JobTracker;
import com.hazelcast.mapreduce.KeyValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@SuppressWarnings("deprecation")
public class Query2 {
    private static final Logger logger = LoggerFactory.getLogger(CsvComplaintParser.class);

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

        float quadrantSize = Float.parseFloat(qSize);

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
            Map<CoordinateNeighborhood,String> mostCommonByQuadrant = jt.newJob(source).mapper(new QuadrantTypeMapper()).reducer(new TypeQuadrantReducerFactory()).submit(new CommonTypeCollator()).get();

            mostCommonByQuadrant.forEach((quadrant, complaintType) ->
                    System.out.printf("%s - %s%n",quadrant,complaintType)
            );

        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            HazelcastClient.shutdownAll();
        }
    }

}


//MultiMap<Neighboorhood,Map.Entry<Type,Count>>
//Map<Neightborhood,Type>