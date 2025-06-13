package ar.edu.itba.pod.common;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;
import java.util.Objects;

public class StreetClaimTypePair implements DataSerializable {
    private String street;
    private String claimType;

    public StreetClaimTypePair() {
        // empty for hazelcast
    }

    public StreetClaimTypePair(String street, String claimType){
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
        return other instanceof StreetClaimTypePair streetClaimTypePair &&
                street.equals(streetClaimTypePair.street) &&
                claimType.equals(streetClaimTypePair.claimType);
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
