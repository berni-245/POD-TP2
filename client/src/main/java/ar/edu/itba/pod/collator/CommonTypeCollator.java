package ar.edu.itba.pod.collator;

import ar.edu.itba.pod.common.CoordinateNeighborhood;
import com.hazelcast.mapreduce.Collator;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("deprecation")
public class CommonTypeCollator implements Collator<Map.Entry<CoordinateNeighborhood,Map<String,Long>>,
        Map<CoordinateNeighborhood,String>> {
    @Override
    public Map<CoordinateNeighborhood, String> collate(Iterable<Map.Entry<CoordinateNeighborhood, Map<String, Long>>> iterable) {
        Map<CoordinateNeighborhood,String> result = new HashMap<>();
        for(Map.Entry<CoordinateNeighborhood,Map<String, Long>> entry : iterable) {
            CoordinateNeighborhood quadrant = entry.getKey();
            Map<String, Long> counts = entry.getValue();
            String mostCommon = counts.entrySet().stream()
                    .max((e1, e2) -> {
                        int cmp = e1.getValue().compareTo(e2.getValue());
                        return (cmp != 0) ? cmp : e2.getKey().compareTo(e1.getKey()); // with type name alphabetically as tie-breaker condition
                    })
                    .map(Map.Entry::getKey)
                    .orElse("");
            result.put(quadrant, mostCommon);
        }
        return result;
    }
}
