package ar.edu.itba.pod.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class WriteTimesCsv {
    public static void write(Path csvPath, TimeInterval parseTimeInterval, TimeInterval... mapReduceTimeIntervals) {
        List<String> lines = new ArrayList<>();
        StringBuilder header = new StringBuilder();
        StringBuilder data = new StringBuilder();

        header.append("parseTimeStart;parseTimeEnd;parseTimeDuration(ms)");
        data.append("%s;%s;%d".formatted(
                    instantToDateTimeFormat(parseTimeInterval.getStart()),
                    instantToDateTimeFormat(parseTimeInterval.getEnd()),
                    parseTimeInterval.getDuration().toMillis()
                )
        );
        for (int i = 0; i < mapReduceTimeIntervals.length; i++) {
            int jobNum = i + 1;
            header.append(";mapReduceJob%dTimeStart;mapReduceJob%dTimeEnd;mapReduceJob%dTimeDuration(ms)".formatted(jobNum, jobNum, jobNum));
            data.append(";%s;%s;%d".formatted(
                            instantToDateTimeFormat(mapReduceTimeIntervals[i].getStart()),
                            instantToDateTimeFormat(mapReduceTimeIntervals[i].getEnd()),
                            mapReduceTimeIntervals[i].getDuration().toMillis()
                    )
            );
        }

        lines.add(header.toString());
        lines.add(data.toString());

        try {
            Files.write(csvPath, lines);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private static String instantToDateTimeFormat(Instant instant) {
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, zone);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss:SSSS");
        return dateTime.format(formatter);
    }
}
