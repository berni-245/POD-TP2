package ar.edu.itba.pod.collator;

import com.hazelcast.mapreduce.Collator;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@SuppressWarnings("deprecation")
public class CountComplaintTypePercentageCollator implements Collator<Map.Entry<String, Integer>, Map<String, String>> {
    private final int totalTypesCount;

    public CountComplaintTypePercentageCollator(int totalTypesCount) {
        this.totalTypesCount = totalTypesCount;
    }

    @Override
    public Map<String, String> collate(Iterable<Map.Entry<String, Integer>> values) {
        DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance(Locale.US); // needed or otherwise it will use the decimal separator of the computer
        df.applyPattern("#.##");
        df.setRoundingMode(RoundingMode.DOWN);

        return StreamSupport.stream(values.spliterator(), false)
                .map(entry -> {
                    float raw = ((float) entry.getValue() / totalTypesCount) * 100;
                    return Map.entry(entry.getKey(), df.format(raw) + "%");
                })
                .sorted(Map.Entry.<String, String>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

}
