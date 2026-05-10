package utils;

import model.Airport;
import model.Flight;
import model.Route;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CSVReader.java
 * 
 * Handles all file I/O for the Flight Route Planner.
 * Supports reading and writing:
 *   - airports.csv
 *   - flights.csv
 *   - route_history.csv
 * 
 * ── CSV Format ──
 *
 * airports.csv:
 *   code,name,city,country
 *   CAI,Cairo International Airport,Cairo,Egypt
 *
 * flights.csv:
 *   source,destination,cost,duration,airline
 *   CAI,DXB,350,200,EgyptAir
 *
 * route_history.csv:
 *   path,cost,duration,layovers,mode
 *   CAI→DXB→LHR,850,545,1,Cheapest
 * 
 * ── Error Handling ──
 *   - Skips malformed lines
 *   - Reports line number on error
 *   - Returns empty list on file not found
 */
public class Csvreader {

    // Default dataset paths
    public static final String AIRPORTS_FILE = "D:\\eclipse\\FlightRoutePlannerV1\\src\\utils/airports.csv";
    public static final String FLIGHTS_FILE  = "D:\\eclipse\\FlightRoutePlannerV1\\src\\utils/flights.csv";
    public static final String HISTORY_FILE  = "dataset/route_history.csv";

    // ════════════════════════════════════════════
    //   READING
    // ════════════════════════════════════════════

    /**
     * Reads airport data from a CSV file.
     * Expected format per line: code,name,city,country
     * 
     * @param filePath Path to airports CSV file
     * @return List of Airport objects
     */
    public static List<Airport> readAirports(String filePath) {
        List<Airport> airports = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNum = 0;

            while ((line = br.readLine()) != null) {
                lineNum++;

                // Skip header line and blank lines
                if (lineNum == 1 || line.trim().isEmpty()) continue;

                String[] parts = line.split(",");

                // Validate column count
                if (parts.length < 4) {
                    System.err.println("[CSVReader] Skipping malformed line " + lineNum + ": " + line);
                    continue;
                }

                String code    = parts[0].trim();
                String name    = parts[1].trim();
                String city    = parts[2].trim();
                String country = parts[3].trim();

                airports.add(new Airport(code, name, city, country));
            }

        } catch (FileNotFoundException e) {
            System.err.println("[CSVReader] File not found: " + filePath);
        } catch (IOException e) {
            System.err.println("[CSVReader] Error reading file: " + e.getMessage());
        }

        System.out.println("[CSVReader] Loaded " + airports.size() + " airports from " + filePath);
        return airports;
    }

    /**
     * Reads flight data from a CSV file.
     * Expected format: source,destination,cost,duration,airline
     * 
     * @param filePath Path to flights CSV file
     * @return List of Flight objects
     */
    public static List<Flight> readFlights(String filePath) {
        List<Flight> flights = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNum = 0;

            while ((line = br.readLine()) != null) {
                lineNum++;

                if (lineNum == 1 || line.trim().isEmpty()) continue;

                String[] parts = line.split(",");

                if (parts.length < 4) {
                    System.err.println("[CSVReader] Skipping malformed line " + lineNum + ": " + line);
                    continue;
                }

                try {
                    String source      = parts[0].trim();
                    String destination = parts[1].trim();
                    double cost        = Double.parseDouble(parts[2].trim());
                    int duration       = Integer.parseInt(parts[3].trim());
                    String airline     = (parts.length >= 5) ? parts[4].trim() : "Unknown";

                    flights.add(new Flight(source, destination, cost, duration, airline));

                } catch (NumberFormatException e) {
                    System.err.println("[CSVReader] Invalid number on line " + lineNum + ": " + line);
                }
            }

        } catch (FileNotFoundException e) {
            System.err.println("[CSVReader] File not found: " + filePath);
        } catch (IOException e) {
            System.err.println("[CSVReader] Error reading file: " + e.getMessage());
        }

        System.out.println("[CSVReader] Loaded " + flights.size() + " flights from " + filePath);
        return flights;
    }

    // ════════════════════════════════════════════
    //   WRITING
    // ════════════════════════════════════════════

    /**
     * Saves a list of airports to a CSV file.
     * 
     * @param airports List of airports to save
     * @param filePath Target file path
     */
    public static void saveAirports(List<Airport> airports, String filePath) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            pw.println("code,name,city,country");

            for (Airport a : airports) {
                pw.printf("%s,%s,%s,%s%n",
                    a.getCode(), a.getName(), a.getCity(), a.getCountry());
            }

            System.out.println("[CSVReader] Saved " + airports.size() + " airports to " + filePath);

        } catch (IOException e) {
            System.err.println("[CSVReader] Error saving airports: " + e.getMessage());
        }
    }

    /**
     * Saves a list of flights to a CSV file.
     * 
     * @param flights  List of flights to save
     * @param filePath Target file path
     */
    public static void saveFlights(List<Flight> flights, String filePath) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            pw.println("source,destination,cost,duration,airline");

            for (Flight f : flights) {
                pw.printf("%s,%s,%.0f,%d,%s%n",
                    f.getSource(), f.getDestination(),
                    f.getCost(), f.getDurationMinutes(), f.getAirline());
            }

            System.out.println("[CSVReader] Saved " + flights.size() + " flights to " + filePath);

        } catch (IOException e) {
            System.err.println("[CSVReader] Error saving flights: " + e.getMessage());
        }
    }

    /**
     * Appends a route to the history file.
     * 
     * @param route    Route to record
     * @param filePath History file path
     */
    public static void appendRouteHistory(Route route, String filePath) {
        try {
            // Create file with header if it doesn't exist
            File file = new File(filePath);
            boolean newFile = !file.exists();

            try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))) {
                if (newFile) {
                    pw.println("path,cost,duration_min,layovers,mode");
                }

                pw.printf("%s,%.0f,%d,%d,%s%n",
                    route.getPathString(),
                    route.getTotalCost(),
                    route.getTotalDurationMinutes(),
                    route.getLayovers(),
                    route.getOptimizationMode());
            }

        } catch (IOException e) {
            System.err.println("[CSVReader] Error appending route history: " + e.getMessage());
        }
    }

    /**
     * Reads route history from file.
     * Returns lines as raw strings for display.
     * 
     * @param filePath History file path
     * @return List of history line strings
     */
    public static List<String> readRouteHistory(String filePath) {
        List<String> history = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNum = 0;

            while ((line = br.readLine()) != null) {
                lineNum++;
                if (lineNum == 1) continue; // skip header
                if (!line.trim().isEmpty()) {
                    history.add(line.trim());
                }
            }

        } catch (FileNotFoundException e) {
            // History file may not exist yet — that's OK
        } catch (IOException e) {
            System.err.println("[CSVReader] Error reading history: " + e.getMessage());
        }

        return history;
    }
}
