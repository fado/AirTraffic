import org.json.JSONObject;

class Aircraft {

    private String registration;
    private String departureAirport;
    private String destinationAirport;
    private int altitude;
    private float track;
    private String latitude;
    private String longitude;

    private final String DELIMETER = ",";

    Aircraft(JSONObject aircraft) {
        this.registration = aircraft.has("Reg") ? aircraft.get("Reg").toString() : "Unknown";
        this.departureAirport = aircraft.has("From") ? aircraft.get("From").toString().split(DELIMETER)[0].substring(5) : "Unknown";
        this.destinationAirport = aircraft.has("To") ? aircraft.get("To").toString().split(DELIMETER)[0].substring(5) : "Unknown";
        this.altitude = aircraft.has("Alt") ? Integer.parseInt(aircraft.get("Alt").toString()) : -1;
        this.track = aircraft.has("Trak") ? Float.parseFloat(aircraft.get("Trak").toString()) : -1;
        this.latitude = aircraft.has("Lat") ? aircraft.get("Lat").toString() : "Unknown";
        this.longitude = aircraft.has("Long") ? aircraft.get("Long").toString() : "Unknown";
    }

    @Override
    public String toString() {
        return this.registration +": "
                + this.departureAirport +" - "+ this.destinationAirport
                +" Position: "+ this.latitude +","+ this.longitude
                +" Track: "+ this.track
                +" Altitude: "+ this.altitude;
    }

}
