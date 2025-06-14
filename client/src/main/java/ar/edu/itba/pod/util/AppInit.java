package ar.edu.itba.pod.util;

import ar.edu.itba.pod.model.Complaint;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import java.util.function.Consumer;

public class AppInit {
    public final static String groupCode = "g3";
    private final HazelcastInstance hazelcastInstance;
    private final String complainsPath;
    private final String typesPath;
    private final City cityFormat;
    private final String city;
    private final String outDirectory;

    public AppInit() {
        String addressesRawString = System.getProperty("addresses");
        String cityProp = System.getProperty("city");
        final String inPath = System.getProperty("inPath");
        outDirectory = System.getProperty("outPath");

        if (addressesRawString == null || cityProp == null || inPath == null || outDirectory == null) {
            throw new IllegalArgumentException("Missing arguments%n");
        }
        city = cityProp.toUpperCase();

        // Group Config
        GroupConfig groupConfig = new GroupConfig().setName(groupCode).setPassword(groupCode + "-pass");

        // Client Network Config
        ClientNetworkConfig clientNetworkConfig = new ClientNetworkConfig();
        addressesRawString = addressesRawString.replace("'", "");
        for (String address : addressesRawString.split(";"))
            clientNetworkConfig.addAddress(address);

        // Client Config
        ClientConfig clientConfig = new ClientConfig().setGroupConfig(groupConfig).setNetworkConfig(clientNetworkConfig);

        // Node Client
        hazelcastInstance = HazelcastClient.newHazelcastClient(clientConfig);

        // Path files
        complainsPath = inPath + "/serviceRequests" + city + ".csv";
        typesPath = inPath + "/serviceTypes" + city + ".csv";

        cityFormat = CsvComplaintParser.getCityFormat(complainsPath);
    }

    public HazelcastInstance getHazelcastInstance() {
        return hazelcastInstance;
    }

    public void parseCsv(Consumer<Complaint> eachAddToKeyValueSource, IMap<String, String> types) {
        CsvComplaintParser.parseCsv(complainsPath, typesPath, cityFormat, eachAddToKeyValueSource, types);
    }

    public String getCity() {
        return city;
    }

    public String getOutDirectory() {
        return outDirectory;
    }
}
