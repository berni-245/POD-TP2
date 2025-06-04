package ar.edu.itba.pod.model;

import java.util.Date;

public abstract class Complaint {
    protected String agency;
    protected String status;
    protected Date createdDate;
    protected String neighborhood;
    protected double latitude;
    protected double longitude;

    public Complaint(String agency, String status, Date createdDate, String neighborhood, double latitude, double longitude) {
        this.agency = agency;
        this.status = status;
        this.createdDate = createdDate;
        this.neighborhood = neighborhood;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public abstract String getId();
    public abstract String getAddress();

    public String getAgency() { return agency; }
    public String getStatus() { return status; }
    public Date getCreatedDate() { return createdDate; }
    public String getNeighborhood() { return neighborhood; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}
