package ar.edu.itba.pod.key_predicate;

import com.hazelcast.mapreduce.KeyPredicate;

@SuppressWarnings("deprecation")
public class FilterForNeighborhoodKeyPred implements KeyPredicate<String> {
    private final String neighborhood;

    public FilterForNeighborhoodKeyPred(String neighborhood) {
        this.neighborhood = neighborhood;
    }
    @Override
    public boolean evaluate(String s) {
        return s.equals(neighborhood);
    }
}
