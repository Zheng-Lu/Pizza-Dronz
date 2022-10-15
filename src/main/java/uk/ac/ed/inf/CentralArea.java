package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CentralArea {
    private static CentralArea Instance;
    private CentralArea(){}

    public static CentralArea getInstance() {
        if (Instance == null){ //if there is no instance available... create new one
            Instance = new CentralArea();
        }

        return Instance;
    }

    @JsonProperty("name")
    private String name;
    @JsonProperty("longitude")
    private Double longitude;
    @JsonProperty("latitude")
    private Double latitude;

    public String getRestaurantName() {
        return name;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getLatitude() {
        return latitude;
    }


    /**
     * A helper function to retrieve the longitudes and latitudes of central area from REST services
     * @return a list that consists of two lists
     * the first list is the list of longitudes
     * the second list is the list of latitudes
     * @throws IOException If the given url is invalid or expired.
     */
    public static List<List<Double>> getCentralAreaLngLats() {
        ObjectMapper mapper = new ObjectMapper();
        List<Double> Lngs = new ArrayList<>();
        List<Double> Lats = new ArrayList<>();
        List<List<Double>> res = new ArrayList<>();

        try {
            CentralArea[] centralLngLats = mapper.readValue(
                    new URL("https://ilp-rest.azurewebsites.net/centralArea"),
                    CentralArea[].class
            );

            for (CentralArea centralLngLat : centralLngLats) {
                Lngs.add(centralLngLat.getLongitude());
                Lats.add(centralLngLat.getLatitude());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        res.add(Lngs);
        res.add(Lats);
        return res;
    }
}
