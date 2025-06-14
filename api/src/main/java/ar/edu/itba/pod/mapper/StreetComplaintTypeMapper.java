package ar.edu.itba.pod.mapper;

import ar.edu.itba.pod.common.StreetComplaintTypePair;
import ar.edu.itba.pod.model.Complaint;
import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;

@SuppressWarnings("deprecation")
public class StreetComplaintTypeMapper implements Mapper<String, Complaint, StreetComplaintTypePair, String> {
    @Override
    public void map(String neighborhood, Complaint complaint, Context<StreetComplaintTypePair, String> context) {
        context.emit(
                new StreetComplaintTypePair(complaint.getStreet(), complaint.getComplaintType()),
                complaint.getComplaintType()
        );
    }
}
