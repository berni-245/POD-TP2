package ar.edu.itba.pod.mapper;

import ar.edu.itba.pod.common.AgencyComplaintTypePair;
import ar.edu.itba.pod.model.Complaint;
import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;

@SuppressWarnings("deprecation")
public class CountAgencyComplaintTypeMapper implements Mapper<String, Complaint, AgencyComplaintTypePair, Integer> {
    @Override
    public void map(String s, Complaint complaint, Context<AgencyComplaintTypePair, Integer> context) {
        context.emit(new AgencyComplaintTypePair(complaint.getAgency(), complaint.getComplaintType()), 1);
    }
}
