package ar.edu.itba.pod.common;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;
import java.util.Objects;

public class AgencyComplaintTypePair implements DataSerializable, Comparable<AgencyComplaintTypePair> {
    private String agency;
    private String claimType;

    public AgencyComplaintTypePair() {
        // empty for hazelcast
    }

    public AgencyComplaintTypePair(String agency, String claimType) {
        this.agency = agency;
        this.claimType = claimType;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(agency);
        out.writeUTF(claimType);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        agency = in.readUTF();
        claimType = in.readUTF();
    }

    @Override
    public int compareTo(AgencyComplaintTypePair o) {
        int cmp = claimType.compareTo(o.claimType);
        if (cmp == 0) {
            cmp = agency.compareTo(o.agency);
        }
        return cmp;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof AgencyComplaintTypePair agencyComplaintTypePair &&
                agency.equals(agencyComplaintTypePair.agency) &&
                claimType.equals(agencyComplaintTypePair.claimType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(agency, claimType);
    }

    public String getAgency() {
        return agency;
    }

    public String getClaimType() {
        return claimType;
    }
}
