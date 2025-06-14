package ar.edu.itba.pod.reducer;

import ar.edu.itba.pod.common.AgencyComplaintTypePair;
import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;

@SuppressWarnings("deprecation")
public class CountAgencyComplaintTypeReducerFactory implements ReducerFactory<AgencyComplaintTypePair, Integer, Integer> {
    @Override
    public Reducer<Integer, Integer> newReducer(AgencyComplaintTypePair agencyComplaintTypePair) {
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
