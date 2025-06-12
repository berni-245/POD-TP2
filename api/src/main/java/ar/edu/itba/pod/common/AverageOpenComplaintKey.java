package ar.edu.itba.pod.common;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;

public class AverageOpenComplaintKey implements DataSerializable {
    private String type;
    private int year;
    private int month;

    public AverageOpenComplaintKey(String type, int year, int month) {
        this.type = type;
        this.year = year;
        this.month = month;
    }

    public String getType() { return type; }
    public int getYear() { return year; }
    public int getMonth() { return month; }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(type);
        out.writeInt(year);
        out.writeInt(month);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        type = in.readUTF();
        year = in.readInt();
        month = in.readInt();
    }
}
