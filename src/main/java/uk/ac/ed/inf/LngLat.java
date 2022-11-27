package uk.ac.ed.inf;

import java.util.HashMap;
import java.util.List;
import java.awt.geom.Line2D;
import java.util.Objects;
import com.mapbox.geojson.Polygon;
import com.mapbox.geojson.Point;

/**
 * Class to define the location for the objects (e.g., drone, restaurants) and fundamental methods
 */
public class LngLat {

    /* Constants */
    public final static double DISTANCE_TOLERANCE = 0.00015;
    public final static double LENGTH_OF_MOVE = 0.00015;

    private final HashMap<Direction, Double> directionHashMap = initMap();
    private HashMap<Direction, Double> initMap() {
        HashMap<Direction, Double> hashmap = new HashMap<>();
        for (Direction dir : Direction.values()){
            hashmap.put(dir, dir.value);
        }
        return hashmap;
    }

    /** coordinate of the drone */
    private double lng;
    private double lat;
    private double angle;

    /**
     * Create a coordinate instance for the drone.
     * @param lng the longitude coordinate of the drone
     * @param lat the latitude coordinate of the drone
     */
    public LngLat(double lng, double lat) {
        this.lng = lng;
        this.lat = lat;
        // define initial angle to be zero
        this.angle = 0.0;
    }

    /* Getters and Setters */

    /**@return the longitude of the current LngLat object*/
    public double getLng(){
        return this.lng;
    }

    /**@return the latitude of the current LngLat object*/
    public double getLat(){
        return this.lat;
    }

    /**@return the anticlockwise angle between the current LngLat object and East direction */
    public double getAngle(){
        return this.angle;
    }

    public void setLngLat(LngLat newPos){
        this.lng = newPos.getLng();
        this.lat = newPos.getLat();
    }


    /**
     * Test if the drone is in the central area
     *
     * @return true if the drone is inside the central area or on the boundary of central area, false if it is outside
     */
    public boolean inCentralArea(DataParser dataParser) {
        List<LngLat> centralArea = dataParser.getCentralArea();

        // Number of vertices n
        int n = centralArea.size();

        // Detect if it is on vertex or edge
        if (isOnPolygonOutline(centralArea.get(0).lng, centralArea.get(0).lat,
                centralArea.get(n - 1).lng, centralArea.get(n - 1).lat)){
            return true;
        }
        for (int i = 0; i < n - 1; i++) {
            if (isOnPolygonOutline(centralArea.get(i).lng, centralArea.get(i).lat,
                    centralArea.get(i+1).lng, centralArea.get(i+1).lat)) {
                return true;
            }
        }

        // Detect if the drone still in polygon central area
        int i, j;
        boolean res = false;
        for (i = 0, j = n - 1; i < n; j = i++) {
            double lat_i =  centralArea.get(i).lat;
            double lat_j =  centralArea.get(j).lat;
            double lng_i =  centralArea.get(i).lng;
            double lng_j =  centralArea.get(j).lng;

            if (((lat_i > lat) != (lat_j > lat)) &&
                    (lng < (lng_j - lng_i) * (lat - lat_i) / (lat_j - lat_i) + lng_i)){
                res = !res;
            }
        }
        return res;
    }


    /**
     * A helper function for inCentralArea()
     * To determine whether the drone locates at the boundary of the polygon area
     *
     * @param x1 the longitude of first location point
     * @param y1 the latitude of first location point
     * @param x2 the longitude of second location point
     * @param y2 the latitude of second location point
     * @return true if the drone is on the boundary of the polygon area, false if not
     */
    private boolean isOnPolygonOutline(double x1, double y1, double x2, double y2) {
        return Math.hypot(x1-lng, y1-lat) + Math.hypot(lng-x2, lat-y2) == Math.hypot(x1-x2, y1-y2);
    }


    /**
     * Pythagorean distance from the current drone position to another position,
     * assuming the drone is on a plane
     *
     * @param pos the other position to be calculated
     * @return the Pythagorean distance between two points
     * @throws NullPointerException If the given input is null.
     */
    public double distanceTo(LngLat pos) {
        if (pos == null){
            throw new NullPointerException("The input cannot be null");
        }

        // calculate Pythagorean distance of two points
        return  Math.hypot(this.lng - pos.lng, this.lat - pos.lat);
    }


    /**
     * Test if two points are close to each other:
     * strictly less than the distance tolerance of 0.00015 degrees
     *
     * @param pos the other position to be tested
     * @return true if two points are strictly less than distance tolerance, false if otherwise
     * @throws NullPointerException If the given input is null.
     */
    public boolean closeTo (LngLat pos) {
        if (pos == null){
            throw new NullPointerException("The input cannot be null");
        }

        return distanceTo(pos) < DISTANCE_TOLERANCE;
    }

    /**
     *  This enum class documents the direction from 16 major compass directions
     */
    public enum Direction {
        East (0),
        East_North_East (22.5),
        North_East (45),
        North_North_East (67.5),
        North (90),
        North_North_West (112.5),
        North_West (135),
        West_North_West (157.5),
        West (180),
        West_South_West (202.5),
        South_West (225),
        South_South_West (247.5),
        South (270),
        South_South_East(292.5),
        South_East (315),
        East_South_East (337.5),
        Null (-999);

        private final double value;

        Direction(double value) {
            this.value = value;
        }

        public double getValue() {
            return this.value;
        }

    }

    /**
     * When the drone makes a move, fly or hover, give the result coordinate of the drone
     *
     * @param direction The angle the drone is flying to, enum type null if hovering,
     * @return The result coordinate of the move, return the original coordinate if choose to hover
     */
    public LngLat nextPosition (Direction direction){
        // when drone is hovering
        if (direction.value == -999){
            return this;
        } else {
            double angle = Math.toRadians(direction.value);
            double newLong = this.lng + Math.cos(angle) * LENGTH_OF_MOVE;
            double newLat = this.lat + Math.sin(angle) * LENGTH_OF_MOVE;

            return new LngLat (newLong, newLat);
        }
    }


    /**
     * Get the valid angle of the drone to travel next
     *
     * @param nextPos the desired position
     * @return valid angle of the drone
     */
    private double calculateAngle(LngLat nextPos){
        double x1 = this.lng;
        double x2 = nextPos.getLng();
        double y1 = this.lat;
        double y2 = nextPos.getLat();

        double radAngle = Math.atan2(x2 - x1, y2 - y1);
        int rawAngle = (int) Math.toDegrees(radAngle);

        // set the angle valid
        if (rawAngle < 0){
            rawAngle += 360;
        } else if (rawAngle >= 360){
            rawAngle -= 360;
        }
        if (rawAngle >= 0 && rawAngle < 90) {
            rawAngle = 90 - rawAngle;
        } else if (rawAngle >= 90 && rawAngle < 360) {
            rawAngle = 450 - rawAngle;
        }

        double angle = Math.round(rawAngle/22.5) * 22.5;

        if (angle == 360){
            angle = 0;
        }

        return angle;
    }


    /**
     * A helper function to convert the given valid angle into direction
     * @param angle angle that link to a direction
     * @return if angle is valid (should be the multiple of 22.5 in range [0,360)),
     * a direction from enum class would be return, otherwise return null
     */
    private Direction getDirectionByAngle(double angle){
        for (Direction direction: directionHashMap.keySet()) {
            if (Objects.equals(angle, direction.value)){
                return direction;
            }
        }
        return null;
    }


    /**
     * Test if going to the next position from current position is outside No-Fly-Zone
     *
     * @param dataParser the map of the drone navigating
     * @param nextPos the position to be tested
     * @return outside No-Fly-Zone or not
     */
    private boolean isOutsideNoFlyZone(DataParser dataParser, LngLat nextPos){
        Line2D path = new Line2D.Double(this.lat, this.lng,
                nextPos.getLat(), nextPos.getLng());
        List<Polygon> noFlyZones = dataParser.getNoFlyZones();

        // for every line segment of every zone,
        // test if it intersects with the line segment of the current move
        for (Polygon zone: noFlyZones) {
            List<Point> points = zone.coordinates().get(0);
            for (int i = 0; i < points.size() - 1; i++) {
                int j = (i + 1) % points.size();
                Point p1 = points.get(i);
                Point p2 = points.get(j);
                Line2D noFlySegment = new Line2D.Double(p1.latitude(), p1.longitude(),
                        p2.latitude(), p2.longitude());
                if (path.intersectsLine(noFlySegment)) {
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * Calculate the next valid move to given position,
     * outside no-fly-zone
     * if next move not valid, modify angle in +10,-10, +20,-20
     *
     * @param dataParser the map to traverse
     * @param destPos desired position of the drone
     * @return the next valid move
     */
    public LngLat move(DataParser dataParser, LngLat destPos){
        double preAngle = this.angle;
        this.angle = calculateAngle(destPos);
        LngLat nextPos = nextPosition(getDirectionByAngle(this.angle));
        double adjustment = 22.5;

        // if the move is not valid, increase the angle until it is valid
        while ( !(isOutsideNoFlyZone(dataParser, nextPos))){
            this.angle += adjustment;
            // go back to previous location is forbidden, since it might cause the drone trap in a point
            if (Math.abs(preAngle - this.angle) == 180){
                this.angle += 22.5*(adjustment/Math.abs(adjustment));
            }
            if (this.angle >= 360){
                this.angle -= 360;
            }
            if (this.angle < 0){
                this.angle += 360;
            }

            nextPos = nextPosition(getDirectionByAngle(this.angle));
            adjustment = - (adjustment + 22.5*(adjustment/Math.abs(adjustment)));

        }
        return nextPos;
    }
}


