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
 * BalancedRouteFinder.java
 * 
 * Finds the optimal BALANCED route using a custom scoring function
 * that combines both cost and duration:
 * 
 *     score = (costWeight × normalizedCost) + (timeWeight × normalizedDuration)
 * 
 * ── Design Rationale ──
 *   A purely cheapest route might take 24 hours.
 *   A purely fastest route might cost $3000.
 *   The balanced route finds the sweet spot based on user-defined weights.
 * 
 * ── Normalization ──
 *   Since cost and duration use different units (USD vs minutes),
 *   we normalize both to [0, 1] range before combining:
 *     normalizedCost     = cost / maxCostInGraph
 *     normalizedDuration = duration / maxDurationInGraph
 * 
 * ── Scoring Function ──
 *   score(edge) = costWeight × (cost / maxCost) + timeWeight × (duration / maxDuration)
 * 
 *   Default weights: costWeight = 0.5, timeWeight = 0.5 (equal priority)
 *   User can adjust: e.g., costWeight = 0.7 for more cost-sensitive search
 * 
 * ── Algorithm ──
 *   Modified Dijkstra using the composite score as the edge weight.
 * 
 * ── Complexity ──
 *   Time:  O((V + E) log V)
 *   Space: O(V)
 */
public class Balancedroutefinder {

    // Weight assigned to cost (0.0 to 1.0)
    private double costWeight;

    // Weight assigned to travel time (0.0 to 1.0)
    private double timeWeight;

    // Maximum values in the graph (for normalization)
    private double maxCost;
    private double maxDuration;

    // Nodes explored for benchmarking
    private int nodesExplored;

    /**
     * Constructor with default equal weights (50% cost, 50% time).
     */
    public Balancedroutefinder() {
        this.costWeight = 0.5;
        this.timeWeight = 0.5;
    }

    /**
     * Constructor with custom weights.
     * Weights should sum to 1.0.
     * 
     * @param costWeight Weight for cost (e.g., 0.6 = 60% cost priority)
     * @param timeWeight Weight for duration (e.g., 0.4 = 40% time priority)
     */
    public Balancedroutefinder(double costWeight, double timeWeight) {
        // Normalize weights so they sum to 1
        double total = costWeight + timeWeight;
        this.costWeight = costWeight / total;
        this.timeWeight = timeWeight / total;
    }

    /**
     * Finds the balanced route from source to destination.
     * 
     * @param graph       The flight graph
     * @param source      Source airport IATA code
     * @param destination Destination airport IATA code
     * @return Optimally balanced Route
     */
    public Route findBalancedRoute(FlightGraph graph, String source, String destination) {

        nodesExplored = 0;

        // ── Step 1: Compute normalization values ──
        computeMaxValues(graph);

        // ── Step 2: Initialize maps ──
        Map<String, Double> score = new HashMap<>();   // composite score
        Map<String, String> parent = new HashMap<>();
        Map<String, Double> costMap = new HashMap<>();
        Map<String, Integer> durationMap = new HashMap<>();

        for (String code : graph.getAllAirportCodes()) {
            score.put(code, Double.MAX_VALUE);
            parent.put(code, null);
            costMap.put(code, 0.0);
            durationMap.put(code, 0);
        }

        score.put(source, 0.0);

        // ── Step 3: Min-Heap ordered by composite score ──
        PriorityQueue<Object[]> minHeap = new PriorityQueue<>(
            (a, b) -> Double.compare((double) a[1], (double) b[1])
        );

        minHeap.offer(new Object[]{source, 0.0});

        // ── Step 4: Modified Dijkstra with composite weight ──
        while (!minHeap.isEmpty()) {

            Object[] current = minHeap.poll();
            String u = (String) current[0];
            double scoreU = (double) current[1];

            nodesExplored++;

            // Lazy deletion
            if (scoreU > score.get(u)) continue;

            if (u.equals(destination)) break;

            LinkedList<Flight> flights = graph.getFlightsFrom(u);

            for (Flight flight : flights) {
                String v = flight.getDestination();
                if (!score.containsKey(v)) continue;

                // Compute normalized edge score
                double edgeScore = computeEdgeScore(flight.getCost(), flight.getDurationMinutes());

                double newScore = score.get(u) + edgeScore;

                if (newScore < score.get(v)) {
                    score.put(v, newScore);
                    parent.put(v, u);
                    costMap.put(v, costMap.get(u) + flight.getCost());
                    durationMap.put(v, durationMap.get(u) + flight.getDurationMinutes());
                    minHeap.offer(new Object[]{v, newScore});
                }
            }
        }

        // ── Step 5: Reconstruct path ──
        if (score.get(destination) == null || score.get(destination) == Double.MAX_VALUE) {
            return null;
        }

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

        double totalCost = costMap.get(destination);
        int totalDuration = durationMap.get(destination);

        String modeLabel = String.format("Balanced (Cost %.0f%% / Time %.0f%%)",
            costWeight * 100, timeWeight * 100);

        return new Route(path, totalCost, totalDuration, modeLabel);
    }

    /**
     * Computes the composite score for a single flight edge.
     * 
     * score = costWeight × (cost / maxCost) + timeWeight × (duration / maxDuration)
     */
    private double computeEdgeScore(double cost, int duration) {
        double normalizedCost = (maxCost > 0) ? cost / maxCost : 0;
        double normalizedTime = (maxDuration > 0) ? duration / maxDuration : 0;
        return (costWeight * normalizedCost) + (timeWeight * normalizedTime);
    }

    /**
     * Scans the graph to find the maximum cost and duration.
     * Used for normalization. Time: O(E)
     */
    private void computeMaxValues(FlightGraph graph) {
        maxCost = 1.0;     // Default to avoid division by zero
        maxDuration = 1.0;

        for (LinkedList<Flight> flights : graph.getAdjacencyList().values()) {
            for (Flight f : flights) {
                if (f.getCost() > maxCost) maxCost = f.getCost();
                if (f.getDurationMinutes() > maxDuration) maxDuration = f.getDurationMinutes();
            }
        }
    }

    // ── Getters / Setters ──

    public double getCostWeight() { return costWeight; }
    public double getTimeWeight() { return timeWeight; }

    public void setCostWeight(double costWeight) {
        double total = costWeight + timeWeight;
        this.costWeight = costWeight / total;
        this.timeWeight = timeWeight / total;
    }

    public int getNodesExplored() { return nodesExplored; }
}