import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Stream;

public class Main {

    private static final String AIRPORT_LIST = "src/airports.csv";
    private static final String DELIMETER = ",";
    private static final int ICAO_INDEX = 5;
    private static final int LATITUDE_INDEX = 6;
    private static final int LONGITUDE_INDEX = 7;
    private static final String BASE_URL = ("https://public-api.adsbexchange.com/VirtualRadar/AircraftList.json?lat=%s&lng=%s&fDstL=0&fDstU=%d");

    /**
     * Returns the latitude and longitude of the airport matching the provided ICAO code.
     *
     * @param airportCode The ICAO code of the airport.
     * @return The latitude and longitude as a comma separated string.
     */
    private static String locateAirport(String airportCode) {
        try(Stream<String> stream = Files.lines(Paths.get(AIRPORT_LIST))){
            Optional<String> airport = stream.filter(line -> line
                    .split(DELIMETER)[ICAO_INDEX]
                    .replace("\"", "")
                    .equalsIgnoreCase(airportCode))
                    .findFirst();

            if(airport.isPresent()) {
                String[] tokens = airport.get().split(DELIMETER);
                return tokens[LATITUDE_INDEX] + "," + tokens[LONGITUDE_INDEX];
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String findLocalAircraft(String latitude, String longitude, int range) {
        String inputLine;
        StringBuilder response = new StringBuilder();

        try {
            URL url = new URL(String.format(BASE_URL, latitude, longitude, range));
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
            connection.setUseCaches(false);
            connection.setDoOutput(true);

            if(connection.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                while((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject obj = new JSONObject(response.toString());
                JSONArray results = obj.getJSONArray("acList");
                return results.toString();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        System.out.println("Enter reference airport (ICAO): ");
        String referenceAirport = new Scanner(System.in).next();

        String location = locateAirport(referenceAirport);
        System.out.println("Found airport at " + location);

        String latitude = location.split(DELIMETER)[0];
        String longitude = location.split(DELIMETER)[1];

        System.out.println(findLocalAircraft(latitude, longitude, 100));
    }

}
