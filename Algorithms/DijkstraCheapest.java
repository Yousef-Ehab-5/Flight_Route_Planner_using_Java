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

/*
  *--DijkstraCheapest.java
  *BY: Yousef
  
Finds the cheapest (minimum cost) route between two airports
using Dijkstra's shortest path algorithm with a Min-Heap (PriorityQueue).
 
 * ── Algorithm: Dijkstra's Shortest Path ──
 
 * Pseudocode:
    1. Initialize dist[source] = 0, dist[all others] = ∞
    2. Insert (source, 0) into min-heap
    3. While heap is not empty:
        a. Extract node u with minimum dist
        b. For each neighbor v of u:
            If dist[u] + weight(u,v) < dist[v]:
                Update dist[v]
                Update parent[v] = u
                Insert (v, dist[v]) into heap
    4. Reconstruct path using parent map
  
 * ── Why Dijkstra? ──
    - Optimal for non-negative edge weights (cost is always ≥ 0)
    - More efficient than Bellman-Ford for sparse graphs
    - Min-heap gives O(log V) extract-min operations
  
 * ── Complexity ──
    Time:  O((V + E) log V)
    Space: O(V)   — for dist[], parent[], and heap
 */
public class DijkstraCheapest {

    // Number of nodes explored (for experimental evaluation)
    private int nodesExplored;

    
     // Finds the cheapest route from source to destination.
     
    public Route findCheapestRoute(FlightGraph graph, String source, String destination) {

        nodesExplored = 0;

        // ── Step 1: Initialize data structures ──

        // dist maps airport code → minimum cost to reach it from source
        // Using HashMap for O(1) average lookup
        Map<String, Double> dist = new HashMap<>();

        // parent maps airport code → previous airport in cheapest path
        Map<String, String> parent = new HashMap<>();

        // durationMap tracks total duration along the cheapest-cost path
        Map<String, Integer> durationMap = new HashMap<>();

        // Initialize all distances to infinity
        for (String code : graph.getAllAirportCodes()) 
        {
            dist.put(code, Double.MAX_VALUE);
            parent.put(code, null);
            durationMap.put(code, 0);
        }

        // Source costs 0
        dist.put(source, 0.0);

        // ── Step 2: Priority Queue (Min-Heap) ──
        // Stores (airportCode, costSoFar)
        // PriorityQueue ordered by cost → acts as a min-heap
        

        // We use a double[]: [hashCode placeholder, cost]
        // But since PriorityQueue needs comparison, we use a small helper array:
        // index 0 → not used (airport stored separately via a parallel map)
        // We use a String-indexed map trick: store (code, cost) as Object[]

        // Simpler approach: use a NodeEntry helper class via anonymous array
        // We'll use a Map to link queue entries to airport codes
        // Actually, let's use a simple Object[] {String code, double cost}
        PriorityQueue<Object[]> minHeap = new PriorityQueue<>(
            (a, b) -> Double.compare((double) a[1], (double) b[1])
        );

        // Insert source with cost 0
        minHeap.offer(new Object[]{source, 0.0});

        // ── Step 3: Main Dijkstra loop ──
        while (!minHeap.isEmpty()) 
        {

            // Extract the airport with minimum cost — O(log V)
            Object[] current = minHeap.poll();
            String u = (String) current[0];
            double costU = (double) current[1];

            nodesExplored++;

            // Skip stale entries (lazy deletion pattern)
            if (costU > dist.get(u)) 
            {
                continue;
            }

            // Early termination: found destination
            if (u.equals(destination)) 
            {
                break;
            }

            // ── Step 4: Relax edges ──
            LinkedList<Flight> neighbors = graph.getFlightsFrom(u);

            for (Flight flight : neighbors) 
            {
                String v = flight.getDestination();

                // Skip if destination airport not in graph
                if (!dist.containsKey(v)) continue;

                // New cost via u
                double newCost = dist.get(u) + flight.getCost();

                // Relax if better path found
                if (newCost < dist.get(v)) 
                {
                    dist.put(v, newCost);
                    parent.put(v, u);
                    // Track duration along cheapest path
                    durationMap.put(v, durationMap.get(u) + flight.getDurationMinutes());
                    // Insert updated entry into heap — O(log V)
                    minHeap.offer(new Object[]{v, newCost});
                }
            }
        }

        // ── Step 5: Reconstruct path ──
        return buildRoute(source, destination, dist, parent, durationMap, "Cheapest");
    }

    /*
      Reconstructs the route path from the parent map.
      Uses a Stack (via Collections.reverse) for path reversal.
     */
    private Route buildRoute(String source, String destination,
                             Map<String, Double> dist,
                             Map<String, String> parent,
                             Map<String, Integer> durationMap,
                             String mode) {

        // No path found
        if (dist.get(destination) == null || dist.get(destination) == Double.MAX_VALUE) 
        {
            return null; // No route exists
        }

        // Trace back from destination to source using parent map
        List<String> path = new ArrayList<>();
        String current = destination;

        while (current != null) {
            path.add(current);
            current = parent.get(current);
        }

        // Path was built in reverse — flip it
        Collections.reverse(path);

        // Verify path starts at source
        if (path.isEmpty() || !path.get(0).equals(source)) {
            return null;
        }

        // Build and return the Route object
        double totalCost = dist.get(destination);
        int totalDuration = durationMap.get(destination);

        return new Route(path, totalCost, totalDuration, mode);
    }

    /**
     * Returns the number of nodes explored during the last algorithm run.
     * Used for experimental evaluation / benchmarking.
     */
    public int getNodesExplored() {
        return nodesExplored;
    }
}
