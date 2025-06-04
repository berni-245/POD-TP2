package ar.edu.itba.pod.model;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

import java.io.IOException;
import java.util.Date;

public class ComplaintNYC extends Complaint {
    private long uniqueKey;
    private String complaintType;
    private String incidentAddress;

    public ComplaintNYC(long uniqueKey, Date createdDate, String agency, String complaintType, String incidentAddress,
                        String status, String borough, double latitude, double longitude) {
        super(agency, status, createdDate, borough, latitude, longitude);
        this.uniqueKey = uniqueKey;
        this.complaintType = complaintType;
        this.incidentAddress = incidentAddress;
    }

    @Override
    public String getId() { return String.valueOf(uniqueKey); }

    @Override
    public String getAddress() { return incidentAddress; }

    public String getComplaintType() { return complaintType; }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        super.writeData(out);
        out.writeLong(uniqueKey);
        out.writeUTF(complaintType);
        out.writeUTF(incidentAddress);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        super.readData(in);
        uniqueKey = in.readLong();
        complaintType = in.readUTF();
        incidentAddress = in.readUTF();
    }
}
