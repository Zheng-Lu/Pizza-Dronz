package uk.ac.ed.inf;

public class Menu {

    private String name;
    private int priceInPence;

    // Getters
    public String getName() {
        return name;
    }

    public int getPriceInPence() {
        return priceInPence;
    }

    // Setters
    public void setName(String newName) {
        this.name = newName;
    }

    public void setPriceInPence(int newPrice) {
        this.priceInPence = newPrice;
    }

}
