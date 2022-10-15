package uk.ac.ed.inf;

import org.junit.Test;
import org.junit.jupiter.api.RepeatedTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;
import static uk.ac.ed.inf.LngLat.LENGTH_OF_MOVE;

public class LngLatTest {

    private LngLat lnglat;
    private final static Double MAX_LATITUDE = 90.0;
    private final static Double MIN_LATITUDE = -90.0;
    private final static Double MAX_LONGITUDE = 180.0;
    private final static Double MIN_LONGITUDE = -180.0;
    private final static List<List<Double>> LngLats = CentralArea.getCentralAreaLngLats();
    private final static List<Double> Lngs = LngLats.get(0);
    private final static List<Double> Lats = LngLats.get(1);
    private final static Double minLng = Collections.min(Lngs);
    private final static Double maxLng = Collections.max(Lngs);
    private final static Double minLat = Collections.min(Lats);
    private final static Double maxLat = Collections.max(Lats);

    private final static Double delta = 0.000000000001;


    public static double getRandomCentralLng() {
        Random r = new Random();

        return minLng + (maxLng - minLng) * r.nextDouble();
    }

    public static double getRandomCentralLat() {
        Random r = new Random();

        return minLat + (maxLat - minLat) * r.nextDouble();
    }

    public static double getRandomNotCentralLng() {
        Random r = new Random();
        double randomValue = MIN_LONGITUDE + (MAX_LONGITUDE - MIN_LONGITUDE) * r.nextDouble();

        while (minLng < randomValue && randomValue < maxLng){
            randomValue = MIN_LATITUDE + (MAX_LATITUDE - MIN_LATITUDE) * r.nextDouble();
        }

        return randomValue;
    }

    public static double getRandomNotCentralLat() {
        Random r = new Random();
        double randomValue = MIN_LATITUDE + (MAX_LATITUDE - MIN_LATITUDE) * r.nextDouble();

        while (minLat < randomValue && randomValue < maxLat){
            randomValue = MIN_LATITUDE + (MAX_LATITUDE - MIN_LATITUDE) * r.nextDouble();
        }

        return randomValue;
    }

    public static double getRandomLat() {
        Random r = new Random();
        return MIN_LATITUDE + (MAX_LATITUDE - MIN_LATITUDE) * r.nextDouble();
    }

    public static double getRandomLng() {
        Random r = new Random();
        return MIN_LONGITUDE + (MAX_LONGITUDE - MIN_LONGITUDE) * r.nextDouble();
    }

    public static LngLat getRandomPointOnEdge(double lng1, double lat1, double lng2, double lat2) {
        double a = lat2 - lat1;
        double b = lng1 - lng2;
        double c = a*lng1 + b*lat1;

        Random r = new Random();

        double x;
        double y;

        if (lng1 == lng2) {
            x = lng1;
            y = minLat + (maxLat - minLat) * r.nextDouble();
            return new LngLat(x,y);
        }
        else if (lat1 == lat2) {
            x = minLng + (maxLng - minLng) * r.nextDouble();
            y = lat1;
            return new LngLat(x,y);
        } else {
            x = minLng + (maxLng - minLng) * r.nextDouble();
            y = (c - a*x)/b;
            return new LngLat(lng1,y);
        }
    }


    @Test
    @RepeatedTest(100)
    public void testIsCentralArea() {
        lnglat = new LngLat(getRandomCentralLng(), getRandomCentralLat());
        assertTrue( "It must be in central area", lnglat.inCentralArea());
        System.out.println("Test -> " + "[" + lnglat.lng + "," + lnglat.lat + "]");
    }

    @Test
    @RepeatedTest(100)
    public void testIsNotCentralArea() {
        lnglat = new LngLat(getRandomNotCentralLng(), getRandomNotCentralLat());
        assertFalse("It must not be in central area", lnglat.inCentralArea());
        System.out.println("Test -> " + "[" + lnglat.lng + "," + lnglat.lat + "]");
    }

    @Test
    @RepeatedTest(1)
    public void testVertexCases() {
        for (int i = 0; i < Lngs.size(); i++) {
            lnglat = new LngLat(Lngs.get(i), Lats.get(i));
            assertTrue( "It must be in central area", lnglat.inCentralArea());
            System.out.println("Test -> " + "[" + lnglat.lng + "," + lnglat.lat + "]");
        }
    }

    @Test
    @RepeatedTest(1)
    public void testEdgeCases() {
        int n = Lngs.size() - 1;

        lnglat = getRandomPointOnEdge(Lngs.get(n),Lats.get(n), Lngs.get(0), Lats.get(0));
        System.out.println("Test -> " + "[" + lnglat.lng + "," + lnglat.lat + "]");
        assertTrue( "It must be in central area", lnglat.inCentralArea());

        for (int i = 0; i < n; i++) {
            lnglat = getRandomPointOnEdge(Lngs.get(i),Lats.get(i), Lngs.get(i+1), Lats.get(i+1));
            System.out.println("Test -> " + "[" + lnglat.lng + "," + lnglat.lat + "]");
            assertTrue( "It must be in central area", lnglat.inCentralArea());
        }
    }

    @Test
    @RepeatedTest(1)
    public void testDistanceTo() {
        LngLat p1 = new LngLat(0, 0);
        LngLat p2 = new LngLat(0, 0);

        assertEquals(1.0, p1.distanceTo(new LngLat(1.0, 0.0)), delta);
        assertEquals(2.0, p1.distanceTo(new LngLat(0.0, 2.0)), delta);

        for (int i = 0; i < 100; i++) {
            double randomLng = getRandomLng();
            double randomLat = getRandomLat();
            assertEquals(Math.hypot(p2.lng - randomLng, p2.lat - randomLat),
                    p2.distanceTo(new LngLat(randomLng, randomLat)), 0.0);
        }


    }

    @Test
    @RepeatedTest(1)
    public void testCloseTo() {
        LngLat p1 = new LngLat(0, 0);
        LngLat p2 = new LngLat(0, 0);

        assertTrue(p1.closeTo(new LngLat(LngLat.DISTANCE_TOLERANCE - 0.00001, 0)));
        assertTrue(p2.closeTo(new LngLat(0, LngLat.DISTANCE_TOLERANCE - 0.00001)));
    }


    @Test
    @RepeatedTest(1)
    public void testNextPosition_1() {
        LngLat p1 = new LngLat(0, 0);
        assertEquals(new LngLat(LENGTH_OF_MOVE, 0).lng, p1.nextPosition(LngLat.Direction.East).lng, delta);
        assertEquals(new LngLat(LENGTH_OF_MOVE, 0).lat, p1.nextPosition(LngLat.Direction.East).lat, delta);
        assertEquals(new LngLat(-LENGTH_OF_MOVE, 0).lng, p1.nextPosition(LngLat.Direction.West).lng, delta);
        assertEquals(new LngLat(-LENGTH_OF_MOVE, 0).lat, p1.nextPosition(LngLat.Direction.West).lat, delta);
        assertEquals(new LngLat(0, LENGTH_OF_MOVE).lng, p1.nextPosition(LngLat.Direction.North).lng, delta);
        assertEquals(new LngLat(0, -LENGTH_OF_MOVE).lat, p1.nextPosition(LngLat.Direction.South).lat, delta);
    }

    @Test
    @RepeatedTest(100)
    public void testNextPosition_2() {
        LngLat.Direction[] directions = LngLat.Direction.values();

        for (int i = 0; i < directions.length; i++) {
            LngLat p = new LngLat(getRandomLng(), getRandomLat());
            double angle = Math.toRadians(i * 22.5);

            assertEquals(p.lng + Math.cos(angle) * LENGTH_OF_MOVE, p.nextPosition(directions[i]).lng, delta);
            assertEquals(p.lat + Math.sin(angle) * LENGTH_OF_MOVE, p.nextPosition(directions[i]).lat, delta);

            System.out.println("Test -> The next position of " + Arrays.toString(new double[]{p.lng, p.lat})
                    + " is " + Arrays.toString(new double[]{p.nextPosition(directions[i]).lng, p.nextPosition(directions[i]).lat}));
        }
    }

    @Test
    @RepeatedTest(1)
    public void inCentralAreaTest() {
        LngLat pointIN = new LngLat(-3.185, 55.943);
        LngLat pointOnEdge = new LngLat(-3.185, 55.942617);
        LngLat pointOut = new LngLat(100, 100);
        LngLat pointCorner1 = new LngLat(-3.184319, 55.942617);
        LngLat pointCorner2 = new LngLat(-3.192473, 55.946233);
        LngLat pointCorner3 = new LngLat(-3.192473, 55.942617);
        LngLat pointCorner4 = new LngLat(-3.184319, 55.946233);

        assertTrue(pointIN.inCentralArea());
        assertTrue(pointOnEdge.inCentralArea());
        assertFalse("point supposed to be out of central area!", pointOut.inCentralArea());
        assertTrue(pointCorner1.inCentralArea());
        assertTrue(pointCorner2.inCentralArea());
        assertTrue(pointCorner3.inCentralArea());
        assertTrue(pointCorner4.inCentralArea());
    }

    @Test
    @RepeatedTest(1)
    public void distanceToTest() {
        LngLat point1 = new LngLat(3,0);
        LngLat point2 = new LngLat(0, 4);
        assertTrue(5.0 == point1.distanceTo(point2));
    }

    @Test
    @RepeatedTest(1)
    public void closeToTest(){
        LngLat point1 = new LngLat(3,0);
        LngLat point2 = new LngLat(0, 4);
        LngLat point3 = new LngLat(0, 4.000001);
        assertFalse(point1.closeTo(point2));
        assertTrue(point2.closeTo(point3));
    }

    @Test
    @RepeatedTest(1)
    public void InCentralAreaTest() {
        LngLat a = new LngLat(-3.200000, 55.947000);
        LngLat b = new LngLat(-3.192473, 55.946233);
        LngLat c = new LngLat(-3.192473, 55.946000);
        LngLat d = new LngLat(-3.190000, 55.946000);

        assertFalse(a.inCentralArea());
        assertTrue(b.inCentralArea());
        assertTrue(c.inCentralArea());
        assertTrue(d.inCentralArea());
    }

    @Test
    @RepeatedTest(1)
    public void testCentralArea_a(){
        // This a default normal case where appleton tower is located in central area

        var appletonTower = new LngLat(-3.186874, 55.944494);
        boolean result = appletonTower.inCentralArea();
        assertTrue(result);

        var loc1 = new LngLat(-3.192473, 55.942618);
        boolean result1 = loc1.inCentralArea();
        assertTrue(result1);

        var loc2 = new LngLat(-3.192473, 55.946232);
        boolean result2 = loc2.inCentralArea();
        assertTrue(result2);

        var loc3 = new LngLat(-3.184320, 55.946232);
        boolean result3 = loc3.inCentralArea();
        assertTrue(result3);

        var loc4 = new LngLat(-3.184319, 55.942618);
        boolean result4 = loc4.inCentralArea();
        assertTrue(result4);

    }

    @Test
    @RepeatedTest(1)
    public void testNotInCentralArea_a(){
        // This a default normal case where appleton tower is located in central area

        var loc1 = new LngLat(-3.192473, 55.946234);
        boolean result1 = loc1.inCentralArea();
        assertFalse(result1);

        var loc2 = new LngLat(-3.192474, 55.946233);
        boolean result2 = loc2.inCentralArea();
        assertFalse(result2);

        var loc3 = new LngLat(-3.184318, 55.946233);
        boolean result3 = loc3.inCentralArea();
        assertFalse(result3);

    }

    @Test
    @RepeatedTest(1)
    public void testCentralAreaBoundary_a(){
        // This a default normal case where appleton tower is located in central area

        var loc1 = new LngLat(-3.192473, 55.946233);
        var loc2 = new LngLat(-3.192473, 55.942617);
        var loc3 = new LngLat(-3.184319, 55.946233);
        var loc4 = new LngLat(-3.184319, 55.942617);
        boolean result1 = loc1.inCentralArea();
        boolean result2 = loc2.inCentralArea();
        boolean result3 = loc3.inCentralArea();
        boolean result4 = loc4.inCentralArea();

        assertTrue(result1);
        assertTrue(result2);
        assertTrue(result3);
        assertTrue(result4);
    }


    @Test
    @RepeatedTest(1)
    public void testNextPositionNormal_a(){
        var appletonTower = new LngLat(-3.186874, 55.944494);
        LngLat newPosition = appletonTower.nextPosition(LngLat.Direction.North);
        double radians = Math.toRadians(90);

        double longitude = -3.186874 + (Math.cos(radians)) * 0.00015;
        // sin() method to get the sine value
        double latitude = 55.944494+ (Math.sin(radians)) * 0.00015;

        assertEquals(new LngLat(longitude, latitude).lng, newPosition.lng, delta);
        System.out.println("Expected: " + new LngLat(longitude, latitude).lng + "\n" +
                "Actual: " +  newPosition.lng);
        assertEquals(new LngLat(longitude, latitude).lat, newPosition.lat, delta);
        System.out.println("Expected: " + new LngLat(longitude, latitude).lat + "\n" +
                "Actual: " +  newPosition.lat);
    }

    @Test
    @RepeatedTest(1)
    public void testNextPositionNull(){
        var appletonTower = new LngLat(-3.186874, 55.944494);
        LngLat newPosition = appletonTower.nextPosition(null);
        assertEquals(appletonTower, newPosition);
    }



}
