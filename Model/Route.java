package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Route.java
 * 
 * Represents a complete travel route from a source to a destination.
 * A route is a sequence of airport codes connected by flights.
 * 
 * DSA Role: Result container for algorithm output.
 *           Internally uses a LinkedList/ArrayList for path storage.
 */
public class Route {

    // Ordered list of airport codes forming the path
    // e.g., ["CAI", "DXB", "LHR"]
    private List<String> path;

    // Total cost of all flights in this route (USD)
    private double totalCost;

    // Total travel duration in minutes
    private int totalDurationMinutes;

    // Number of stops (layovers = path.size() - 2)
    // For a direct flight: 0 layovers
    private int layovers;

    // The optimization mode used to find this route
    private String optimizationMode;

    /**
     * Default constructor — initializes an empty route.
     */
    public Route() {
        this.path = new ArrayList<>();
        this.totalCost = 0.0;
        this.totalDurationMinutes = 0;
        this.layovers = 0;
        this.optimizationMode = "Unknown";
    }

    /**
     * Full constructor.
     */
    public Route(List<String> path, double totalCost, int totalDurationMinutes, String optimizationMode) {
        this.path = new ArrayList<>(path);
        this.totalCost = totalCost;
        this.totalDurationMinutes = totalDurationMinutes;
        // Layovers = total airports in path minus source and destination
        this.layovers = Math.max(0, path.size() - 2);
        this.optimizationMode = optimizationMode;
    }

    // ──────────────── Getters ────────────────

    public List<String> getPath() {
        return path;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public int getTotalDurationMinutes() {
        return totalDurationMinutes;
    }

    public int getLayovers() {
        return layovers;
    }

    public String getOptimizationMode() {
        return optimizationMode;
    }

    // ──────────────── Setters ────────────────

    public void setPath(List<String> path) {
        this.path = path;
        this.layovers = Math.max(0, path.size() - 2);
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public void setTotalDurationMinutes(int totalDurationMinutes) {
        this.totalDurationMinutes = totalDurationMinutes;
    }

    public void setLayovers(int layovers) {
        this.layovers = layovers;
    }

    public void setOptimizationMode(String optimizationMode) {
        this.optimizationMode = optimizationMode;
    }

    /**
     * Adds an airport code to the path.
     */
    public void addToPath(String airportCode) {
        path.add(airportCode);
        this.layovers = Math.max(0, path.size() - 2);
    }

    /**
     * Returns the source airport of this route.
     */
    public String getSource() {
        if (path.isEmpty()) return "N/A";
        return path.get(0);
    }

    /**
     * Returns the destination airport of this route.
     */
    public String getDestination() {
        if (path.isEmpty()) return "N/A";
        return path.get(path.size() - 1);
    }

    /**
     * Converts total duration to a human-readable format.
     * Example: 375 minutes → "6h 15m"
     */
    public String getFormattedDuration() {
        int hours = totalDurationMinutes / 60;
        int minutes = totalDurationMinutes % 60;
        return hours + "h " + minutes + "m";
    }

    /**
     * Returns the route path as a readable arrow-separated string.
     * Example: "CAI → DXB → LHR"
     */
    public String getPathString() {
        return String.join(" → ", path);
    }

    /**
     * Checks if this route is valid (has at least 2 airports).
     */
    public boolean isValid() {
        return path != null && path.size() >= 2;
    }

    /**
     * Full string summary of the route.
     */
    @Override
    public String toString() {
        return "Route [" + optimizationMode + "]\n" +
               "  Path:     " + getPathString() + "\n" +
               "  Cost:     $" + String.format("%.2f", totalCost) + "\n" +
               "  Duration: " + getFormattedDuration() + "\n" +
               "  Layovers: " + layovers;
    }
}