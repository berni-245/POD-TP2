package ar.edu.itba.pod.reducer;

import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;

@SuppressWarnings("deprecation")
public class CountComplaintTypeReducerFactory implements ReducerFactory<String, Integer, Integer> {
    @Override
    public Reducer<Integer, Integer> newReducer(String s) {
        return new Reducer<>() {
            int count = 0;

            @Override
            public void reduce(Integer integer) {
                count += integer;
            }

            @Override
            public Integer finalizeReduce() {
                return count;
            }
        };
    }
}
