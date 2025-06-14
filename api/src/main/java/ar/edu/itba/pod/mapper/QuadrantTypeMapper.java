package ar.edu.itba.pod.mapper;

import ar.edu.itba.pod.common.CoordinateNeighborhood;
import ar.edu.itba.pod.model.Complaint;
import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;

@SuppressWarnings("deprecation")
public class QuadrantTypeMapper implements Mapper<CoordinateNeighborhood, Complaint, CoordinateNeighborhood, String> {
    @Override
    public void map(CoordinateNeighborhood coordinateNeighborhood, Complaint complaint, Context<CoordinateNeighborhood, String> context) {
        context.emit(coordinateNeighborhood, complaint.getComplaintType());
    }
}
