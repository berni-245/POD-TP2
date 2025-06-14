package ar.edu.itba.pod.mapper;

import ar.edu.itba.pod.common.AgencyMonthKey;
import ar.edu.itba.pod.model.Complaint;
import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;

import java.time.LocalDateTime;

@SuppressWarnings("deprecation")
public class AverageOpenComplaintsMapper implements Mapper<String, Complaint, AgencyMonthKey, Integer> {
    @Override
    public void map(String key, Complaint complaint, Context<AgencyMonthKey, Integer> context) {
        LocalDateTime time = complaint.getCreatedDate();
        int year = time.getYear();
        int month = time.getMonthValue();
        String agency = complaint.getAgency();

        if (complaint.isOpen()) {
            context.emit(new AgencyMonthKey(agency, year, month), 1);
        }
    }
}