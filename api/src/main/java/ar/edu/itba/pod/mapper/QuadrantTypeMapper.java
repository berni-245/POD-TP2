package ar.edu.itba.pod.mapper;

import ar.edu.itba.pod.common.CoordinateNeighborhood;
import ar.edu.itba.pod.model.Complaint;
import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;

public class QuadrantTypeMapper implements Mapper<CoordinateNeighborhood, Complaint, CoordinateNeighborhood, String> {
    @Override
    public void map(CoordinateNeighborhood coordinateNeighborhood, Complaint complaint, Context<CoordinateNeighborhood, String> context) {
        String type = complaint.getComplaintType();
        if(type != null) {
            context.emit(coordinateNeighborhood, type);
        }
    }
}
