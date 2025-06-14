package ar.edu.itba.pod.util;

import java.util.List;

public class AgencyMonthlyData implements Comparable<AgencyMonthlyData> {
    private final String agency;
    private final List<MonthCount> monthCounts;

    public AgencyMonthlyData(String agency, List<MonthCount> monthCounts) {
        this.agency = agency;
        this.monthCounts = monthCounts;
    }

    public String getAgency() {
        return agency;
    }

    public List<MonthCount> getMonthCounts() {
        return monthCounts;
    }

    @Override
    public int compareTo(AgencyMonthlyData other) {
        return this.agency.compareTo(other.getAgency());
    }
}
