package uk.ac.ed.inf;

public class Drone {
    public static final int BATTERY = 2000;
    public boolean returned;
    private final LngLat startPos;
    private LngLat dronePos;
    private int remainBattery;
    private int numPizzaCarried;


    public Drone(LngLat startPos) {
        this.startPos = startPos;
        this.dronePos = new LngLat (startPos.lng, startPos.lat);
        this.remainBattery = BATTERY;
        this.returned = false;
    }
}
