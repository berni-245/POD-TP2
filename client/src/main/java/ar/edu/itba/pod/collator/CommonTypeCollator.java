package ar.edu.itba.pod.collator;

import ar.edu.itba.pod.common.CoordinateNeighborhood;
import com.hazelcast.mapreduce.Collator;

import java.util.Comparator;
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

            // looks for the complaint type with the highest appearances (unties with alphabetic order)
            String mostCommon = counts.entrySet().stream()
                    .max(
                            Comparator.comparing(Map.Entry<String, Long>::getValue)
                                    .thenComparing(Map.Entry::getKey)

                    )
                    .map(Map.Entry::getKey)
                    .orElse("");
            result.put(quadrant, mostCommon);
        }
        return result;
    }
}
