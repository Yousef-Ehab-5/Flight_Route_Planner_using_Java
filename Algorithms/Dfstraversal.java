package algorithms;

import graph.FlightGraph;
import model.Flight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * DFSTraversal.java
 * 
 * Implements Depth-First Search for:
 *   1. Full graph traversal (visit all reachable airports)
 *   2. Connectivity checking (is destination reachable from source?)
 *   3. Finding ALL possible paths from source to destination
 * 
 * ── Why DFS? ──
 *   DFS is ideal for:
 *   - Checking connectivity in the graph
 *   - Enumerating all paths (useful for debugging & analysis)
 *   - It uses a Stack data structure (either call stack or explicit Stack)
 * 
 * ── Pseudocode (Iterative DFS) ──
 *   1. Push source onto stack
 *   2. While stack is not empty:
 *       a. Pop node u
 *       b. If u is unvisited:
 *           Mark visited
 *           For each neighbor v:
 *               Push v onto stack
 * 
 * ── Complexity ──
 *   Time:  O(V + E)
 *   Space: O(V) — for visited map and stack
 */
public class Dfstraversal {

    // Tracks nodes explored count for benchmarking
    private int nodesExplored;

    /**
     * Performs a full DFS traversal starting from a source airport.
     * Returns all airports reachable from the source in DFS order.
     * 
     * Uses an EXPLICIT Stack (not recursion) to avoid stack overflow
     * on large graphs.
     * 
     * @param graph  The flight graph
     * @param source Starting airport code
     * @return List of airport codes in DFS visit order
     */
    public List<String> traverseFromSource(FlightGraph graph, String source) {

        nodesExplored = 0;

        // List to record the DFS visit order
        List<String> visitOrder = new ArrayList<>();

        // Visited map: prevents revisiting
        Map<String, Boolean> visited = new HashMap<>();
        for (String code : graph.getAllAirportCodes()) {
            visited.put(code, false);
        }

        // ── Stack data structure for DFS ──
        // Java's Stack class (extends Vector → LIFO)
        Stack<String> stack = new Stack<>();

        // Push source
        stack.push(source);

        // ── DFS Main Loop ──
        while (!stack.isEmpty()) {

            // Pop top element — O(1)
            String u = stack.pop();

            // Skip if already visited (lazy duplicate handling)
            if (visited.containsKey(u) && visited.get(u)) {
                continue;
            }

            // Mark as visited
            visited.put(u, true);
            visitOrder.add(u);
            nodesExplored++;

            // Push all unvisited neighbors onto stack
            // Push in reverse order to maintain alphabetical traversal
            LinkedList<Flight> flights = graph.getFlightsFrom(u);
            List<Flight> flightList = new ArrayList<>(flights);

            // Push in reverse so first neighbor is on top
            for (int i = flightList.size() - 1; i >= 0; i--) {
                String v = flightList.get(i).getDestination();
                if (visited.containsKey(v) && !visited.get(v)) {
                    stack.push(v);
                }
            }
        }

        return visitOrder;
    }

    /**
     * Checks if the destination is reachable from source using DFS.
     * 
     * @param graph       The flight graph
     * @param source      Source airport code
     * @param destination Destination airport code
     * @return true if a path exists, false otherwise
     */
    public boolean isReachable(FlightGraph graph, String source, String destination) {
        List<String> visited = traverseFromSource(graph, source);
        return visited.contains(destination);
    }

    /**
     * Finds ALL paths from source to destination using recursive DFS.
     * 
     * ⚠️ WARNING: Exponential in worst case. Use only for small graphs
     * or for educational demonstration.
     * 
     * @param graph       The flight graph
     * @param source      Source airport code
     * @param destination Destination airport code
     * @param maxDepth    Maximum path length (to prevent infinite loops)
     * @return List of all paths (each path is a list of airport codes)
     */
    public List<List<String>> findAllPaths(FlightGraph graph, String source,
                                           String destination, int maxDepth) {

        List<List<String>> allPaths = new ArrayList<>();
        List<String> currentPath = new ArrayList<>();
        Map<String, Boolean> visitedInPath = new HashMap<>();

        currentPath.add(source);
        visitedInPath.put(source, true);

        dfsHelper(graph, source, destination, currentPath, visitedInPath, allPaths, maxDepth);

        return allPaths;
    }

    /**
     * Recursive DFS helper for finding all paths.
     */
    private void dfsHelper(FlightGraph graph, String current, String destination,
                            List<String> currentPath, Map<String, Boolean> visitedInPath,
                            List<List<String>> allPaths, int maxDepth) {

        // Base case: reached destination
        if (current.equals(destination)) {
            allPaths.add(new ArrayList<>(currentPath));
            return;
        }

        // Depth limit to prevent excessively long paths
        if (currentPath.size() >= maxDepth) {
            return;
        }

        // Explore neighbors
        for (Flight flight : graph.getFlightsFrom(current)) {
            String next = flight.getDestination();

            // Avoid cycles in current path
            if (visitedInPath.containsKey(next) && visitedInPath.get(next)) {
                continue;
            }

            // Go deeper
            currentPath.add(next);
            visitedInPath.put(next, true);

            dfsHelper(graph, next, destination, currentPath, visitedInPath, allPaths, maxDepth);

            // Backtrack
            currentPath.remove(currentPath.size() - 1);
            visitedInPath.put(next, false);
        }
    }

    /**
     * Returns nodes explored count for benchmarking.
     */
    public int getNodesExplored() {
        return nodesExplored;
    }
}