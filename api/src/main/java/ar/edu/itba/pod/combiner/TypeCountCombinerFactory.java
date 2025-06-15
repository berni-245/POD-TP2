package ar.edu.itba.pod.combiner;

import ar.edu.itba.pod.common.CoordinateNeighborhood;
import com.hazelcast.mapreduce.Combiner;
import com.hazelcast.mapreduce.CombinerFactory;

import java.util.HashMap;
import java.util.Map;

public class TypeCountCombinerFactory
        implements CombinerFactory<CoordinateNeighborhood,String,Map<String,Long>> {

    @Override
    public Combiner<String, Map<String,Long>> newCombiner(CoordinateNeighborhood coordinateNeighborhood) {
        return new Combiner<>() {
            private final Map<String, Long> map = new HashMap<>();

            @Override
            public void combine(String type) {
                map.merge(type, 1L, Long::sum);
            }

            @Override
            public Map<String,Long> finalizeChunk() {
                Map<String,Long> out = new HashMap<>(map);
                map.clear();
                return out;
            }
        };
    }
}
