public class Aircraft {

    private String registration;
    private String airline;
    private String departureAirport;
    private int numberOfPassangers;
    private float fuelRemaining;

    public Aircraft(String registration, String airline, String departureAirport, int numberOfPassangers, float fuelRemaining) {
        this.registration = registration;
        this.airline = airline;
        this.departureAirport = departureAirport;
        this.numberOfPassangers = numberOfPassangers;
        this.fuelRemaining = fuelRemaining;
    }
}
