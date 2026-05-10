package application;

import algorithms.*;
import graph.FlightGraph;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.Airport;
import model.Flight;
import model.Route;
import utils.Csvreader;
import java.util.List;
import java.util.function.Consumer;

/**
 * RouteFinderPanel.java
 *
 * The main route search interface.
 * Allows users to:
 *   - Select source and destination airports
 *   - Choose an optimization mode (Cheapest / Fastest / Fewest Layovers )
 *   - Run the corresponding algorithm
 *   - View detailed route results
 *   - Compare multiple optimization modes side-by-side
 *   - Save route to history
 *   - View route history
 *   - Run DFS connectivity check
 */
public class Routefinderpanel {

    private FlightGraph graph = new FlightGraph();
    private Consumer<String> statusCallback = null;

    // Algorithm instances
    private final DijkstraCheapest    dijkstraCheapest = new DijkstraCheapest();
    private final Dijkstrafastest     dijkstraFastest  = new Dijkstrafastest();
    private final Bfslayover          bfsLayover       = new Bfslayover();
 
    

    // Input controls
    private ComboBox<String> sourceBox;
    private ComboBox<String> destBox;
    private ComboBox<String> modeBox;
    // Result area
    private TextArea resultArea;
   
    

    // Comparison table
    private TableView<String[]> compTable;
    private ObservableList<String[]> compData;

    // Stores the last computed route (for save-to-history)
    private Route lastRoute = null;

    // ── Light Theme Palette ──────────────────────
    private static final String BG_MAIN      = "#f0f4ff";
    private static final String BG_PANEL     = "#ffffff";
    private static final String BG_INPUT     = "#f1f5f9";
    private static final String BG_TEXTAREA  = "#f8fafc";
    private static final String ACCENT       = "#4f46e5";
    private static final String ACCENT_BLUE  = "#3b82f6";
    private static final String ACCENT_SKY   = "#0369a1";
    private static final String TEXT_MAIN    = "#1e293b";
    private static final String TEXT_LABEL   = "#475569";
    private static final String BORDER_COLOR = "#c7d2fe";
    private static final String BORDER_INPUT = "#93c5fd";
    // ────────────────────────────────────────────

    public Routefinderpanel(FlightGraph graph, Consumer<String> statusCallback) {
        this.graph = graph;
        this.statusCallback = statusCallback;
    }

    /**
     * Builds and returns the route finder panel.
     */
    public ScrollPane buildPanel() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: " + BG_MAIN + ";");

        Label title = new Label("🗺  Route Finder");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.web(ACCENT));

        // Search form
        VBox searchForm = buildSearchForm();

        // Results
        TitledPane resultsPane = buildResultsPane();

        // Comparison section
        TitledPane compPane = buildComparisonPane();

        

        

        root.getChildren().addAll(title, searchForm, resultsPane, compPane);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: " + BG_MAIN + "; -fx-background: " + BG_MAIN + ";");
        return scroll;
    }

    // ════════════════════════════════════════════
    //   FORM BUILDERS
    // ════════════════════════════════════════════

    private VBox buildSearchForm() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color: " + BG_PANEL + "; " +
                     "-fx-background-radius: 8; " +
                     "-fx-border-color: " + BORDER_COLOR + "; " +
                     "-fx-border-radius: 8; -fx-border-width: 1;");

        // Airport selection row
        HBox airportRow = new HBox(16);
        airportRow.setAlignment(Pos.CENTER_LEFT);

        List<Airport> airports = graph.getAllAirports();
        ObservableList<String> codes = FXCollections.observableArrayList();
        airports.stream().map(a -> a.getCode() + " - " + a.getCity()).sorted().forEach(codes::add);

        sourceBox = new ComboBox<>(codes);
        sourceBox.setPromptText("Select departure airport");
        sourceBox.setPrefWidth(280);
        sourceBox.setStyle(getComboStyle());

        destBox = new ComboBox<>(codes);
        destBox.setPromptText("Select arrival airport");
        destBox.setPrefWidth(280);
        destBox.setStyle(getComboStyle());

        Label fromLabel = styledLabel("From:");
        Label toLabel   = styledLabel("To:");

        airportRow.getChildren().addAll(fromLabel, sourceBox, toLabel, destBox);

        // Mode selection row
        HBox modeRow = new HBox(16);
        modeRow.setAlignment(Pos.CENTER_LEFT);

        modeBox = new ComboBox<>();
        modeBox.getItems().addAll(
            "🏷  Cheapest Route (Dijkstra by Cost)",
            "⚡  Fastest Route (Dijkstra by Duration)",
            "🔁  Fewest Layovers (BFS)" );
        
        modeBox.setValue("🏷  Cheapest Route (Dijkstra by Cost)");
        modeBox.setStyle(getComboStyle());
        modeBox.setPrefWidth(320);

        

        modeRow.getChildren().addAll(styledLabel("Optimization:"), modeBox);

        // Find route button
        Button findBtn = new Button("✈  Find Optimal Route");
        findBtn.setStyle("-fx-background-color: " + ACCENT + "; " +
                         "-fx-text-fill: white; " +
                         "-fx-font-size: 14; -fx-font-weight: bold; " +
                         "-fx-background-radius: 6; -fx-cursor: hand; " +
                         "-fx-padding: 10 24 10 24;");
        findBtn.setOnAction(e -> findRoute());

        Button compareBtn = new Button("📊 Compare All Modes");
        compareBtn.setStyle("-fx-background-color: " + ACCENT_BLUE + "; " +
                            "-fx-text-fill: white; -fx-font-weight: bold; " +
                            "-fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 10 18 10 18;");
        compareBtn.setOnAction(e -> compareAllModes());

        HBox btnRow = new HBox(12, findBtn, compareBtn);
        btnRow.setAlignment(Pos.CENTER_LEFT);

        box.getChildren().addAll(airportRow, modeRow, btnRow);
        return box;
    }

    

    private TitledPane buildResultsPane() {
        resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setPrefHeight(200);
        resultArea.setStyle("-fx-control-inner-background: " + BG_TEXTAREA + "; " +
                            "-fx-text-fill: " + ACCENT_SKY + "; " +
                            "-fx-font-family: 'Courier New'; -fx-font-size: 12; " +
                            "-fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 4;");
        resultArea.setText("Route results will appear here after searching...\n\n" +
                           "Select source, destination, and optimization mode, then click 'Find Optimal Route'.");

        Button saveHistBtn = styledButton("💾 Save to History", ACCENT_BLUE);
        saveHistBtn.setOnAction(e -> saveLastRoute());

        VBox content = new VBox(8, resultArea, saveHistBtn);
        content.setPadding(new Insets(10));
        content.setStyle("-fx-background-color: " + BG_PANEL + ";");

        TitledPane pane = new TitledPane("📋  Route Results", content);
        pane.setExpanded(true);
        return pane;
    }

    
    
    @SuppressWarnings("unchecked")
    private TitledPane buildComparisonPane() {
        compTable = new TableView<>();
        compTable.setPrefHeight(160);
        compTable.setStyle("-fx-background-color: " + BG_PANEL + "; " +
                           "-fx-control-inner-background: " + BG_PANEL + "; " +
                           "-fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 5;");
        compTable.setPlaceholder(new Label("Click 'Compare All Modes' to see comparison."));

        TableColumn<String[], String> modeCol = new TableColumn<>("Mode");
        modeCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[0]));

        TableColumn<String[], String> pathCol = new TableColumn<>("Path");
        pathCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[1]));

        TableColumn<String[], String> costCol = new TableColumn<>("Cost");
        costCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[2]));
        costCol.setPrefWidth(80);

        TableColumn<String[], String> durCol = new TableColumn<>("Duration");
        durCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[3]));
        durCol.setPrefWidth(85);

        TableColumn<String[], String> layoverCol = new TableColumn<>("Layovers");
        layoverCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[4]));
        layoverCol.setPrefWidth(70);

        TableColumn<String[], String> nodesCol = new TableColumn<>("Nodes Explored");
        nodesCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue()[5]));
        nodesCol.setPrefWidth(110);

        compData = FXCollections.observableArrayList();
        compTable.setItems(compData);
        compTable.getColumns().addAll(modeCol, pathCol, costCol, durCol, layoverCol, nodesCol);

        VBox content = new VBox(compTable);
        content.setPadding(new Insets(10));
        content.setStyle("-fx-background-color: " + BG_PANEL + ";");

        TitledPane pane = new TitledPane("📊  Algorithm Comparison", content);
        pane.setExpanded(false);
        return pane;
    }

    

    

    // ════════════════════════════════════════════
    //   ALGORITHM ACTIONS
    // ════════════════════════════════════════════

    private void findRoute() {
        String srcEntry = sourceBox.getValue();
        String dstEntry = destBox.getValue();

        if (srcEntry == null || dstEntry == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Input", "Please select both source and destination airports.");
            return;
        }

        // Extract just the IATA code (before " - ")
        String src = srcEntry.split(" - ")[0].trim();
        String dst = dstEntry.split(" - ")[0].trim();

        if (src.equals(dst)) {
            showAlert(Alert.AlertType.WARNING, "Same Airport", "Source and destination must be different airports.");
            return;
        }

        String mode = modeBox.getValue();
        long startTime = System.nanoTime();
        Route route = null;

        if (mode.contains("Cheapest")) {
            route = dijkstraCheapest.findCheapestRoute(graph, src, dst);

        } else if (mode.contains("Fastest")) {
            route = dijkstraFastest.findFastestRoute(graph, src, dst);

        } else if (mode.contains("Fewest")) {
            route = bfsLayover.findMinLayoverRoute(graph, src, dst);

        } 

        long elapsed = System.nanoTime() - startTime;
        double elapsedMs = elapsed / 1_000_000.0;

        if (route == null || !route.isValid()) {
            resultArea.setText("❌ No route found between " + src + " and " + dst + ".\n\n" +
                               "Possible reasons:\n" +
                               "  - No direct or connecting flights\n" +
                               "  - Airports are not connected in the graph\n\n" +
                               "Use DFS Connectivity Check to verify.");
            statusCallback.accept("No route found: " + src + " → " + dst);
            return;
        }

        lastRoute = route;

        // Format result
        StringBuilder sb = new StringBuilder();
        sb.append("✅ ROUTE FOUND\n");
        sb.append("═".repeat(50)).append("\n\n");
        sb.append("  Optimization: ").append(route.getOptimizationMode()).append("\n");
        sb.append("  Path:         ").append(route.getPathString()).append("\n");
        sb.append("  Total Cost:   $").append(String.format("%.2f", route.getTotalCost())).append("\n");
        sb.append("  Duration:     ").append(route.getFormattedDuration()).append("\n");
        sb.append("  Layovers:     ").append(route.getLayovers()).append("\n");
        sb.append("  Airports:     ").append(route.getPath().size()).append(" stops\n");
        sb.append("\n");

        // Per-leg breakdown
        sb.append("  Flight Details:\n");
        List<String> path = route.getPath();
        for (int i = 0; i < path.size() - 1; i++) {
            String from = path.get(i);
            String to   = path.get(i + 1);
            Flight leg  = findFlight(from, to);
            if (leg != null) {
                sb.append(String.format("    %d. %s → %s | $%.0f | %s | %s%n",
                    i + 1, from, to, leg.getCost(), leg.getFormattedDuration(), leg.getAirline()));
            } else {
                sb.append(String.format("    %d. %s → %s%n", i + 1, from, to));
            }
        }

        sb.append("\n  Algorithm runtime: ").append(String.format("%.3f ms", elapsedMs));

        resultArea.setText(sb.toString());
        statusCallback.accept("Route found: " + src + " → " + dst + " | $" +
            String.format("%.0f", route.getTotalCost()) + " | " + route.getFormattedDuration());
    }

    private void compareAllModes() {
        String srcEntry = sourceBox.getValue();
        String dstEntry = destBox.getValue();

        if (srcEntry == null || dstEntry == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Input", "Please select source and destination airports.");
            return;
        }

        String src = srcEntry.split(" - ")[0].trim();
        String dst = dstEntry.split(" - ")[0].trim();

        compData.clear();

        // Run all 3 algorithms
        Object[][] modes = {
            {"Cheapest",      dijkstraCheapest.findCheapestRoute(graph, src, dst),     dijkstraCheapest.getNodesExplored()},
            {"Fastest",       dijkstraFastest.findFastestRoute(graph, src, dst),        dijkstraFastest.getNodesExplored()},
            {"Min Layovers",  bfsLayover.findMinLayoverRoute(graph, src, dst),          bfsLayover.getNodesExplored()},
            
        };

        for (Object[] entry : modes) {
            String name   = (String) entry[0];
            Route  route  = (Route)  entry[1];
            int    nodes  = (int)    entry[2];

            if (route != null && route.isValid()) {
                compData.add(new String[]{
                    name,
                    route.getPathString(),
                    "$" + String.format("%.0f", route.getTotalCost()),
                    route.getFormattedDuration(),
                    String.valueOf(route.getLayovers()),
                    String.valueOf(nodes)
                });
            } else {
                compData.add(new String[]{name, "No route found", "-", "-", "-", "-"});
            }
        }

        statusCallback.accept("Comparison complete for " + src + " → " + dst);
    }
 

    

    private void saveLastRoute() {
        if (lastRoute == null) {
            showAlert(Alert.AlertType.WARNING, "No Route", "Run a route search first.");
            return;
        }
        Csvreader.appendRouteHistory(lastRoute, Csvreader.HISTORY_FILE);
        showAlert(Alert.AlertType.INFORMATION, "Saved", "Route saved to history.");
        statusCallback.accept("Route saved to history.");
    }

    

    // ════════════════════════════════════════════
    //   HELPERS
    // ════════════════════════════════════════════

    /**
     * Finds a specific direct flight between two airports.
     */
    private Flight findFlight(String src, String dst) {
        for (Flight f : graph.getFlightsFrom(src)) {
            if (f.getDestination().equals(dst)) return f;
        }
        return null;
    }

    private Label styledLabel(String text) {
        Label l = new Label(text);
        l.setTextFill(Color.web(TEXT_LABEL));
        l.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        return l;
    }

    private Button styledButton(String text, String bgColor) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + bgColor + "; " +
                     "-fx-text-fill: white; " +
                     "-fx-background-radius: 5; -fx-cursor: hand; " +
                     "-fx-padding: 6 14 6 14; -fx-font-weight: bold;");
        return btn;
    }

    private String getComboStyle() {
        return "-fx-background-color: " + BG_INPUT + "; " +
               "-fx-text-fill: " + TEXT_MAIN + "; " +
               "-fx-border-color: " + BORDER_INPUT + "; -fx-border-radius: 4;";
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
