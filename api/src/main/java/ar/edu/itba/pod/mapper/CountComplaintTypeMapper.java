package ar.edu.itba.pod.mapper;

import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;

@SuppressWarnings("deprecation")
public class CountComplaintTypeMapper implements Mapper<String, String, String, Integer> {
    @Override
    public void map(String street, String complaintType, Context<String, Integer> context) {
        context.emit(street, 1);
    }
}
