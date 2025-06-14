package ar.edu.itba.pod.util;

import ar.edu.itba.pod.common.AgencyMonthKey;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;
import java.util.Objects;

public class MonthCount implements DataSerializable, Comparable<MonthCount> {
    private int year;
    private int month;
    private int count;

    public MonthCount() {
        // Hazelcast lo quiere asi! :)
    }

    public MonthCount(int year, int month, int count) {
        this.year = year;
        this.month = month;
        this.count = count;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getCount() {
        return count;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeInt(year);
        out.writeInt(month);
        out.writeInt(count);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        year = in.readInt();
        month = in.readInt();
        count = in.readInt();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof MonthCount monthCount &&
                count == monthCount.getCount() &&
                year == monthCount.getYear() &&
                month == monthCount.getMonth();
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, month, count);
    }

    @Override
    public int compareTo(MonthCount other) {
        if (this.year != other.year) {
            return Integer.compare(this.year, other.year);
        }
        return Integer.compare(this.month, other.month);
    }

    @Override
    public String toString() {
        return String.format("%04d-%02d: %d", year, month, count);
    }
}
