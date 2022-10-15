package uk.ac.ed.inf;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

public class RestaurantTest {

    private final static URL url;

    static {
        try {
            url = new URL("https://ilp-rest.azurewebsites.net");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private final static Restaurant[] restaurants = Restaurant.getRestaurantsFromRestServer(url);

    @Test
    public void testGetRestaurants() {
        assertEquals("Name must be matched", "Civerinos Slice", restaurants[0].getRestaurantName());
        assertEquals("Name must be matched", "Sora Lella Vegan Restaurant", restaurants[1].getRestaurantName());
        assertEquals("Name must be matched", "Domino's Pizza - Edinburgh - Southside", restaurants[2].getRestaurantName());
        assertEquals("Name must be matched", "Sodeberg Pavillion", restaurants[3].getRestaurantName());
    }

    @Test
    public void testGetMenu() {
        Menu menu0 = new Menu();
        menu0.setName("Margarita");
        menu0.setPriceInPence(1000);

        Menu menu1 = new Menu();
        menu1.setName("Calzone");
        menu1.setPriceInPence(1400);

        assertEquals("Menu Name must be matched",  menu0.getName(), restaurants[0].getMenu()[0].getName());
        assertEquals("Menu Price must be matched",  menu0.getPriceInPence(), restaurants[0].getMenu()[0].getPriceInPence());

        assertEquals("Menu Name must be matched",  menu1.getName(), restaurants[0].getMenu()[1].getName());
        assertEquals("Menu Price must be matched",  menu1.getPriceInPence(), restaurants[0].getMenu()[1].getPriceInPence());
    }
}
