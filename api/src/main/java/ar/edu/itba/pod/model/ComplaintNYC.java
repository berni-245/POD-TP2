package ar.edu.itba.pod.model;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;

public class ComplaintNYC extends Complaint {
    private long uniqueKey;
    private String incidentAddress;

    public ComplaintNYC() {
        super();
        // empty for hazelcast
    }

    public ComplaintNYC(long uniqueKey, LocalDateTime createdDate, String agency, String complaintType, String incidentAddress,
                        String status, String borough, double latitude, double longitude) {
        super(agency, status, createdDate, borough, latitude, longitude, complaintType);
        this.uniqueKey = uniqueKey;
        this.incidentAddress = incidentAddress;
    }

    @Override
    public String getId() { return String.valueOf(uniqueKey); }

    @Override
    public String getStreet() {
        String firstWord = incidentAddress.split(" ")[0];
        if (isInteger(firstWord))
            return incidentAddress.substring(firstWord.length() + 1);
        return incidentAddress;
    }

    @Override
    public boolean isOpen() {
        return !this.getStatus().equals("Closed");
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        super.writeData(out);
        out.writeLong(uniqueKey);
        out.writeUTF(incidentAddress);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        super.readData(in);
        uniqueKey = in.readLong();
        incidentAddress = in.readUTF();
    }

    private boolean isInteger(String word) {
        try {
            Integer.parseInt(word);
        }
        catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}
