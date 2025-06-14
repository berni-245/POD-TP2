package ar.edu.itba.pod.collator;

import ar.edu.itba.pod.common.AgencyMonthKey;
import ar.edu.itba.pod.util.MonthCount;
import ar.edu.itba.pod.util.AgencyMonthlyData;
import com.hazelcast.mapreduce.Collator;

import java.util.*;

@SuppressWarnings("deprecation")
public class AverageOpenComplaintsCollator implements Collator<Map.Entry<AgencyMonthKey, Integer>, List<String>> {
    private final int window;

    public AverageOpenComplaintsCollator(int window) {
        this.window = window;
    }

    @Override
    public List<String> collate(Iterable<Map.Entry<AgencyMonthKey, Integer>> values) {
        List<String> result = new ArrayList<>();

        // Mapa temporal para agrupar las agencias con los datos mensuales y luego trabajo con listas que ordenadas
        Map<String, List<MonthCount>> tempAgencyDataMap = new HashMap<>();
        List<AgencyMonthlyData> agencyDataList = new ArrayList<>();

        for (Map.Entry<AgencyMonthKey, Integer> entry : values) {
            AgencyMonthKey key = entry.getKey();
            int count = entry.getValue();

            tempAgencyDataMap.computeIfAbsent(key.getAgency(), k -> new ArrayList<>())
                    .add(new MonthCount(key.getYear(), key.getMonth(), count));
        }

        for (Map.Entry<String, List<MonthCount>> entry : tempAgencyDataMap.entrySet()) {
            agencyDataList.add(new AgencyMonthlyData(entry.getKey(), entry.getValue()));
        }

        Collections.sort(agencyDataList);

        for (AgencyMonthlyData monthlyData : agencyDataList) {
            String actualAgency = monthlyData.getAgency();
            List<MonthCount> monthCounts = monthlyData.getMonthCounts();
            Collections.sort(monthCounts);

            for (int monthCountIndex = 0; monthCountIndex < monthCounts.size(); monthCountIndex++) {
                MonthCount actualMonthCount = monthCounts.get(monthCountIndex);
                int actualYear = actualMonthCount.getYear();
                int actualMonth = actualMonthCount.getMonth();
                int month = actualMonth;
                int sum = actualMonthCount.getCount();
                int div = 1;

                for (int indexOffset = monthCountIndex - 1; indexOffset >= 0 && month > 1 && actualMonth - month < window; indexOffset--, div++, month--) {
                    MonthCount previousMonthCount = monthCounts.get(indexOffset);
                    if (actualMonthCount.getYear() == previousMonthCount.getYear() && previousMonthCount.getMonth() + window > actualMonth) {
                        sum += monthCounts.get(indexOffset).getCount();
                    }
                }

                result.add(String.format("%s;%d;%02d;%d.%d", actualAgency, actualYear, actualMonth, sum / div, ((sum * 100) / div) % 100));
                // Por los siguientes meses que no aparezcan pero su promedio es distinto de 0 por el aporte que este mes genera
                while (actualMonth < 12 && actualMonth + 1 - actualMonthCount.getMonth() < window && (monthCountIndex + 1 >= monthCounts.size() || monthCounts.get(monthCountIndex + 1).getMonth() != actualMonth + 1)) {
                    actualMonth++;
                    month = actualMonth;
                    sum = actualMonthCount.getCount();
                    div = div < window ? div + 1 : div;

                    for (int indexOffset = monthCountIndex - 1; indexOffset >= 0 && month > 1 && actualMonth - month < window; indexOffset--, month--) {
                        MonthCount previousMonthCount = monthCounts.get(indexOffset);
                        if (actualMonthCount.getYear() == previousMonthCount.getYear() && previousMonthCount.getMonth() + window > actualMonth) {
                            sum += monthCounts.get(indexOffset).getCount();
                        }
                    }

                    result.add(String.format("%s;%d;%02d;%d.%d", actualAgency, actualYear, actualMonth, sum / div, ((sum * 100) / div) % 100));
                }
            }
        }

        return result;
    }
}

