package ar.edu.itba.pod.mapper;

import ar.edu.itba.pod.common.StreetClaimTypePair;
import ar.edu.itba.pod.model.Complaint;
import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;

@SuppressWarnings("deprecation")
public class StreetComplaintTypeMapper implements Mapper<String, Complaint, StreetClaimTypePair, String> {
    @Override
    public void map(String neighborhood, Complaint complaint, Context<StreetClaimTypePair, String> context) {
        context.emit(
                new StreetClaimTypePair(complaint.getStreet(), complaint.getComplaintType()),
                complaint.getComplaintType()
        );
    }
}
