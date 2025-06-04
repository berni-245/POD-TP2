package ar.edu.itba.pod.model;

import java.util.Date;

public class ComplaintChicago extends Complaint {
    private String srNumber;
    private String srShortCode;
    private int streetNumber;
    private String streetDirection;
    private String streetName;
    private String streetType;
    private String communityArea;

    public ComplaintChicago(String srNumber, String srShortCode, String agency, String status, Date createdDate,
                            int streetNumber, String streetDirection, String streetName, String streetType,
                            String communityArea, double latitude, double longitude) {
        super(agency, status, createdDate, communityArea, latitude, longitude);
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
    public String getAddress() {
        return streetNumber + " " + streetDirection + " " + streetName + " " + streetType;
    }

    public String getSrShortCode() { return srShortCode; }
    public int getStreetNumber() { return streetNumber; }
    public String getStreetDirection() { return streetDirection; }
    public String getStreetName() { return streetName; }
    public String getStreetType() { return streetType; }
    public String getCommunityArea() { return communityArea; }
}
