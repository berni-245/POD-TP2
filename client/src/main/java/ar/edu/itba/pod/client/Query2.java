package ar.edu.itba.pod.client;

import ar.edu.itba.pod.collator.CommonTypeCollator;
import ar.edu.itba.pod.common.CoordinateNeighborhood;
import ar.edu.itba.pod.mapper.QuadrantTypeMapper;
import ar.edu.itba.pod.model.Complaint;
import ar.edu.itba.pod.reducer.TypeQuadrantReducerFactory;
import ar.edu.itba.pod.util.AppInit;
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
    private static final Logger logger = LoggerFactory.getLogger(Query2.class);

    public static void main(String[] args) {
        logger.info("Query2 Client Starting ...");

        try {

            // Initialize arguments and connection to hazelcast
            AppInit initConfigurator = new AppInit();
            HazelcastInstance hazelcastInstance = initConfigurator.getHazelcastInstance();
            String groupCode = AppInit.groupCode;
            String city = initConfigurator.getCity();

            final String qSize = System.getProperty("q");

            if (qSize == null) {
                System.err.println("Missing argument qSize (Query1 Client)");
                return;
            }
            double quadrantSize = Double.parseDouble(qSize);

            // Initialize KeyValueSource
            IMap<String, String> typesMap = hazelcastInstance.getMap(
                    groupCode + "-types-" + city
            );
            MultiMap<CoordinateNeighborhood,Complaint> complaintCount = hazelcastInstance.getMultiMap(
                    groupCode + "-complaints-count-" + city
            );

            initConfigurator.parseCsv(
                    elem -> complaintCount.put(
                            new CoordinateNeighborhood(
                                    elem.getNeighborhood(), elem.getLatitude(), elem.getLongitude(), quadrantSize
                            ),
                            elem
                    ),
                    typesMap
            );

            KeyValueSource<CoordinateNeighborhood,Complaint> source = KeyValueSource.fromMultiMap(complaintCount);
            JobTracker jt = hazelcastInstance.getJobTracker(
                    groupCode + "complaint-type-tracker" + city
            );
            Map<CoordinateNeighborhood,String> mostCommonByQuadrant = jt.newJob(source)
                    .mapper(new QuadrantTypeMapper())
                    .reducer(new TypeQuadrantReducerFactory())
                    .submit(new CommonTypeCollator())
                    .get();

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