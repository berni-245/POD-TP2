package ar.edu.itba.pod.reducer;

import ar.edu.itba.pod.common.StreetComplaintTypePair;
import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;

@SuppressWarnings("deprecation")
public class StreetComplaintTypeReducerFactory implements ReducerFactory<StreetComplaintTypePair, String, String> {
    @Override
    public Reducer<String, String> newReducer(StreetComplaintTypePair streetComplaintTypePair) {
        return new Reducer<>() {
            @Override
            public void reduce(String s) {
            }

            @Override
            public String finalizeReduce() {
                return streetComplaintTypePair.getClaimType();
            }
        };
    }
}
