package uk.ac.ed.inf;

import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;

/**
 * Hello world!
 *
 */
public class App
{
    public static boolean isDateValid(String date)
    {
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            df.setLenient(false);
            df.parse(date);

            String[] d = date.split("-");
            int year = Integer.parseInt(d[0]);
            if ( year <= 1975  || d[1].length() != 2 || d[2].length() != 2) {
                return false;
            }

            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    /* Returns true if url is valid */
    public static boolean isValidURL(String url)
    {
        /* Try creating a valid URL */
        try {
            new URL(url).toURI();
            return true;
        }

        // If there was an Exception
        // while creating URL object
        catch (Exception e) {
            return false;
        }
    }

    public static void moveDroneToPick() {

    }


    // TODO
    /**
     * Create the flightpath geojson file
     * @param allFlightpath all flightpath of the day
     * @param year required year
     * @param month required month
     * @param day required day
     * @throws IOException when failed to create Geojson file
     */
    private static void createGeojson(List<Flightpath> allFlightpath, String year,
                                      String month, String day) {
        // convert flightpath objects to linestring
        List<Point> flightpathPoints = new ArrayList<>();
        flightpathPoints.add(Point.fromLngLat(allFlightpath.get(0).fromLongitude, allFlightpath.get(0).fromLatitude));
        for (Flightpath fp : allFlightpath){
            flightpathPoints.add(Point.fromLngLat(fp.toLongitude, fp.toLatitude));
        }
        LineString flightpathLineString = LineString.fromLngLats(flightpathPoints);
        Feature flightpathFeature = Feature.fromGeometry( (Geometry) flightpathLineString );

        // convert flightpath to one feature in a feature collection
        ArrayList<Feature> flightpathList = new ArrayList<Feature>();
        flightpathList.add(flightpathFeature);
        FeatureCollection flightpathFC = FeatureCollection.fromFeatures(flightpathList);
        String flightpathJson = flightpathFC.toJson();

        // write the geojson file
        try {
            FileWriter myWriter = new FileWriter(
                    "drone-" + day + "-" + month + "-" + year + ".geojson");
            myWriter.write(flightpathJson);
            myWriter.close();
            System.out.println("Flightpath Geojson created");
        } catch (IOException e) {
            System.err.println("Fatal error: Unable to generate flightpath Geojson");
            e.printStackTrace();
        }
    }


    public static void main( String[] args ) {
        if (isDateValid(args[0])) {
            String[] date = args[0].split("-");
            String year = date[0];
            String month = date[1];
            String day = date[2];
        } else {
            throw new IllegalArgumentException("Invalid date input");
        }


        if (isValidURL(args[1])) {
            String baseAddress = args[1];
        } else {
            throw new IllegalArgumentException("Invalid URL input");
        }

        String seed = args[2];


    }
}
