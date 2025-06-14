package ar.edu.itba.pod.common;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;
import java.util.Objects;

public class AgencyMonthKey implements DataSerializable {
    private String agency;
    private int year;
    private int month;

    public AgencyMonthKey() {
        // Empty for hazelcast
    }

    public AgencyMonthKey(String agency, int year, int month) {
        this.agency = agency;
        this.year = year;
        this.month = month;
    }

    public String getAgency() { return agency; }
    public int getYear() { return year; }
    public int getMonth() { return month; }

    @Override
    public boolean equals(Object other) {
        return other instanceof AgencyMonthKey agencyMonthKey &&
                agency.equals(agencyMonthKey.getAgency()) &&
                year == agencyMonthKey.getYear() &&
                month == agencyMonthKey.getMonth();
    }

    @Override
    public int hashCode() {
        return Objects.hash(agency, year, month);
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(agency);
        out.writeInt(year);
        out.writeInt(month);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        agency = in.readUTF();
        year = in.readInt();
        month = in.readInt();
    }
}
