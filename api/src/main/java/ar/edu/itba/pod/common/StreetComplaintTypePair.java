package ar.edu.itba.pod.common;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;
import java.util.Objects;

public class StreetComplaintTypePair implements DataSerializable {
    private String street;
    private String claimType;

    public StreetComplaintTypePair() {
        // empty for hazelcast
    }

    public StreetComplaintTypePair(String street, String claimType){
        this.street = street;
        this.claimType = claimType;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(street);
        out.writeUTF(claimType);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        street = in.readUTF();
        claimType = in.readUTF();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof StreetComplaintTypePair streetComplaintTypePair &&
                street.equals(streetComplaintTypePair.street) &&
                claimType.equals(streetComplaintTypePair.claimType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(street, claimType);
    }

    public String getStreet() {
        return street;
    }

    public String getClaimType() {
        return claimType;
    }
}
