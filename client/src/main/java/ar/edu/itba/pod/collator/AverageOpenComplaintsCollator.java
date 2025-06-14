package ar.edu.itba.pod.collator;

import ar.edu.itba.pod.common.AgencyMonthKey;
import ar.edu.itba.pod.util.MonthCount;
import com.hazelcast.mapreduce.Collator;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public class AverageOpenComplaintsCollator implements Collator<Map.Entry<AgencyMonthKey, Integer>, List<String>> {

    private final int window;

    public AverageOpenComplaintsCollator(int window) {
        this.window = window;
    }

    @Override
    public List<String> collate(Iterable<Map.Entry<AgencyMonthKey, Integer>> values) {
        Map<String, List<MonthCount>> groupedByAgency = new HashMap<>();
        List<List<String>> result = new ArrayList<>();

        for (Map.Entry<AgencyMonthKey, Integer> entry : values) {
            AgencyMonthKey key = entry.getKey();
            int count = entry.getValue();

            groupedByAgency.computeIfAbsent(key.getAgency(), k -> new ArrayList<>())
                    .add(new MonthCount(key.getYear(), key.getMonth(), count));
        }

        List<String> agencies = new ArrayList<>(groupedByAgency.keySet());
        Collections.sort(agencies);

        for (String agency : agencies) {
            List<MonthCount> monthCounts = groupedByAgency.get(agency);
            monthCounts.sort(Comparator.comparingInt(mc -> mc.getYear() * 100 + mc.getMonth()));

            List<String> agencyResult = new ArrayList<>();

            // TODO: FIXEAR
            for (int i = window - 1; i < monthCounts.size(); i++) {
                int sum = 0;
                for (int j = i - window + 1; j <= i; j++) {
                    sum += monthCounts.get(j).getCount();
                }
                int avg = sum / window;
                MonthCount current = monthCounts.get(i);
                agencyResult.add(String.format("%s;%d;%02d;%d", agency, current.getYear(), current.getMonth(), avg));
            }

            result.add(agencyResult);
        }

        List<String> flatList = result.stream().flatMap(List::stream).collect(Collectors.toList());

        return flatList;
    }
}

