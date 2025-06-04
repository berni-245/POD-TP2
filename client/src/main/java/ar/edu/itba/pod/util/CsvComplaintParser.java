package ar.edu.itba.pod.util;

import ar.edu.itba.pod.model.Complaint;
import ar.edu.itba.pod.model.ComplaintChicago;
import ar.edu.itba.pod.model.ComplaintNYC;
import com.hazelcast.core.ICollection;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsvComplaintParser {

    private static final Logger logger = LoggerFactory.getLogger(CsvComplaintParser.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public static void parse(String filePath, City city, ICollection<Complaint> outputList) {
        try {
            List<String> lines = Files.readAllLines(Path.of(filePath), StandardCharsets.UTF_8);

            for (int i = 1; i < lines.size(); i++) {    // (Skip header)
                String line = lines.get(i);
                String[] fields = line.split(";");

                Complaint complaint = switch (city) {
                    case NYC -> parseNYCComplaint(fields);
                    case CHI -> parseChicagoComplaint(fields);
                };

                outputList.add(complaint);
            }

        } catch (IOException | ParseException e) {
            throw new RuntimeException("Error reading file: " + filePath, e);
        }
    }

    private static ComplaintNYC parseNYCComplaint(String[] fields) throws ParseException {
        long complaintId = Long.parseLong(fields[0]);
        Date createdDate = dateFormat.parse(fields[1]);
        String agencyName = fields[2];
        String complaintType = fields[3];
        String incidentAddress = fields[4];
        String status = fields[5];
        String borough = fields[6];
        double latitude = Double.parseDouble(fields[7]);
        double longitude = Double.parseDouble(fields[8]);

        return new ComplaintNYC(complaintId, createdDate, agencyName, complaintType,
                                    incidentAddress, status ,borough, latitude, longitude);
    }

    private static ComplaintChicago parseChicagoComplaint(String[] fields) throws ParseException {
        String srNumber = fields[0];
        String srShortCode = fields[1];
        String ownerDepartment = fields[2];
        String status = fields[3];
        Date creationDate = dateFormat.parse(fields[4]);
        int streetNumber = Integer.parseInt(fields[5]);
        String streetDirection = fields[6];
        String streetName = fields[7];
        String streetType = fields[8];
        String communityArea = fields[9];
        double latitude = Double.parseDouble(fields[10]);
        double longitude = Double.parseDouble(fields[11]);

        return new ComplaintChicago(srNumber, srShortCode, ownerDepartment, status,
                                        creationDate, streetNumber, streetDirection, streetName,
                                        streetType, communityArea, latitude, longitude);
    }
}
