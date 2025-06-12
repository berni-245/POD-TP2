package ar.edu.itba.pod.common;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;

public class MonthCount implements DataSerializable {
    public int year;
    public int month;
    public int count;

    public MonthCount(int year, int month, int count) {
        this.year = year;
        this.month = month;
        this.count = count;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeInt(year);
        out.writeInt(month);
        out.writeInt(count);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        int year = in.readInt();
        int month = in.readInt();
        int count = in.readInt();
    }
}
