import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Stream;

public class Main {

    private static final String AIRPORT_LIST = "src/airports.csv";
    private static final String DELIMETER = ",";
    private static final int ICAO_INDEX = 5;
    private static final int NAME_INDEX = 1;
    private static final int LATITUDE_INDEX = 6;
    private static final int LONGITUDE_INDEX = 7;
    private static final String BASE_URL = ("https://public-api.adsbexchange.com/VirtualRadar/AircraftList.json?" +
            "lat=%s&lng=%s&fDstL=0&fDstU=%d");

    /**
     * Returns the latitude and longitude of the airport matching the provided ICAO code.
     *
     * @param airportCode The ICAO code of the airport.
     * @return The latitude and longitude as a comma separated string.
     */
    private static String locateAirport(String airportCode) throws IOException {
        try(Stream<String> stream = Files.lines(Paths.get(AIRPORT_LIST))){
            Optional<String> airport = stream.filter(line -> line
                    .split(DELIMETER)[ICAO_INDEX]
                    .replace("\"", "")
                    .equalsIgnoreCase(airportCode))
                    .findFirst();

            if(airport.isPresent()) {
                String[] tokens = airport.get().split(DELIMETER);
                return tokens[LATITUDE_INDEX] + "," + tokens[LONGITUDE_INDEX] +","+ tokens[NAME_INDEX];
            }
            return null;
        }
    }

    /**
     * Returns a list of Aircraft local to the passed-in latitude and longitude, or none if there aren't any.
     *
     * @param latitude Reference airport's latitude.
     * @param longitude Reference airport's longitude.
     * @param range Range (in KM) from the airport that we should look.
     * @return List of Aircraft local to the passed-in latitude and longitude, or null if there aren't any.
     */
    private static List<Aircraft> findLocalAircraft(String latitude, String longitude, int range) throws IOException {

        if(latitude == null || longitude == null) {
            throw new InvalidParameterException("Latitude and longitude cannot be null.");
        }

        URL url = new URL(String.format(BASE_URL, latitude, longitude, range));
        HttpURLConnection connection = getConfiguredConnection(url);

        if(connection == null || connection.getResponseCode() != 200) {
            throw new ConnectException("Bad response code from the ADSBExchange API.");
        }

        String inputLine;
        StringBuilder response = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        while((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        JSONObject obj = new JSONObject(response.toString());
        JSONArray localAircraftJSON = obj.getJSONArray("acList");

        List<Aircraft> localAircraft = new ArrayList<>();
        if (localAircraftJSON != null) {

            for(int i = 0; i < localAircraftJSON.length(); i++) {
                JSONObject aircraft = localAircraftJSON.getJSONObject(i);
                localAircraft.add(new Aircraft(aircraft));
            }
        }
        return localAircraft;
    }

    /**
     * Returns a fully configured HttpURLConnection object.
     *
     * @param url The API URL that we're trying to access.
     * @return A fully configured HTTP URLConnection object.
     */
    private static HttpURLConnection getConfiguredConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 " +
                "(KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        connection.setUseCaches(false);
        connection.setDoOutput(true);

        return connection;
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Enter reference airport (ICAO): ");
        String referenceAirport = new Scanner(System.in).next();

        String location = locateAirport(referenceAirport);

        String latitude = null;
        String longitude = null;

        if (location != null) {
            latitude = location.split(DELIMETER)[0];
            longitude = location.split(DELIMETER)[1];
        } else {
            System.out.println("Invalid ICAO code.");
        }

        List<Aircraft> localAircraft = findLocalAircraft(latitude, longitude, 100);

        for(Aircraft aircraft : localAircraft) {
            System.out.println(aircraft.toString());
        }
    }

}
