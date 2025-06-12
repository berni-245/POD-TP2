package ar.edu.itba.pod.reducers;

import ar.edu.itba.pod.common.AverageOpenComplaintKey;
import ar.edu.itba.pod.common.MonthCount;
import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class AverageOpenComplaintsReducerFactory implements ReducerFactory<AverageOpenComplaintKey, MonthCount, List<MonthCount>> {
    @Override
    public Reducer<MonthCount, List<MonthCount>> newReducer(AverageOpenComplaintKey key) {
        return new Reducer<>() {
            private final List<MonthCount> list = new ArrayList<>();

            @Override
            public void reduce(MonthCount value) {
                list.add(value);
            }

            @Override
            public List<MonthCount> finalizeReduce() {
                return list;
            }
        };
    }
}
