package algorithms;

import graph.FlightGraph;
import model.Flight;
import model.Route;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * DijkstraFastest.java
 * 
 * Finds the fastest (minimum total duration) route between two airports
 * using Dijkstra's algorithm, but optimizing for duration instead of cost.
 * 
 * ── How it Differs from DijkstraCheapest ──
 *   - The edge weight used for relaxation is flight.getDurationMinutes()
 *   - The result minimizes total travel time, not cost
 * 
 * ── Pseudocode ──
 *   Same as Dijkstra, but with:
 *     weight(u, v) = flight duration in minutes
 * 
 * ── Complexity ──
 *   Time:  O((V + E) log V)
 *   Space: O(V)
 */
public class Dijkstrafastest {

    // Number of nodes explored (for benchmarking)
    private int nodesExplored;

    /**
     * Finds the fastest route from source to destination.
     * 
     * @param graph       The flight graph
     * @param source      Source airport IATA code
     * @param destination Destination airport IATA code
     * @return Route object with the minimum-duration path
     */
    public Route findFastestRoute(FlightGraph graph, String source, String destination) {

        nodesExplored = 0;

        // ── Initialize distance map (duration tracking) ──
        Map<String, Integer> duration = new HashMap<>();
        Map<String, String> parent = new HashMap<>();
        Map<String, Double> costMap = new HashMap<>();

        for (String code : graph.getAllAirportCodes()) {
            duration.put(code, Integer.MAX_VALUE);
            parent.put(code, null);
            costMap.put(code, 0.0);
        }

        duration.put(source, 0);

        // ── Min-Heap ordered by duration ──
        // Each entry: [airportCode, totalDurationSoFar]
        PriorityQueue<Object[]> minHeap = new PriorityQueue<>(
            (a, b) -> Integer.compare((int) a[1], (int) b[1])
        );

        minHeap.offer(new Object[]{source, 0});

        // ── Dijkstra Main Loop ──
        while (!minHeap.isEmpty()) {

            Object[] current = minHeap.poll();
            String u = (String) current[0];
            int durationU = (int) current[1];

            nodesExplored++;

            // Lazy deletion: skip outdated entries
            if (durationU > duration.get(u)) {
                continue;
            }

            // Early exit when destination reached
            if (u.equals(destination)) {
                break;
            }

            // ── Relax all outgoing flights ──
            LinkedList<Flight> neighbors = graph.getFlightsFrom(u);

            for (Flight flight : neighbors) {
                String v = flight.getDestination();

                if (!duration.containsKey(v)) continue;

                // New duration via u
                int newDuration = duration.get(u) + flight.getDurationMinutes();

                // Relax edge if shorter path found
                if (newDuration < duration.get(v)) {
                    duration.put(v, newDuration);
                    parent.put(v, u);
                    // Track cost along the fastest path (for display)
                    costMap.put(v, costMap.get(u) + flight.getCost());
                    minHeap.offer(new Object[]{v, newDuration});
                }
            }
        }

        // ── Reconstruct Route ──
        return buildRoute(source, destination, duration, parent, costMap, "Fastest");
    }

    /**
     * Reconstructs the path from the parent map.
     */
    private Route buildRoute(String source, String destination,
                             Map<String, Integer> duration,
                             Map<String, String> parent,
                             Map<String, Double> costMap,
                             String mode) {

        if (duration.get(destination) == null || duration.get(destination) == Integer.MAX_VALUE) {
            return null;
        }

        // Backtrack from destination to source
        List<String> path = new ArrayList<>();
        String current = destination;

        while (current != null) {
            path.add(current);
            current = parent.get(current);
        }

        Collections.reverse(path);

        if (path.isEmpty() || !path.get(0).equals(source)) {
            return null;
        }

        int totalDuration = duration.get(destination);
        double totalCost = costMap.get(destination);

        return new Route(path, totalCost, totalDuration, mode);
    }

    /**
     * Returns nodes explored count for benchmarking.
     */
    public int getNodesExplored() {
        return nodesExplored;
    }
}