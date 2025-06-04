package ar.edu.itba.pod.util;

import com.hazelcast.core.IMap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class CsvServiceTypeLoader {

    public static void loadChicagoServiceTypes(IMap<String, String> serviceTypeMap, String csvPath) {
        try (Stream<String> lines = Files.lines(Path.of(csvPath), StandardCharsets.UTF_8)) {
            lines
                    .skip(1) // (Skip header)
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
