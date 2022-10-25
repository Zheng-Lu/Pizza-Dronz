package uk.ac.ed.inf;

public class Flightpath {
    String orderNo;
    double fromLongitude;
    double fromLatitude;
    double angle;
    double toLongitude;
    double toLatitude;
    int ticksSinceStartOfCalculation;

    /**
     * Create one step in flightpath
     * @param orderNo order number
     * @param fromLongitude previous longitude coordinate of the drone
     * @param fromLatitude previous latitude coordinate of the drone
     * @param angle current angle of the drone
     * @param toLongitude next longitude coordinate of the drone
     * @param toLatitude next latitude coordinate of the drone
     * @param  ticksSinceStartOfCalculation next latitude coordinate of the drone
     */
    public Flightpath(String orderNo, double fromLongitude, double fromLatitude,
                      double angle, double toLongitude, double toLatitude,
                      int ticksSinceStartOfCalculation){
        this.orderNo = orderNo;
        this.fromLongitude = fromLongitude;
        this.fromLatitude = fromLatitude;
        this.angle = angle;
        this.toLongitude = toLongitude;
        this.toLatitude = toLatitude;
        this.ticksSinceStartOfCalculation =  ticksSinceStartOfCalculation;
    }
}
