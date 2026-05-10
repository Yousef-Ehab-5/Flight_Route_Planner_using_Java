package model;

/*
 -------------------------------- Flight.java----------------------------------
 BY: Abdallah  
 * Represents a directed weighted edge in the flight graph.
 * Each flight connects a source airport to a destination airport
 * with a given cost (USD) and duration (minutes). 
 * DSA Role: Graph edge with dual weights (cost and time)
 */

public class Flight {
   
    private String source;                     // IATA code of the departure airport    
    private String destination;           // IATA code of the arrival airport   
    private double cost;                    // Ticket cost in USD    
    private int durationMinutes;   // Flight duration in minutes

    // Optional: airline name for display purposes
    private String airline;

    
    public Flight(String source, String destination, double cost, int durationMinutes, String airline) {
        this.source = source;
        this.destination = destination;
        this.cost = cost;
        this.durationMinutes = durationMinutes;
        this.airline = airline;
    }

    /*
     * Simplified constructor (no airline).
     */
    public Flight(String source, String destination, double cost, int durationMinutes) {
        this(source, destination, cost, durationMinutes, "Unknown");
    }

    // ──────────────── Getters ────────────────//

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public double getCost() {
        return cost;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public String getAirline() {
        return airline;
    }

    // ──────────────── Setters ────────────────//

    public void setSource(String source) {
        this.source = source;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public void setAirline(String airline) {
        this.airline = airline;
    }

   
     // Converts duration from minutes to a human-readable format.
   
    public String getFormattedDuration() {
        int hours = durationMinutes / 60;
        int minutes = durationMinutes % 60;
        return hours + "h " + minutes + "m";
    }

    
      //Returns a string summary of this flight.
     
    @Override
    public String toString() {
        return source + " → " + destination +
               " | Cost: $" + String.format("%.0f", cost) +
               " | Duration: " + getFormattedDuration() +
               " | Airline: " + airline;
    }
}
