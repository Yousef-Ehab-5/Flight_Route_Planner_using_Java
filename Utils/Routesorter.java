package utils;

import model.Route;

import java.util.List;

/**
 * RouteSorter.java
 * 
 * Provides sorting utilities for lists of Route objects.
 * Implements three sort criteria:
 *   1. Sort by total cost (ascending)
 *   2. Sort by total duration (ascending)
 *   3. Sort by number of layovers (ascending)
 * 
 * ── Sorting Algorithm: Insertion Sort ──
 *   Why Insertion Sort?
 *   - Educational: easy to understand and trace
 *   - Efficient for small lists (n < 20) — which is typical for route results
 *   - Stable: preserves original order for equal elements
 *   - In-place: O(1) extra space
 * 
 * ── Complexity ──
 *   Worst case:  O(n²) — when list is reverse-sorted
 *   Best case:   O(n)  — when list is already sorted
 *   Space:       O(1)  — in-place
 * 
 * Note: For large datasets, Collections.sort() (Timsort, O(n log n))
 * would be preferred, but Insertion Sort is used here for educational
 * demonstration.
 */
public class Routesorter {

    /**
     * Sorts routes by total cost in ascending order (cheapest first).
     * 
     * Algorithm: Insertion Sort
     * Time:  O(n²) worst case
     * Space: O(1)
     * 
     * @param routes List of routes to sort (sorted in-place)
     */
    public static void sortByCost(List<Route> routes) {
        int n = routes.size();

        // Insertion sort: build sorted portion from left to right
        for (int i = 1; i < n; i++) {
            Route key = routes.get(i);
            int j = i - 1;

            // Shift elements greater than key one position ahead
            while (j >= 0 && routes.get(j).getTotalCost() > key.getTotalCost()) {
                routes.set(j + 1, routes.get(j));
                j--;
            }

            // Insert key at its correct position
            routes.set(j + 1, key);
        }
    }

    /**
     * Sorts routes by total duration in ascending order (fastest first).
     * 
     * @param routes List of routes to sort (sorted in-place)
     */
    public static void sortByDuration(List<Route> routes) {
        int n = routes.size();

        for (int i = 1; i < n; i++) {
            Route key = routes.get(i);
            int j = i - 1;

            while (j >= 0 && routes.get(j).getTotalDurationMinutes() > key.getTotalDurationMinutes()) {
                routes.set(j + 1, routes.get(j));
                j--;
            }

            routes.set(j + 1, key);
        }
    }

    /**
     * Sorts routes by number of layovers in ascending order (fewest stops first).
     * 
     * @param routes List of routes to sort (sorted in-place)
     */
    public static void sortByLayovers(List<Route> routes) {
        int n = routes.size();

        for (int i = 1; i < n; i++) {
            Route key = routes.get(i);
            int j = i - 1;

            while (j >= 0 && routes.get(j).getLayovers() > key.getLayovers()) {
                routes.set(j + 1, routes.get(j));
                j--;
            }

            routes.set(j + 1, key);
        }
    }

    /**
     * Prints the sorted routes to console for debugging.
     * 
     * @param routes List of routes to display
     * @param label  Label for the sort criterion
     */
    public static void printSorted(List<Route> routes, String label) {
        System.out.println("\n=== Routes sorted by: " + label + " ===");
        for (int i = 0; i < routes.size(); i++) {
            Route r = routes.get(i);
            System.out.printf("%d. %s | $%.0f | %s | %d layovers%n",
                i + 1,
                r.getPathString(),
                r.getTotalCost(),
                r.getFormattedDuration(),
                r.getLayovers());
        }
    }
}
