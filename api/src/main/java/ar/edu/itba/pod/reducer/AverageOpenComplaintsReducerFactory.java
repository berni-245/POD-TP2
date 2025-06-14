package ar.edu.itba.pod.reducer;

import ar.edu.itba.pod.common.AgencyMonthKey;
import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;

@SuppressWarnings("deprecation")
public class AverageOpenComplaintsReducerFactory implements ReducerFactory<AgencyMonthKey, Integer, Integer> {
    @Override
    public Reducer<Integer, Integer> newReducer(AgencyMonthKey key) {
        return new Reducer<>() {
            int count = 0;

            @Override
            public void reduce(Integer value) {
                count += value;
            }

            @Override
            public Integer finalizeReduce() {
                return count;
            }
        };
    }
}