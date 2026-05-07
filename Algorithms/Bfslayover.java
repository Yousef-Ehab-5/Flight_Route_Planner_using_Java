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
import java.util.Queue;

/**
 * BFSLayover.java
 * 
 * Finds the route with the fewest layovers (minimum number of flights)
 * using Breadth-First Search (BFS).
 * 
 * ── Why BFS for Minimum Layovers? ──
 *   BFS explores nodes level by level.
 *   Level 0 = source
 *   Level 1 = airports reachable in 1 flight
 *   Level 2 = airports reachable in 2 flights
 *   ...
 *   The FIRST time BFS reaches the destination, it has used the MINIMUM
 *   number of flights (edges), which directly means minimum layovers.
 * 
 * ── Pseudocode ──
 *   1. Add source to queue, mark visited
 *   2. While queue is not empty:
 *       a. Dequeue airport u
 *       b. For each neighbor v of u:
 *           If v is unvisited:
 *               Mark visited, set parent[v] = u
 *               If v == destination: return path
 *               Enqueue v
 *   3. Reconstruct path via parent map
 * 
 * ── Data Structures ──
 *   Queue  → BFS frontier (LinkedList as Queue)
 *   HashMap → visited set, parent map
 * 
 * ── Complexity ──
 *   Time:  O(V + E)
 *   Space: O(V)
 */
public class Bfslayover {

    // Number of nodes explored (for benchmarking)
    private int nodesExplored;

    /**
     * Finds the minimum-layover route from source to destination.
     * 
     * @param graph       The flight graph
     * @param source      Source airport IATA code
     * @param destination Destination airport IATA code
     * @return Route with the fewest flights/layovers
     */
    public Route findMinLayoverRoute(FlightGraph graph, String source, String destination) {

        nodesExplored = 0;

        // ── Visited set: prevents revisiting nodes ──
        Map<String, Boolean> visited = new HashMap<>();

        // ── Parent map: for path reconstruction ──
        Map<String, String> parent = new HashMap<>();

        // ── Cost and duration tracking along BFS path ──
        Map<String, Double> costMap = new HashMap<>();
        Map<String, Integer> durationMap = new HashMap<>();

        // Initialize all as unvisited
        for (String code : graph.getAllAirportCodes()) {
            visited.put(code, false);
            parent.put(code, null);
            costMap.put(code, 0.0);
            durationMap.put(code, 0);
        }

        // ── BFS Queue: LinkedList used as Queue ──
        // Java's LinkedList implements Queue interface
        Queue<String> queue = new LinkedList<>();

        // Enqueue source and mark visited
        visited.put(source, true);
        queue.offer(source);

        boolean found = false;

        // ── BFS Main Loop ──
        while (!queue.isEmpty()) {

            // Dequeue front element — O(1)
            String u = queue.poll();
            nodesExplored++;

            // Goal check
            if (u.equals(destination)) {
                found = true;
                break;
            }

            // Explore all outgoing flights from u
            LinkedList<Flight> flights = graph.getFlightsFrom(u);

            for (Flight flight : flights) {
                String v = flight.getDestination();

                // Skip if already visited
                if (!visited.containsKey(v) || visited.get(v)) {
                    continue;
                }

                // Mark visited and record parent
                visited.put(v, true);
                parent.put(v, u);

                // Accumulate cost and duration along this BFS path
                costMap.put(v, costMap.get(u) + flight.getCost());
                durationMap.put(v, durationMap.get(u) + flight.getDurationMinutes());

                // Enqueue for future exploration — O(1)
                queue.offer(v);
            }
        }

        // No path found
        if (!found && !source.equals(destination)) {
            // Check if destination was ever set in parent
            if (parent.get(destination) == null && !source.equals(destination)) {
                // Only fails if BFS never reached destination
                // Re-check: if destination was enqueued and dequeued we set found=true
                // But if it was visited via queue before breaking, check visited
                if (!visited.containsKey(destination) || !visited.get(destination)) {
                    return null;
                }
            }
        }

        // ── Reconstruct path from parent map ──
        List<String> path = new ArrayList<>();
        String current = destination;

        while (current != null) {
            path.add(current);
            current = parent.get(current);
        }

        Collections.reverse(path);

        // Validate path
        if (path.isEmpty() || !path.get(0).equals(source)) {
            return null;
        }

        double totalCost = costMap.get(destination);
        int totalDuration = durationMap.get(destination);

        return new Route(path, totalCost, totalDuration, "Fewest Layovers");
    }

    /**
     * Returns nodes explored count for benchmarking.
     */
    public int getNodesExplored() {
        return nodesExplored;
    }
}