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
import utils.Routesorter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * RouteFinderPanel.java
 * 
 * The main route search interface.
 * Allows users to:
 *   - Select source and destination airports
 *   - Choose an optimization mode (Cheapest / Fastest / Fewest Layovers / Balanced)
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
    private final DijkstraCheapest dijkstraCheapest = new DijkstraCheapest();
    private final Dijkstrafastest  dijkstraFastest  = new Dijkstrafastest();
    private final Bfslayover       bfsLayover       = new Bfslayover();
    private final Dfstraversal     dfsTraversal     = new Dfstraversal();
    private final Balancedroutefinder balancedFinder = new Balancedroutefinder();

    // Input controls
    private ComboBox<String> sourceBox;
    private ComboBox<String> destBox;
    private ComboBox<String> modeBox;
    private Slider costWeightSlider;
    private Slider timeWeightSlider;
    private Label costWeightLabel;
    private Label timeWeightLabel;

    // Result area
    private TextArea resultArea;
    private TextArea historyArea;

    // Comparison table
    private TableView<String[]> compTable;
    private ObservableList<String[]> compData;

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
        root.setStyle("-fx-background-color: #1a1a2e;");

        Label title = new Label("🗺  Route Finder");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#e94560"));

        // Search form
        VBox searchForm = buildSearchForm();

        // Results
        TitledPane resultsPane = buildResultsPane();

        // Comparison section
        TitledPane compPane = buildComparisonPane();

        // History section
        TitledPane historyPane = buildHistoryPane();

        // DFS section
        TitledPane dfsPane = buildDFSPane();

        root.getChildren().addAll(title, searchForm, resultsPane, compPane, dfsPane, historyPane);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #1a1a2e; -fx-background: #1a1a2e;");
        return scroll;
    }

    // ════════════════════════════════════════════
    //   FORM BUILDERS
    // ════════════════════════════════════════════

    private VBox buildSearchForm() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color: #16213e; -fx-background-radius: 8; " +
                     "-fx-border-color: #0f3460; -fx-border-radius: 8;");

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
            "🔁  Fewest Layovers (BFS)",
            "⚖  Balanced Route (Custom Score)"
        );
        modeBox.setValue("🏷  Cheapest Route (Dijkstra by Cost)");
        modeBox.setStyle(getComboStyle());
        modeBox.setPrefWidth(320);

        // Show balanced options when mode changes
        VBox balancedOptions = buildBalancedWeightControls();
        balancedOptions.setVisible(false);
        balancedOptions.setManaged(false);

        modeBox.setOnAction(e -> {
            boolean isBalanced = modeBox.getValue().contains("Balanced");
            balancedOptions.setVisible(isBalanced);
            balancedOptions.setManaged(isBalanced);
        });

        modeRow.getChildren().addAll(styledLabel("Optimization:"), modeBox);

        // Find route button
        Button findBtn = new Button("✈  Find Optimal Route");
        findBtn.setStyle("-fx-background-color: #e94560; -fx-text-fill: white; " +
                         "-fx-font-size: 14; -fx-font-weight: bold; " +
                         "-fx-background-radius: 6; -fx-cursor: hand; " +
                         "-fx-padding: 10 24 10 24;");
        findBtn.setOnAction(e -> findRoute());

        Button compareBtn = new Button("📊 Compare All Modes");
        compareBtn.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; " +
                            "-fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 10 18 10 18;");
        compareBtn.setOnAction(e -> compareAllModes());

        HBox btnRow = new HBox(12, findBtn, compareBtn);
        btnRow.setAlignment(Pos.CENTER_LEFT);

        box.getChildren().addAll(airportRow, modeRow, balancedOptions, btnRow);
        return box;
    }

    private VBox buildBalancedWeightControls() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(8, 0, 4, 0));

        Label header = styledLabel("Balanced Route Weights:");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        costWeightSlider = new Slider(0, 1, 0.5);
        costWeightSlider.setBlockIncrement(0.1);
        costWeightSlider.setShowTickMarks(true);
        costWeightSlider.setPrefWidth(250);

        timeWeightSlider = new Slider(0, 1, 0.5);
        timeWeightSlider.setBlockIncrement(0.1);
        timeWeightSlider.setPrefWidth(250);

        costWeightLabel = new Label("Cost Weight: 50%");
        costWeightLabel.setTextFill(Color.web("#64ffda"));

        timeWeightLabel = new Label("Time Weight: 50%");
        timeWeightLabel.setTextFill(Color.web("#64ffda"));

        // Keep sliders in sync (they must sum to 1)
        costWeightSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double cw = newVal.doubleValue();
            double tw = 1.0 - cw;
            timeWeightSlider.setValue(tw);
            costWeightLabel.setText(String.format("Cost Weight: %.0f%%", cw * 100));
            timeWeightLabel.setText(String.format("Time Weight: %.0f%%", tw * 100));
        });

        HBox row1 = new HBox(12, costWeightLabel, costWeightSlider);
        row1.setAlignment(Pos.CENTER_LEFT);

        HBox row2 = new HBox(12, timeWeightLabel, timeWeightSlider);
        row2.setAlignment(Pos.CENTER_LEFT);

        box.getChildren().addAll(header, row1, row2);
        return box;
    }

    private TitledPane buildResultsPane() {
        resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setPrefHeight(200);
        resultArea.setStyle("-fx-control-inner-background: #0f1729; " +
                            "-fx-text-fill: #64ffda; " +
                            "-fx-font-family: 'Courier New'; -fx-font-size: 12;");
        resultArea.setText("Route results will appear here after searching...\n\n" +
                           "Select source, destination, and optimization mode, then click 'Find Optimal Route'.");

        Button saveHistBtn = styledButton("💾 Save to History", "#0f3460");
        saveHistBtn.setOnAction(e -> saveLastRoute());

        VBox content = new VBox(8, resultArea, saveHistBtn);
        content.setPadding(new Insets(10));
        content.setStyle("-fx-background-color: #16213e;");

        TitledPane pane = new TitledPane("📋  Route Results", content);
        pane.setExpanded(true);
        return pane;
    }

    @SuppressWarnings("unchecked")
    private TitledPane buildComparisonPane() {
        compTable = new TableView<>();
        compTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        compTable.setPrefHeight(160);
        compTable.setStyle("-fx-background-color: #16213e; -fx-control-inner-background: #16213e;");
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
        content.setStyle("-fx-background-color: #16213e;");

        TitledPane pane = new TitledPane("📊  Algorithm Comparison", content);
        pane.setExpanded(false);
        return pane;
    }

    private TitledPane buildDFSPane() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(12));
        content.setStyle("-fx-background-color: #16213e;");

        Label info = new Label("DFS traversal: check graph connectivity and reachability between airports.");
        info.setTextFill(Color.web("#a8b2d8"));
        info.setFont(Font.font("Arial", 11));

        Button dfsBtn = styledButton("🔍 Check Reachability (DFS)", "#27ae60");
        dfsBtn.setOnAction(e -> runDFS());

        Button traverseBtn = styledButton("🌐 Full DFS Traversal", "#555");
        traverseBtn.setOnAction(e -> runFullDFS());

        TextArea dfsOutput = new TextArea();
        dfsOutput.setEditable(false);
        dfsOutput.setPrefHeight(120);
        dfsOutput.setStyle("-fx-control-inner-background: #0f1729; -fx-text-fill: #a8ff78; " +
                           "-fx-font-family: 'Courier New'; -fx-font-size: 11;");
        dfsOutput.setText("DFS output will appear here...");

        // Store reference for update from action
        dfsOutputArea = dfsOutput;

        HBox btnRow = new HBox(10, dfsBtn, traverseBtn);
        content.getChildren().addAll(info, btnRow, dfsOutput);

        TitledPane pane = new TitledPane("🔍  DFS — Graph Connectivity", content);
        pane.setExpanded(false);
        return pane;
    }

    private TitledPane buildHistoryPane() {
        historyArea = new TextArea();
        historyArea.setEditable(false);
        historyArea.setPrefHeight(140);
        historyArea.setStyle("-fx-control-inner-background: #0f1729; -fx-text-fill: #ccd6f6; " +
                             "-fx-font-family: 'Courier New'; -fx-font-size: 11;");

        Button loadHistBtn = styledButton("📂 Load History", "#0f3460");
        loadHistBtn.setOnAction(e -> loadHistory());

        Button clearHistBtn = styledButton("✕ Clear Display", "#555");
        clearHistBtn.setOnAction(e -> historyArea.clear());

        HBox btnRow = new HBox(10, loadHistBtn, clearHistBtn);
        VBox content = new VBox(8, btnRow, historyArea);
        content.setPadding(new Insets(10));
        content.setStyle("-fx-background-color: #16213e;");

        TitledPane pane = new TitledPane("📜  Route History", content);
        pane.setExpanded(false);
        return pane;
    }

    // ════════════════════════════════════════════
    //   ALGORITHM ACTIONS
    // ════════════════════════════════════════════

    // Stores the last computed route (for save-to-history)
    private Route lastRoute = null;
    private TextArea dfsOutputArea;

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

        } else if (mode.contains("Balanced")) {
            double cw = costWeightSlider.getValue();
            double tw = 1.0 - cw;
            Balancedroutefinder bf = new Balancedroutefinder(cw, tw);
            route = bf.findBalancedRoute(graph, src, dst);
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

        // Run all four algorithms
        Object[][] modes = {
            {"Cheapest",      dijkstraCheapest.findCheapestRoute(graph, src, dst),      dijkstraCheapest.getNodesExplored()},
            {"Fastest",       dijkstraFastest.findFastestRoute(graph, src, dst),         dijkstraFastest.getNodesExplored()},
            {"Min Layovers",  bfsLayover.findMinLayoverRoute(graph, src, dst),           bfsLayover.getNodesExplored()},
            {"Balanced (50/50)", new Balancedroutefinder().findBalancedRoute(graph, src, dst), 0},
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

    private void runDFS() {
        String srcEntry = sourceBox.getValue();
        String dstEntry = destBox.getValue();

        if (srcEntry == null || dstEntry == null) {
            dfsOutputArea.setText("Please select source and destination first.");
            return;
        }

        String src = srcEntry.split(" - ")[0].trim();
        String dst = dstEntry.split(" - ")[0].trim();

        boolean reachable = dfsTraversal.isReachable(graph, src, dst);

        StringBuilder sb = new StringBuilder();
        sb.append("DFS Reachability Check\n");
        sb.append("══════════════════════\n");
        sb.append("Source:      ").append(src).append("\n");
        sb.append("Destination: ").append(dst).append("\n\n");

        if (reachable) {
            sb.append("✅ REACHABLE — A path exists from ").append(src).append(" to ").append(dst).append("\n");
        } else {
            sb.append("❌ NOT REACHABLE — No path from ").append(src).append(" to ").append(dst).append("\n");
        }

        sb.append("\nNodes explored (DFS from ").append(src).append("): ").append(dfsTraversal.getNodesExplored());

        dfsOutputArea.setText(sb.toString());
        statusCallback.accept("DFS check: " + src + " → " + dst + " = " + (reachable ? "Reachable" : "Not Reachable"));
    }

    private void runFullDFS() {
        String srcEntry = sourceBox.getValue();
        if (srcEntry == null) {
            dfsOutputArea.setText("Please select a source airport first.");
            return;
        }

        String src = srcEntry.split(" - ")[0].trim();
        List<String> visited = dfsTraversal.traverseFromSource(graph, src);

        StringBuilder sb = new StringBuilder();
        sb.append("DFS Full Traversal from ").append(src).append("\n");
        sb.append("══════════════════════════════════\n");
        sb.append("Visit order:\n");

        for (int i = 0; i < visited.size(); i++) {
            sb.append(String.format("  %2d. %s%n", i + 1, visited.get(i)));
        }

        sb.append("\nTotal airports reachable: ").append(visited.size());
        sb.append("\nTotal airports in graph: ").append(graph.getAirportCount());

        dfsOutputArea.setText(sb.toString());
        statusCallback.accept("DFS traversal from " + src + ": " + visited.size() + " airports reachable.");
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

    private void loadHistory() {
        List<String> history = Csvreader.readRouteHistory(Csvreader.HISTORY_FILE);
        if (history.isEmpty()) {
            historyArea.setText("No route history found.");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Route History\n═══════════════════════════════════════\n");
            for (String line : history) {
                sb.append(line).append("\n");
            }
            historyArea.setText(sb.toString());
        }
        statusCallback.accept("Loaded " + history.size() + " history entries.");
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
        l.setTextFill(Color.web("#a8b2d8"));
        l.setFont(Font.font("Arial", 12));
        return l;
    }

    private Button styledButton(String text, String bgColor) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: white; " +
                     "-fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 6 14 6 14;");
        return btn;
    }

    private String getComboStyle() {
        return "-fx-background-color: #0f3460; -fx-text-fill: #ccd6f6;";
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}