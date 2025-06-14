package ar.edu.itba.pod.collator;

import ar.edu.itba.pod.common.AgencyComplaintTypePair;
import com.hazelcast.mapreduce.Collator;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@SuppressWarnings("deprecation")
public class CountAgencyComplaintTypeCollator implements Collator<Map.Entry<AgencyComplaintTypePair, Integer>, Map<AgencyComplaintTypePair, Integer>> {

    @Override
    public Map<AgencyComplaintTypePair, Integer> collate(Iterable<Map.Entry<AgencyComplaintTypePair, Integer>> values) {
        return StreamSupport.stream(values.spliterator(), false)
                .sorted(Map.Entry.<AgencyComplaintTypePair, Integer>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
}
