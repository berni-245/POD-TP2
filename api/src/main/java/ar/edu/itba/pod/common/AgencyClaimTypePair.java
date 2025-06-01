package ar.edu.itba.pod.common;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;
import java.util.Objects;

public class AgencyClaimTypePair implements DataSerializable {
    private String agency;
    private String claimType;

    public AgencyClaimTypePair(String agency, String claimType) {
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
    public boolean equals(Object other) {
        return other instanceof AgencyClaimTypePair agencyClaimTypePair &&
                agency.equals(agencyClaimTypePair.agency) &&
                claimType.equals(agencyClaimTypePair.claimType);
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
