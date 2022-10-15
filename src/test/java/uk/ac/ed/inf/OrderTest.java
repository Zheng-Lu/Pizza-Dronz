package uk.ac.ed.inf;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OrderTest {
    private final static URL url;

    static {
        try {
            url = new URL("https://ilp-rest.azurewebsites.net/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private final static Restaurant[] restaurants = Restaurant.getRestaurantsFromRestServer(url);

    @Test
    public void testGetDeliveryCost_1() throws Exception {
        String[] orderedPizza = {"Margarita", "Calzone"};
        int expected_cost = 1000 + 1400 + 100;
        assertEquals(expected_cost, Order.getDeliveryCost(restaurants, orderedPizza));
    }

    @Test
    public void testGetDeliveryCost_2() throws Exception {
        String[] orderedPizza = {"Margarita","Margarita", "Calzone", "Calzone"};
        int expected_cost = 1000*2 + 1400*2 + 100;
        assertEquals(expected_cost, Order.getDeliveryCost(restaurants, orderedPizza));
    }

    @Test
    public void testGetDeliveryCost_3() throws Exception {
        String[] orderedPizza = {"Margarita","Margarita", "Calzone"};
        int expected_cost = 1000*2 + 1400 + 100;
        assertEquals(expected_cost, Order.getDeliveryCost(restaurants, orderedPizza));
    }

    @Test
    public void testGetDeliveryCost_4() throws Exception {
        String[] orderedPizza = {"Margarita"};
        int expected_cost = 1000 + 100;
        assertEquals(expected_cost, Order.getDeliveryCost(restaurants, orderedPizza));
    }

    @Test(expected = Exception.class)
    public void testInvalidPizzaCombinationMultipleSuppliersException_1() throws Exception {
        String[] orderedPizza = {"Margarita", "Meat Lover", "Proper Pizza"};
        Order.getDeliveryCost(restaurants, orderedPizza);
    }

    @Test
    public void testInvalidPizzaCombinationMultipleSuppliersException_2() throws Exception {
        String[] orderedPizza = {"Margarita", "Meat Lover", "Proper Pizza"};
        Exception exception = assertThrows(Exception.class, () -> {
            Order.getDeliveryCost(restaurants, orderedPizza);
        });

        String expectedMessage = "InvalidPizzaCombinationMultipleSuppliers";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test(expected = Exception.class)
    public void testInvalidPizzaCountException_1() throws Exception {
        String[] orderedPizza = {"Margarita", "Meat Lover", "Proper Pizza", "Proper Pizza", "Proper Pizza"};
        Order.getDeliveryCost(restaurants, orderedPizza);
    }

    @Test
    public void testInvalidPizzaCountException_2() throws Exception {
        String[] orderedPizza = {"Margarita", "Meat Lover", "Proper Pizza", "Proper Pizza", "Proper Pizza"};
        Exception exception = assertThrows(Exception.class, () -> {
            Order.getDeliveryCost(restaurants, orderedPizza);
        });

        String expectedMessage = "InvalidPizzaCount";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testInvalidPizzaCountException_3() throws Exception {
        String[] orderedPizza = {};
        Exception exception = assertThrows(Exception.class, () -> {
            Order.getDeliveryCost(restaurants, orderedPizza);
        });

        String expectedMessage = "InvalidPizzaCount";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testInvalidPizzaNotDefinedException_1() throws Exception {
        String[] orderedPizza = {"Pineapple Pizza", "Mapo Tofu Pizza"};
        Exception exception = assertThrows(Exception.class, () -> {
            Order.getDeliveryCost(restaurants, orderedPizza);
        });

        String expectedMessage = "InvalidPizzaNotDefined";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testInvalidPizzaNotDefinedException_2() throws Exception {
        String[] orderedPizza = {"Pineapple Pizza"};
        Exception exception = assertThrows(Exception.class, () -> {
            Order.getDeliveryCost(restaurants, orderedPizza);
        });

        String expectedMessage = "InvalidPizzaNotDefined";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void OrderTest() throws Exception {
        Restaurant[] participants = Restaurant.getRestaurantsFromRestServer(new URL("https://ilp-rest.azurewebsites.net/"));
        String[] a = {"Meat Lover", "Vegan Delight"};
        String[] b = {"Meat Lover", "Vegan Delight", "Random stuff"};
        String[] c = {"Random stuff"};
        String[] d = {"Random stuff", "Random stuff", "Random stuff", "Random stuff", "Random stuff",};

        assertEquals(Order.getDeliveryCost(participants, a), 2600);
        Throwable e_b = assertThrows(Exception.class, () -> Order.getDeliveryCost(participants, b));
        assertEquals("InvalidPizzaCombinationMultipleSuppliers", e_b.getMessage());
        Throwable e_c = assertThrows(Exception.class, () -> Order.getDeliveryCost(participants, c));
        assertEquals("InvalidPizzaNotDefined", e_c.getMessage());
        Throwable e_d = assertThrows(Exception.class, () -> Order.getDeliveryCost(participants, d));
        assertEquals("InvalidPizzaCount", e_d.getMessage());
    }

    @Test
    public void orderRightTest() throws Exception {
        Restaurant[] rs =  Restaurant.getRestaurantsFromRestServer(new URL("https://ilp-rest.azurewebsites.net"));
        String[] order1 = {"Pineapple & Ham & Cheese", "Proper Pizza"};
        String[] order2 = {"Margarita", "Calzone"};
        String[] order3 = {"Super Cheese"};
        assertEquals(2400, Order.getDeliveryCost(rs, order1));
        assertEquals(2500, Order.getDeliveryCost(rs, order2));
        assertEquals(1500, Order.getDeliveryCost(rs, order3));
    }

    @Test(expected = Exception.class)
    public void orderExceptionTest() throws Exception{
        Restaurant[] rs =  Restaurant.getRestaurantsFromRestServer(new URL("https://ilp-rest.azurewebsites.net"));
        String[] order1 = {"Meat Lover", "All Shrooms"};
        Order.getDeliveryCost(rs, order1);
    }

    @Test
    public void RestaurantTest() throws MalformedURLException {
        Restaurant[] participants = Restaurant.getRestaurantsFromRestServer(new URL("https://ilp-rest.azurewebsites.net"));
        assertEquals(participants[0].getRestaurantName(), "Civerinos Slice");
    }

    @Test
    public void testPizzaNotInMenu(){
        Restaurant[] participants;
        try {
            participants = Restaurant.getRestaurantsFromRestServer(new URL("https://ilp-rest.azurewebsites.net/"));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        String[] testString = {"Margarita", "This Pizza doesn't exist"};
        Order order = new Order();
        int a = 0;

        try{
            a = order.getDeliveryCost(participants, testString);
            fail("Expected exception");
        }
        catch(Exception e ) {
            assertEquals( "InvalidPizzaCombinationMultipleSuppliers", e.getMessage() ); // Optionally make sure you get the correct message, too
        }

        assertEquals(0, a);
    }

    @Test
    public void testDeliveryCostNormal(){
        // pizza names are all in one restaurant

        // return true
        // also check cost is correct
        Restaurant[] participants;
        try {
            participants = Restaurant.getRestaurantsFromRestServer(new URL("https://ilp-rest.azurewebsites.net"));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        String[] testString = {"Meat Lover", "Vegan Delight"};
        Order order = new Order();
        int a = 0;
        try {
            a = order.getDeliveryCost(participants, testString);
        } catch (Exception e) {
            assertEquals( "InvalidPizzaCombinationMultipleSuppliers", e.getMessage() );
        }

        assertEquals(2600, a);
    }

    @Test
    public void testDeliveryCost2(){
        // pizza names are in two restaurants
        // return false
        Restaurant[] participants;
        try {
            participants = Restaurant.getRestaurantsFromRestServer(new URL("https://ilp-rest.azurewebsites.net///"));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        String[] testString = {"Meat Lover", "Margarita"};
        Order order = new Order();
        int a = 0;

        try{
            a = order.getDeliveryCost(participants, testString);
            fail("Expected exception");
        }
        catch(Exception e ) {
            assertEquals( "InvalidPizzaCombinationMultipleSuppliers", e.getMessage() ); // Optionally make sure you get the correct message, too
        }

        assertEquals(0, a);

    }

    @Test
    public void testDeliveryCost3(){
        // pizzas with 0 string
        Restaurant[] participants;


        try {
            participants = Restaurant.getRestaurantsFromRestServer(new URL("https://ilp-rest.azurewebsites.net///"));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        String[] testString = {};

        Order order = new Order();
        int a = 0;

        try{
            a = order.getDeliveryCost(participants, testString);
            fail("Expected exception");
        }
        catch(Exception e ) {
            assertEquals( "InvalidPizzaCount", e.getMessage() ); // Optionally make sure you get the correct message, too
        }

        assertEquals(0, a);
    }

    @Test
    public void testDeliveryCost4() {
        //Too many pizza entries
        Restaurant[] participants;


        try {
            participants = Restaurant.getRestaurantsFromRestServer(new URL("https://ilp-rest.azurewebsites.net/////"));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        String[] testString = {"Meat Lover", "Meat Lover", "Meat Lover", "Meat Lover", "Meat Lover"};

        Order order = new Order();
        int a = 0;

        try{
            a = order.getDeliveryCost(participants, testString);
            fail("Expected exception");
        }
        catch(Exception e ) {
            assertEquals( "InvalidPizzaCount", e.getMessage() ); // Optionally make sure you get the correct message, too
        }

        assertEquals(0, a);
    }
}
