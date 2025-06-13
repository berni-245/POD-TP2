package ar.edu.itba.pod.reducer;

import ar.edu.itba.pod.common.StreetClaimTypePair;
import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;

@SuppressWarnings("deprecation")
public class StreetComplaintTypeReducerFactory implements ReducerFactory<StreetClaimTypePair, String, String> {
    @Override
    public Reducer<String, String> newReducer(StreetClaimTypePair streetClaimTypePair) {
        return new Reducer<>() {
            @Override
            public void reduce(String s) {
            }

            @Override
            public String finalizeReduce() {
                return streetClaimTypePair.getClaimType();
            }
        };
    }
}
