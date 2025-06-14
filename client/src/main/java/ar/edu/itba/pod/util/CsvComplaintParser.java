package ar.edu.itba.pod.util;

import ar.edu.itba.pod.model.Complaint;
import ar.edu.itba.pod.model.ComplaintChicago;
import ar.edu.itba.pod.model.ComplaintNYC;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.hazelcast.core.IMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsvComplaintParser {

    private static final Logger logger = LoggerFactory.getLogger(CsvComplaintParser.class);
    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static City getCityFormat(String filePath) {
        boolean isNYCFormat;
        try (Stream<String> lines = Files.lines(Path.of(filePath), StandardCharsets.UTF_8)) {
            isNYCFormat = Arrays.stream(lines.findFirst().orElseThrow().split(";")).findFirst().orElseThrow().equals("Unique Key");
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + filePath, e);
        }
        if (isNYCFormat)
            return City.NYC;
        return City.CHI;
    }

    public static void parseCsv(String complaintsPath, String typesPath, City city, Consumer<Complaint> eachAddToKeyValueSource, IMap<String, String> types) {
        switch (city) {
            case CHI -> loadChicagoServiceTypes(typesPath, types);
            case NYC -> loadNewYorkServiceTypes(typesPath, types);
        }

        try (Stream<String> lines = Files.lines(Path.of(complaintsPath), StandardCharsets.UTF_8)) {
            lines
                    .skip(1)   // (Skip header)
                    .map(line -> line.split(";"))
                    .map(fields -> {
                        try {
                            return switch (city) {
                                case NYC -> parseNewYorkCsv(fields);
                                case CHI -> parseChicagoCsv(fields, types);
                            };
                        } catch (ParseException e) {
                            throw new RuntimeException("Error parsing: " + complaintsPath, e);
                        }
                    })
                    .forEach(elem -> {
                        if (types.containsValue(elem.getComplaintType())) // only add it if it belongs to csv types
                            eachAddToKeyValueSource.accept(elem);
                    });

        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + complaintsPath, e);
        }
    }

    private static ComplaintNYC parseNewYorkCsv(String[] fields) throws ParseException {
        long complaintId = Long.parseLong(fields[0]);
        LocalDateTime createdDate = LocalDateTime.parse(fields[1], dateFormat);        String agencyName = fields[2];
        String complaintType = fields[3];
        String incidentAddress = fields[4];
        String status = fields[5];
        String borough = fields[6];
        double latitude = Double.parseDouble(fields[7]);
        double longitude = Double.parseDouble(fields[8]);

        return new ComplaintNYC(complaintId, createdDate, agencyName, complaintType,
                                    incidentAddress, status ,borough, latitude, longitude);
    }

    private static ComplaintChicago parseChicagoCsv(String[] fields, IMap<String, String> types) throws ParseException {
        String srNumber = fields[0];
        String srShortCode = fields[1];
        String ownerDepartment = fields[2];
        String status = fields[3];
        LocalDateTime creationDate = LocalDateTime.parse(fields[4], dateFormat);
        int streetNumber = Integer.parseInt(fields[5]);
        String streetDirection = fields[6];
        String streetName = fields[7];
        String streetType = fields[8];
        String communityArea = fields[9];
        double latitude = Double.parseDouble(fields[10]);
        double longitude = Double.parseDouble(fields[11]);

        return new ComplaintChicago(srNumber, srShortCode, types.get(srShortCode), ownerDepartment, status,
                                        creationDate, streetNumber, streetDirection, streetName,
                                        streetType, communityArea, latitude, longitude);
    }

    private static void loadNewYorkServiceTypes(String csvPath, IMap<String, String> serviceTypeList) {
        try (Stream<String> lines = Files.lines(Path.of(csvPath), StandardCharsets.UTF_8)) {
            lines
                    .skip(1)
                    .map(line -> line.split(";"))
                    .forEach(fields -> {
                        String serviceType = fields[0].trim();
                        serviceTypeList.put(serviceType, serviceType);
                    });
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Chicago service types", e);
        }
    }

    private static void loadChicagoServiceTypes(String csvPath, IMap<String, String> serviceTypeMap) {
        try (Stream<String> lines = Files.lines(Path.of(csvPath), StandardCharsets.UTF_8)) {
            lines
                    .skip(1)
                    .map(line -> line.split(";"))
                    .forEach(fields -> {
                        String shortCode = fields[0].trim();
                        String serviceType = fields[1].trim();
                        serviceTypeMap.put(shortCode, serviceType);
                    });
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Chicago service types", e);
        }
    }
}
