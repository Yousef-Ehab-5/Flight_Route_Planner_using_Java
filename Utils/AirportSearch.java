package utils;

import model.Airport;

import java.util.ArrayList;
import java.util.List;

/**
 * AirportSearch.java
 * 
 * Provides searching utilities for a list of Airport objects.
 * Supports:
 *   - Search by city name
 *   - Search by country name
 *   - Search by airport code (exact match)
 *   - Search by keyword (matches code, name, city, or country)
 * 
 * ── Search Algorithm: Linear Search ──
 *   Why linear search?
 *   - Airport lists are typically small (< 500 entries)
 *   - Case-insensitive matching is required (harder with binary search)
 *   - Binary search requires sorted list + exact matching
 * 
 * ── Complexity ──
 *   All search methods: O(n) where n = number of airports
 *   Space: O(k) where k = number of matching results
 * 
 * For very large datasets (> 10,000 airports), a Trie or inverted
 * index would be more efficient, but is beyond this course scope.
 */
public class AirportSearch {

    /**
     * Searches airports by city name (case-insensitive, partial match).
     * 
     * @param airports   List of all airports
     * @param cityQuery  City name to search for
     * @return List of airports in the matching city
     */
    public static List<Airport> searchByCity(List<Airport> airports, String cityQuery) {
        List<Airport> results = new ArrayList<>();

        if (cityQuery == null || cityQuery.trim().isEmpty()) {
            return results;
        }

        String query = cityQuery.trim().toLowerCase();

        // Linear scan — O(n)
        for (Airport airport : airports) {
            if (airport.getCity().toLowerCase().contains(query)) {
                results.add(airport);
            }
        }

        return results;
    }

    /**
     * Searches airports by country name (case-insensitive, partial match).
     * 
     * @param airports      List of all airports
     * @param countryQuery  Country name to search for
     * @return List of airports in the matching country
     */
    public static List<Airport> searchByCountry(List<Airport> airports, String countryQuery) {
        List<Airport> results = new ArrayList<>();

        if (countryQuery == null || countryQuery.trim().isEmpty()) {
            return results;
        }

        String query = countryQuery.trim().toLowerCase();

        for (Airport airport : airports) {
            if (airport.getCountry().toLowerCase().contains(query)) {
                results.add(airport);
            }
        }

        return results;
    }

    /**
     * Searches airports by IATA code (exact, case-insensitive match).
     * 
     * @param airports  List of all airports
     * @param code      IATA code to find
     * @return The matching Airport, or null if not found
     */
    public static Airport searchByCode(List<Airport> airports, String code) {
        if (code == null || code.trim().isEmpty()) return null;

        String query = code.trim().toUpperCase();

        for (Airport airport : airports) {
            if (airport.getCode().equalsIgnoreCase(query)) {
                return airport;
            }
        }

        return null;
    }

    /**
     * Searches airports by any keyword across all fields:
     * code, name, city, and country.
     * 
     * @param airports List of all airports
     * @param keyword  Search keyword
     * @return List of airports matching the keyword in any field
     */
    public static List<Airport> searchByKeyword(List<Airport> airports, String keyword) {
        List<Airport> results = new ArrayList<>();

        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>(airports); // Return all if no keyword
        }

        String query = keyword.trim().toLowerCase();

        for (Airport airport : airports) {
            boolean matches = airport.getCode().toLowerCase().contains(query)
                           || airport.getName().toLowerCase().contains(query)
                           || airport.getCity().toLowerCase().contains(query)
                           || airport.getCountry().toLowerCase().contains(query);

            if (matches) {
                results.add(airport);
            }
        }

        return results;
    }
}
