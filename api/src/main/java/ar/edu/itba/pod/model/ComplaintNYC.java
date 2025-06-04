package ar.edu.itba.pod.model;

public class ComplaintNYC extends Complaint {
    private long uniqueKey;
    private String complaintType;
    private String incidentAddress;

    public ComplaintNYC(long uniqueKey, String createdDate, String agency, String complaintType, String incidentAddress,
                        String status, String borough, double latitude, double longitude) {
        super(agency, status, createdDate, borough, latitude, longitude);
        this.uniqueKey = uniqueKey;
        this.complaintType = complaintType;
        this.incidentAddress = incidentAddress;
    }

    @Override
    public String getId() {
        return String.valueOf(uniqueKey);
    }

    @Override
    public String getAddress() {
        return incidentAddress;
    }

    public String getComplaintType() {
        return complaintType;
    }
}
