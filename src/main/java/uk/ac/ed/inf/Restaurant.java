package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Restaurant {
    @JsonProperty("name")
    private String name;
    @JsonProperty("longitude")
    private Double longitude;
    @JsonProperty("latitude")
    private Double latitude;
    @JsonProperty("menu")
    private Menu[] menus;

    // Getters
    public String getRestaurantName() {
        return name;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Menu[] getMenu() {
        return menus;
    }

    // Setters
    public void setRestaurantName(String newName) {
        this.name = newName;
    }

    public void setMenus(Menu[] newMenus) {
        this.menus = newMenus;
    }

    /**
     * Method that retrieving restaurant data from the REST service
     * @param serverBaseAddress url of server base address
     * @return an array of Restaurants which are defined (including the menus)
     */
    public static Restaurant[] getRestaurantsFromRestServer(URL serverBaseAddress) {
        ObjectMapper mapper = new ObjectMapper();
        Restaurant[] restaurants = new Restaurant[0];
        String endpoint = "/restaurants";

        try {
            restaurants = mapper.readValue(new URL(serverBaseAddress + endpoint), Restaurant[].class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return restaurants;
    }


    public static Restaurant getRestaurantByItem(Restaurant[] restaurants, String orderItem) {
        for (Restaurant restaurant : restaurants){
            for (Menu menu : restaurant.getMenu()) {
                if (orderItem.equals(menu.getName())) {
                    return restaurant;
                }
            }
        }
        return null;
    }
}
