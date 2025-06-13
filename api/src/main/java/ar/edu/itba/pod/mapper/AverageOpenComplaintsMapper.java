package ar.edu.itba.pod.mapper;

import ar.edu.itba.pod.common.AverageOpenComplaintKey;
import ar.edu.itba.pod.common.MonthCount;
import ar.edu.itba.pod.model.Complaint;
import ar.edu.itba.pod.model.ComplaintChicago;
import ar.edu.itba.pod.model.ComplaintNYC;
import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;

import java.time.LocalDateTime;

@SuppressWarnings("deprecation")
public class AverageOpenComplaintsMapper implements Mapper<String, Complaint, AverageOpenComplaintKey, MonthCount> {
    @Override
    public void map(String key, Complaint complaint, Context<AverageOpenComplaintKey, MonthCount> context) {
        LocalDateTime time = complaint.getCreatedDate();
        int year = time.getYear();
        int month = time.getMonthValue();
        String type;

        if (complaint instanceof ComplaintChicago chi && complaint.getStatus().equals("Open")) {
            type = chi.getSrShortCode();
        } else if (complaint instanceof ComplaintNYC nyc && !complaint.getStatus().equals("Closed")) {
            type = nyc.getComplaintType();
        } else {
            return;
        }

        context.emit(new AverageOpenComplaintKey(type, year, month), new MonthCount(year, month, 1));
    }
}