package ar.edu.itba.pod.model;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;
import java.util.Date;

public abstract class Complaint implements DataSerializable {
    private String agency;
    private String status;
    private Date createdDate;
    private String neighborhood;
    private double latitude;
    private double longitude;
    private String complaintType;

    public Complaint(String agency, String status, Date createdDate, String neighborhood, double latitude, double longitude, String complaintType) {
        this.agency = agency;
        this.status = status;
        this.createdDate = createdDate;
        this.neighborhood = neighborhood;
        this.latitude = latitude;
        this.longitude = longitude;
        this.complaintType = complaintType;
    }

    public abstract String getId();
    public abstract String getAddress();

    public String getComplaintType() { return complaintType; }
    public String getAgency() { return agency; }
    public String getStatus() { return status; }
    public Date getCreatedDate() { return createdDate; }
    public String getNeighborhood() { return neighborhood; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(agency);
        out.writeUTF(status);
        out.writeObject(createdDate);
        out.writeUTF(neighborhood);
        out.writeDouble(latitude);
        out.writeDouble(longitude);
        out.writeUTF(complaintType);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        agency = in.readUTF();
        status = in.readUTF();
        createdDate = in.readObject();
        neighborhood = in.readUTF();
        latitude = in.readDouble();
        longitude = in.readDouble();
        complaintType = in.readUTF();
    }
}
