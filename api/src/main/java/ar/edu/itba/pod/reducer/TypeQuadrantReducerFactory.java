package ar.edu.itba.pod.reducer;

import ar.edu.itba.pod.common.CoordinateNeighborhood;
import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("deprecation")
public class TypeQuadrantReducerFactory implements ReducerFactory<CoordinateNeighborhood,Map<String,Long>,Map<String,Long>> {
    @Override
    public Reducer<Map<String,Long>, Map<String, Long>> newReducer(CoordinateNeighborhood coordinateNeighborhood) {
        return new Reducer<>() {
            private final Map<String, Long> count = new HashMap<>();

            @Override
            public void reduce(Map<String, Long> map) {
                for (Map.Entry<String, Long> entry : map.entrySet()) {
                    count.merge(entry.getKey(), entry.getValue(), Long::sum);
                }
            }

            @Override
            public Map<String, Long> finalizeReduce() {
                return count;
            }
        };
    }
}