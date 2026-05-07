package graph;

import model.Airport;
import model.Flight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * FlightGraph.java
 * 
 * Core graph data structure for the Flight Route Planner.
 * 
 * ── Data Structure: Directed Weighted Graph using Adjacency List ──
 * 
 *   Vertices  → Airports (stored in a HashMap<String, Airport>)
 *   Edges     → Flights  (stored as LinkedList<Flight> per vertex)
 *   Weights   → Cost (USD) and Duration (minutes)
 * 
 * ── Why Adjacency List? ──
 *   - Airline networks are SPARSE: not every airport connects to every other.
 *   - Space complexity: O(V + E) vs O(V²) for adjacency matrix.
 *   - Faster edge iteration for Dijkstra and BFS on sparse graphs.
 * 
 * ── Complexity Summary ──
 *   Add airport      → O(1) average (HashMap insert)
 *   Remove airport   → O(V + E) (must remove all edges too)
 *   Add flight       → O(1) average (LinkedList append)
 *   Remove flight    → O(degree(v)) (scan adjacency list)
 *   Get neighbors    → O(1) average (HashMap lookup)
 *   Space            → O(V + E)
 */
public class FlightGraph {

    // Maps airport IATA code → Airport object
    // O(1) average lookup — used throughout Dijkstra, BFS, DFS
    private HashMap<String, Airport> airports;

    // Maps airport IATA code → list of outgoing flights
    // LinkedList chosen for O(1) append and O(n) traversal
    private HashMap<String, LinkedList<Flight>> adjacencyList;

    /**
     * Constructs an empty flight graph.
     */
    public FlightGraph() {
        airports = new HashMap<>();
        adjacencyList = new HashMap<>();
    }

    // ════════════════════════════════════════════
    //   AIRPORT OPERATIONS
    // ════════════════════════════════════════════

    /**
     * Adds an airport to the graph.
     * Also initializes an empty adjacency list for it.
     * Time: O(1) average
     * 
     * @param airport Airport to add
     * @return true if added, false if already exists
     */
    public boolean addAirport(Airport airport) {
        if (airports.containsKey(airport.getCode())) {
            return false; // Already exists
        }
        airports.put(airport.getCode(), airport);
        adjacencyList.put(airport.getCode(), new LinkedList<>());
        return true;
    }

    /**
     * Removes an airport and ALL its associated flights.
     * Time: O(V + E) — must scan all adjacency lists to remove inbound edges
     * 
     * @param code IATA code of the airport to remove
     * @return true if removed, false if not found
     */
    public boolean removeAirport(String code) {
        if (!airports.containsKey(code)) {
            return false;
        }

        // Remove the airport's own adjacency list
        airports.remove(code);
        adjacencyList.remove(code);

        // Remove all flights FROM other airports TO this airport
        for (LinkedList<Flight> flights : adjacencyList.values()) {
            flights.removeIf(f -> f.getDestination().equalsIgnoreCase(code));
        }

        return true;
    }

    /**
     * Checks if an airport exists in the graph.
     * Time: O(1) average
     */
    public boolean hasAirport(String code) {
        return airports.containsKey(code);
    }

    /**
     * Returns an Airport object by IATA code.
     * Time: O(1) average
     */
    public Airport getAirport(String code) {
        return airports.get(code);
    }

    /**
     * Returns all airports as a list.
     * Time: O(V)
     */
    public List<Airport> getAllAirports() {
        return new ArrayList<>(airports.values());
    }

    /**
     * Returns a copy of the airports HashMap (used by algorithms).
     */
    public HashMap<String, Airport> getAirportMap() {
        return airports;
    }

    /**
     * Returns the number of airports (vertices) in the graph.
     */
    public int getAirportCount() {
        return airports.size();
    }

    // ════════════════════════════════════════════
    //   FLIGHT OPERATIONS
    // ════════════════════════════════════════════

    /**
     * Adds a directed flight edge from source to destination.
     * Time: O(1) average
     * 
     * @param flight Flight to add
     * @return true if added, false if either airport doesn't exist
     */
    public boolean addFlight(Flight flight) {
        String src = flight.getSource();
        String dst = flight.getDestination();

        if (!airports.containsKey(src) || !airports.containsKey(dst)) {
            return false; // One or both airports not in graph
        }

        adjacencyList.get(src).add(flight);
        return true;
    }

    /**
     * Removes a flight from source to destination.
     * Time: O(degree(source))
     * 
     * @param source      Source airport code
     * @param destination Destination airport code
     * @return true if removed, false if not found
     */
    public boolean removeFlight(String source, String destination) {
        LinkedList<Flight> flights = adjacencyList.get(source);
        if (flights == null) return false;
        return flights.removeIf(f -> f.getDestination().equalsIgnoreCase(destination));
    }

    /**
     * Returns all outgoing flights from a given airport.
     * Time: O(1) average
     * 
     * @param airportCode Source airport code
     * @return LinkedList of outgoing flights, or empty list if none
     */
    public LinkedList<Flight> getFlightsFrom(String airportCode) {
        LinkedList<Flight> result = adjacencyList.get(airportCode);
        return (result != null) ? result : new LinkedList<>();
    }

    /**
     * Returns all flights in the entire graph.
     * Time: O(V + E)
     */
    public List<Flight> getAllFlights() {
        List<Flight> all = new ArrayList<>();
        for (LinkedList<Flight> list : adjacencyList.values()) {
            all.addAll(list);
        }
        return all;
    }

    /**
     * Returns the total number of flight edges.
     * Time: O(V)
     */
    public int getFlightCount() {
        int count = 0;
        for (LinkedList<Flight> list : adjacencyList.values()) {
            count += list.size();
        }
        return count;
    }

    /**
     * Returns all airport codes currently in the graph.
     * Used by algorithms to iterate over all vertices.
     */
    public java.util.Set<String> getAllAirportCodes() {
        return airports.keySet();
    }

    /**
     * Returns the full adjacency list map.
     * Used by graph traversal algorithms.
     */
    public HashMap<String, LinkedList<Flight>> getAdjacencyList() {
        return adjacencyList;
    }

    /**
     * Clears the entire graph (all airports and flights).
     */
    public void clear() {
        airports.clear();
        adjacencyList.clear();
    }

    /**
     * Returns a text representation of the graph for debugging.
     * Time: O(V + E)
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Flight Graph ===\n");
        sb.append("Airports: ").append(airports.size()).append("\n");
        sb.append("Flights:  ").append(getFlightCount()).append("\n\n");

        for (Map.Entry<String, LinkedList<Flight>> entry : adjacencyList.entrySet()) {
            sb.append("[").append(entry.getKey()).append("]\n");
            for (Flight f : entry.getValue()) {
                sb.append("  ").append(f).append("\n");
            }
        }
        return sb.toString();
    }
}