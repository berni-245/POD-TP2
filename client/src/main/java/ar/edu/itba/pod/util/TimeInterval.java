package ar.edu.itba.pod.util;

import java.time.Duration;
import java.time.Instant;

public class TimeInterval {
    private final Instant start;
    private final Instant end;

    public TimeInterval(Instant start, Instant end) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start must be before end");
        }
        this.start = start;
        this.end = end;
    }

    public Duration getDuration() {
        return Duration.between(start, end);
    }

    public Instant getStart() {
        return start;
    }

    public Instant getEnd() {
        return end;
    }
}

