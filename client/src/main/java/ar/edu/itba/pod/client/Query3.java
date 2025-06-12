package ar.edu.itba.pod.client;

import ar.edu.itba.pod.common.AverageOpenComplaintKey;
import ar.edu.itba.pod.common.MonthCount;
import ar.edu.itba.pod.mapper.AverageOpenComplaintsMapper;
import ar.edu.itba.pod.model.Complaint;
import ar.edu.itba.pod.reducers.AverageOpenComplaintsReducerFactory;
import ar.edu.itba.pod.util.City;
import ar.edu.itba.pod.util.CsvComplaintParser;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICompletableFuture;
import com.hazelcast.core.IList;
import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.Job;
import com.hazelcast.mapreduce.JobTracker;
import com.hazelcast.mapreduce.KeyValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@SuppressWarnings("deprecation")
public class Query3 {
    private static final Logger logger = LoggerFactory.getLogger(Query3.class);

    public static void main(String[] args) throws IOException {
        logger.info("Query3 Client Starting ...");

        final String address = System.getProperty("address");
        final String inPath = System.getProperty("inPath");
        final String outPath = System.getProperty("outPath");
        final String windowString = System.getProperty("w");
        final String cityString = System.getProperty("city");

        if (address == null || inPath == null || outPath == null || windowString == null || cityString == null) {
            System.err.println("Missing argument (Query3 Client)");
            return;
        }

        final int window = Integer.parseInt(windowString);
        final City city = City.fromString(cityString);

        if (window < 1 || window > 12) {
            System.out.println("Invalid window number: " + window);
        }

        try {
            // Group Config
            String groupCode = "g3";
            GroupConfig groupConfig = new GroupConfig().setName(groupCode).setPassword(groupCode + "-pass");
            // Client Network Config
            ClientNetworkConfig clientNetworkConfig = new ClientNetworkConfig();
            clientNetworkConfig.addAddress("127.0.0.1");
            // Client Config
            ClientConfig clientConfig = new ClientConfig().setGroupConfig(groupConfig).setNetworkConfig(clientNetworkConfig);
            // Node Client
            HazelcastInstance hazelcastInstance = HazelcastClient.newHazelcastClient(clientConfig);
            // Job Tracker
            JobTracker jobTracker = hazelcastInstance.getJobTracker("averageOpenComplaints");

            String complainsPath = inPath + "/serviceRequests" + city + ".csv";
            String typesPath = inPath + "/serviceTypes" + city + ".csv";
            City cityFormat = CsvComplaintParser.getCityFormat(complainsPath);

            String complainsMapName = groupCode + "-complains-" + city;
            String typesMapName = groupCode + "-types-" + city;

            IMap<String, String> typesMap = hazelcastInstance.getMap(typesMapName);
            IMap<String, Complaint> complainsMap = hazelcastInstance.getMap(complainsMapName);
            CsvComplaintParser.parseCsv(complainsPath, typesPath, cityFormat, elem -> complainsMap.put(elem.getId(), elem), typesMap);

            KeyValueSource<String, Complaint> source = KeyValueSource.fromMap(complainsMap);
            Job<String, Complaint> job = jobTracker.newJob(source);

            Map<AverageOpenComplaintKey, List<MonthCount>> result = job
                    .mapper(new AverageOpenComplaintsMapper())
                    .reducer(new AverageOpenComplaintsReducerFactory())
                    .submit()
                    .get();

        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            HazelcastClient.shutdownAll();
        }
    }
}