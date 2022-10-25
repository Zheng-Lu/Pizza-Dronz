package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.List;

public class Drone {
    public static final int BATTERY = 2000;
    public boolean returned;
    private final LngLat startPos;
    private LngLat dronePos;
    private int remainBattery;
    private int numPizzaCarried;
    private List<Flightpath> flightpaths;


    /**
     * Create the drone
     * @param startPos starting position of the drone, usually Appleton Tower
     */
    public Drone(LngLat startPos) {
        this.startPos = startPos;
        this.dronePos = new LngLat (startPos.lng, startPos.lat);
        this.remainBattery = BATTERY;
        this.returned = false;
        this.flightpaths = new ArrayList<Flightpath>();
    }




}
