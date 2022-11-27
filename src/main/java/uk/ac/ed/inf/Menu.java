package uk.ac.ed.inf;

/**
 * The class to store the information of a menu from a restaurant.
 */
public class Menu {

    private String name;
    private int priceInPence;

    /* Getters and setters */
    public String getName() {
        return name;
    }

    public int getPriceInPence() {
        return priceInPence;
    }

    public void setName(String newName) {
        this.name = newName;
    }
}
