package ar.edu.itba.pod.reducer;

import ar.edu.itba.pod.common.CoordinateNeighborhood;
import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("deprecation")
public class TypeQuadrantReducerFactory implements ReducerFactory<CoordinateNeighborhood,String,Map<String,Long>> {
    @Override
    public Reducer<String, Map<String, Long>> newReducer(CoordinateNeighborhood coordinateNeighborhood) {
        return new Reducer<>() {
            private final Map<String, Long> count = new HashMap<>();

            @Override
            public void reduce(String s) {
                count.merge(s,1L, Long::sum);
            }

            @Override
            public Map<String, Long> finalizeReduce() {
                return count;
            }
        };
    }
}