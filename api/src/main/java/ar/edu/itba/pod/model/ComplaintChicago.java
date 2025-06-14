package ar.edu.itba.pod.model;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;

public class ComplaintChicago extends Complaint {
    private String srNumber;
    private String srShortCode;
    private int streetNumber;
    private String streetDirection;
    private String streetName;
    private String streetType;
    private String communityArea;

    public ComplaintChicago() {
        super();
        // empty for hazelcast
    }
    public ComplaintChicago(String srNumber, String srShortCode, String complaintType, String agency, String status, LocalDateTime createdDate,
                            int streetNumber, String streetDirection, String streetName, String streetType,
                            String communityArea, double latitude, double longitude) {
        super(agency, status, createdDate, communityArea, latitude, longitude, complaintType);
        this.srNumber = srNumber;
        this.srShortCode = srShortCode;
        this.streetNumber = streetNumber;
        this.streetDirection = streetDirection;
        this.streetName = streetName;
        this.streetType = streetType;
        this.communityArea = communityArea;
    }

    @Override
    public String getId() {
        return srNumber;
    }

    @Override
    public String getStreet() {
        return streetDirection + " " + streetName + " " + streetType;
    }

    public String getSrShortCode() { return srShortCode; }
    public int getStreetNumber() { return streetNumber; }
    public String getStreetDirection() { return streetDirection; }
    public String getStreetName() { return streetName; }
    public String getStreetType() { return streetType; }
    public String getCommunityArea() { return communityArea; }

    @Override
    public boolean isOpen() {
        return this.getStatus().equals("Open");
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        super.writeData(out);
        out.writeUTF(srNumber);
        out.writeUTF(srShortCode);
        out.writeInt(streetNumber);
        out.writeUTF(streetDirection);
        out.writeUTF(streetName);
        out.writeUTF(streetType);
        out.writeUTF(communityArea);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        super.readData(in);
        srNumber = in.readUTF();
        srShortCode = in.readUTF();
        streetNumber = in.readInt();
        streetDirection = in.readUTF();
        streetName = in.readUTF();
        streetType = in.readUTF();
        communityArea = in.readUTF();
    }
}
