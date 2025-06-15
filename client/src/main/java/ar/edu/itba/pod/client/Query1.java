package ar.edu.itba.pod.client;

import ar.edu.itba.pod.collator.CountAgencyComplaintTypeCollator;
import ar.edu.itba.pod.common.AgencyComplaintTypePair;
import ar.edu.itba.pod.mapper.CountAgencyComplaintTypeMapper;
import ar.edu.itba.pod.model.Complaint;
import ar.edu.itba.pod.reducer.CountAgencyComplaintTypeReducerFactory;
import ar.edu.itba.pod.util.AppInit;
import ar.edu.itba.pod.util.TimeInterval;
import ar.edu.itba.pod.util.WriteTimesCsv;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICompletableFuture;
import com.hazelcast.core.IMap;
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
public class Query1 {

    private static final Logger logger = LoggerFactory.getLogger(Query1.class);

    public static void main(String[] args) {
        logger.info("Query1 Client Starting ...");

        try {

            // Initialize arguments and connection to hazelcast
            AppInit initConfigurator = new AppInit();
            HazelcastInstance hazelcastInstance = initConfigurator.getHazelcastInstance();
            String groupCode = AppInit.groupCode;
            String city = initConfigurator.getCity();

            // Initialize KeyValueSource
            IMap<String, String> typesMap = hazelcastInstance.getMap(
                    groupCode + "-types-" + city
            );
            IMap<String, Complaint> complainsMap = hazelcastInstance.getMap(
                    groupCode + "-complains-" + city
            );
            logger.info("Inicio de la lectura del archivo");
            Instant parseStart = Instant.now();
            initConfigurator.parseCsv(elem -> complainsMap.put(elem.getId(), elem), typesMap);
            Instant parseEnd = Instant.now();
            logger.info("Fin de la lectura del archivo");

            KeyValueSource<String, Complaint> source = KeyValueSource.fromMap(complainsMap);

            // Job Tracker
            JobTracker jobTracker = hazelcastInstance.getJobTracker(
                    groupCode + "-count-agency-and-complaint-type-pair-" + city
            );

            Job<String, Complaint> jobCount = jobTracker.newJob(source);
            logger.info("Inicio del trabajo map/reduce");
            Instant mapReduceStart = Instant.now();
            Map<AgencyComplaintTypePair, Integer> resultAgencyComplaintTypeCount = jobCount
                    .mapper(new CountAgencyComplaintTypeMapper())
                    .reducer(new CountAgencyComplaintTypeReducerFactory())
                    .submit(new CountAgencyComplaintTypeCollator())
                    .get();
            Instant mapReduceEnd = Instant.now();
            logger.info("Fin del trabajo map/reduce");

            // Parse output to csv
            Path csvPath = Paths.get(initConfigurator.getOutDirectory(), "query1.csv");

            List<String> lines = new ArrayList<>();
            lines.add("type;agency;requests"); // header

            for (Map.Entry<AgencyComplaintTypePair, Integer> elem : resultAgencyComplaintTypeCount.entrySet()) {
                AgencyComplaintTypePair pair = elem.getKey();
                String line = pair.getAgency() + ";" + pair.getClaimType() + ";" + elem.getValue();
                lines.add(line);
            }

            Files.write(csvPath, lines);

            // Parse times to csv
            Path timesCsvPath = Paths.get(initConfigurator.getOutDirectory(), "time1.csv");
            WriteTimesCsv.write(
                    timesCsvPath,
                    new TimeInterval(parseStart, parseEnd),
                    new TimeInterval(mapReduceStart, mapReduceEnd)
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
