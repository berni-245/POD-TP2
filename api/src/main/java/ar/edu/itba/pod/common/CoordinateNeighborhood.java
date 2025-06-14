package ar.edu.itba.pod.common;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;
import java.util.Objects;

public class CoordinateNeighborhood implements DataSerializable {
    private String neighborhood;
    private int xCoordinate;
    private int yCoordinate;

    public CoordinateNeighborhood() {
        // Empty for hazelcast
    }

    public CoordinateNeighborhood(String neighborhood, double latitude, double longitude, double quadrantSize) {
        this.neighborhood = neighborhood;
        this.xCoordinate = (int) (latitude/quadrantSize);
        this.yCoordinate = (int) (longitude/quadrantSize);
    }


    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(neighborhood);
        out.writeInt(xCoordinate);
        out.writeInt(yCoordinate);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        neighborhood = in.readUTF();
        xCoordinate = in.readInt();
        yCoordinate = in.readInt();
    }

    @Override
    public String toString() {
        return "%s %dº %dº".formatted(neighborhood, xCoordinate, yCoordinate);
    }

    @Override
    public int hashCode(){
        return Objects.hash(neighborhood, xCoordinate, yCoordinate);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CoordinateNeighborhood coordinateNeighborhood &&
                neighborhood.equals(coordinateNeighborhood.neighborhood) &&
                xCoordinate == coordinateNeighborhood.xCoordinate &&
                yCoordinate == coordinateNeighborhood.yCoordinate;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public int getXCoordinate() {
        return xCoordinate;
    }

    public int getYCoordinate() {
        return yCoordinate;
    }
}
