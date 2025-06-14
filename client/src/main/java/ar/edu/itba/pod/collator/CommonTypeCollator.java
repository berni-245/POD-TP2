package ar.edu.itba.pod.collator;

import ar.edu.itba.pod.common.CoordinateNeighborhood;
import com.hazelcast.mapreduce.Collator;

import java.util.HashMap;
import java.util.Map;

public class CommonTypeCollator implements Collator<Map.Entry<CoordinateNeighborhood,Map<String,Long>>,
        Map<CoordinateNeighborhood,String>> {
    @Override
    public Map<CoordinateNeighborhood, String> collate(Iterable<Map.Entry<CoordinateNeighborhood, Map<String, Long>>> iterable) {
        Map<CoordinateNeighborhood,String> result = new HashMap<>();
        for(Map.Entry<CoordinateNeighborhood,Map<String, Long>> entry : iterable) {
            var quadrant = entry.getKey();
            var counts = entry.getValue();

            String mostCommon = counts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("");
            result.put(quadrant, mostCommon);
        }
        return result;
    }
}
