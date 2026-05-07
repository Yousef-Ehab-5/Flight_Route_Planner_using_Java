package application;

import graph.FlightGraph;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.Airport;
import model.Flight;
import utils.Csvreader;

import java.util.List;

/**
 * MainDashboard.java
 * 
 * The main JavaFX application window.
 * Acts as the root controller that:
 *   1. Loads data from CSV files into the flight graph
 *   2. Hosts all panels in a TabPane
 *   3. Provides a navigation sidebar
 * 
 * GUI Structure:
 *   ┌─────────────────────────────────────────┐
 *   │           HEADER / TITLE BAR            │
 *   ├──────────┬──────────────────────────────┤
 *   │          │                              │
 *   │   Side   │       Tab Content            │
 *   │   Nav    │   (Airport / Flight /        │
 *   │          │    Route / Graph panels)     │
 *   │          │                              │
 *   ├──────────┴──────────────────────────────┤
 *   │              STATUS BAR                 │
 *   └─────────────────────────────────────────┘
 */
public class Maindashboard extends Application {

    // The shared flight graph — passed to all panels
    private FlightGraph graph;

    // Status bar label at the bottom
    private Label statusLabel;

    /**
     * JavaFX entry point.
     * Called by the JavaFX runtime after launch().
     */
    @Override
    public void start(Stage primaryStage) {

        // ── Step 1: Initialize the graph and load data ──
        graph = new FlightGraph();
        loadDataFromCSV();

        // ── Step 2: Build the UI ──
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #7efcee;");

        // Header
        VBox header = buildHeader();
        root.setTop(header);

        // Central content: TabPane with all panels
        TabPane tabPane = buildTabPane();
        root.setCenter(tabPane);

        // Status bar
        HBox statusBar = buildStatusBar();
        root.setBottom(statusBar);

        // ── Step 3: Configure and show the Scene ──
        Scene scene = new Scene(root, 1100, 720);
        primaryStage.setTitle("✈  Flight Route Planner — DSA Project");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();

        setStatus("System ready. Loaded " + graph.getAirportCount() + " airports and " +
                  graph.getFlightCount() + " flights.");
    }

    // ════════════════════════════════════════════
    //   DATA LOADING
    // ════════════════════════════════════════════

    /**
     * Loads airports and flights from CSV files into the graph.
     */
    private void loadDataFromCSV() {
        List<Airport> airports = Csvreader.readAirports(Csvreader.AIRPORTS_FILE);
        List<Flight> flights   = Csvreader.readFlights(Csvreader.FLIGHTS_FILE);

        for (Airport a : airports) {
            graph.addAirport(a);
        }

        int loaded = 0;
        for (Flight f : flights) {
            if (graph.addFlight(f)) loaded++;
        }

        System.out.println("[Dashboard] Graph initialized: " +
            graph.getAirportCount() + " airports, " + loaded + " flights.");
    }

    // ════════════════════════════════════════════
    //   UI BUILDERS
    // ════════════════════════════════════════════

    /**
     * Builds the top header bar with logo and title.
     */
    private VBox buildHeader() {
        VBox header = new VBox(4);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(16, 20, 12, 20));
        header.setStyle("-fx-background-color: #16213e; " +
                        "-fx-border-color: #0f3460; " +
                        "-fx-border-width: 0 0 2 0;");

        Label title = new Label("✈  Multi-Criteria Flight Route Planner");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#e94560"));

        Label subtitle = new Label("Data Structures & Algorithms Project  |  Graph Algorithms: Dijkstra · BFS · DFS");
        subtitle.setFont(Font.font("Arial", 12));
        subtitle.setTextFill(Color.web("#a8b2d8"));

        header.getChildren().addAll(title, subtitle);
        return header;
    }

    /**
     * Builds the main TabPane containing all functional panels.
     */
    private TabPane buildTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: #1a1a2e; " +
                         "-fx-tab-min-width: 140px;");

        // ── Tab 1: Home / Dashboard ──
        Tab homeTab = new Tab("🏠  Dashboard");
        homeTab.setContent(buildHomePage());

        // ── Tab 2: Airport Management ──
        Tab airportTab = new Tab("🛫  Airports");
        AirportPanel airportPanel = new AirportPanel(graph, this::setStatus);
        airportTab.setContent(airportPanel.buildPanel());

        // ── Tab 3: Flight Management ──
        Tab flightTab = new Tab("🛬  Flights");
        Flightpanel flightPanel = new Flightpanel(graph, this::setStatus);
        flightTab.setContent(flightPanel.buildPanel());

        // ── Tab 4: Route Finder ──
        Tab routeTab = new Tab("🗺  Find Route");
        Routefinderpanel routePanel = new Routefinderpanel(graph, this::setStatus);
        routeTab.setContent(routePanel.buildPanel());

        tabPane.getTabs().addAll(homeTab, airportTab, flightTab, routeTab);
        return tabPane;
    }

    /**
     * Builds the home dashboard summary page.
     */
    private ScrollPane buildHomePage() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: #1a1a2e;");

        // Welcome section
        Label welcome = new Label("Welcome to the Flight Route Planner");
        welcome.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        welcome.setTextFill(Color.web("#e94560"));

        Label desc = new Label(
            "This system models airline routes as a weighted directed graph and applies\n" +
            "graph algorithms to compute optimal travel routes under multiple criteria."
        );
        desc.setTextFill(Color.web("#ccd6f6"));
        desc.setFont(Font.font("Arial", 13));

        // Stats grid
        GridPane statsGrid = buildStatsGrid();

        // Algorithm summary
        TitledPane algoPane = buildAlgorithmSummary();

        // DSA summary
        TitledPane dsaPane = buildDSASummary();

        content.getChildren().addAll(welcome, desc, statsGrid, algoPane, dsaPane);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #1a1a2e; -fx-background: #1a1a2e;");
        return scroll;
    }

    /**
     * Builds the statistics summary grid on the home page.
     */
    private GridPane buildStatsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 0, 10, 0));

        String[][] stats = {
            {"✈  Airports",      String.valueOf(graph.getAirportCount())},
            {"🛬  Flights",       String.valueOf(graph.getFlightCount())},
            {"📊  Algorithms",   "4 (Dijkstra×2, BFS, DFS)"},
            {"💾  Data Structures", "Graph, PQ, HashMap, Stack, Queue"},
        };

        for (int i = 0; i < stats.length; i++) {
            VBox card = buildStatCard(stats[i][0], stats[i][1]);
            grid.add(card, i % 2, i / 2);
            GridPane.setHgrow(card, Priority.ALWAYS);
        }

        return grid;
    }

    /**
     * Builds a single statistic card.
     */
    private VBox buildStatCard(String label, String value) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(14));
        card.setStyle("-fx-background-color: #16213e; " +
                      "-fx-background-radius: 8; " +
                      "-fx-border-color: #0f3460; " +
                      "-fx-border-radius: 8; " +
                      "-fx-border-width: 1;");

        Label lbl = new Label(label);
        lbl.setTextFill(Color.web("#a8b2d8"));
        lbl.setFont(Font.font("Arial", 12));

        Label val = new Label(value);
        val.setTextFill(Color.web("#e94560"));
        val.setFont(Font.font("Arial", FontWeight.BOLD, 15));

        card.getChildren().addAll(lbl, val);
        return card;
    }

    /**
     * Builds the algorithm information pane.
     */
    private TitledPane buildAlgorithmSummary() {
        VBox content = new VBox(8);
        content.setPadding(new Insets(12));

        String[][] algos = {
            {"Dijkstra (Cheapest)",  "O((V+E) log V)", "Finds minimum-cost route using Priority Queue"},
            {"Dijkstra (Fastest)",   "O((V+E) log V)", "Finds minimum-duration route"},
            {"BFS (Min Layovers)",   "O(V+E)",         "Finds fewest-hops route using Queue"},
            {"DFS (Traversal)",      "O(V+E)",         "Explores graph, checks connectivity using Stack"},
            {"Balanced Route",       "O((V+E) log V)", "Composite score: costWeight×cost + timeWeight×time"},
        };

        for (String[] algo : algos) {
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);

            Label name = new Label(algo[0]);
            name.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            name.setTextFill(Color.web("#64ffda"));
            name.setMinWidth(180);

            Label complexity = new Label(algo[1]);
            complexity.setTextFill(Color.web("#e94560"));
            complexity.setFont(Font.font("Arial", 11));
            complexity.setMinWidth(130);

            Label desc = new Label(algo[2]);
            desc.setTextFill(Color.web("#ccd6f6"));
            desc.setFont(Font.font("Arial", 11));

            row.getChildren().addAll(name, complexity, desc);
            content.getChildren().add(row);
        }

        TitledPane pane = new TitledPane("📐  Algorithms & Complexity", content);
        pane.setStyle("-fx-text-fill: #0e0f0f;");
        pane.setExpanded(true);
        return pane;
    }

    /**
     * Builds the data structures summary pane.
     */
    private TitledPane buildDSASummary() {
        VBox content = new VBox(8);
        content.setPadding(new Insets(12));

        String[][] ds = {
            {"Graph (Adjacency List)", "Vertices=Airports, Edges=Flights", "O(V+E) space — efficient for sparse networks"},
            {"Priority Queue (Min-Heap)", "Used in Dijkstra", "O(log V) insert/extract — drives optimal path selection"},
            {"HashMap", "Airport lookup, dist/parent tracking", "O(1) average — fast key-based access"},
            {"LinkedList", "Adjacency lists, route storage", "O(1) append — ideal for dynamic edge lists"},
            {"Queue (LinkedList)", "BFS frontier", "O(1) enqueue/dequeue — level-by-level traversal"},
            {"Stack", "DFS traversal, path reconstruction", "O(1) push/pop — LIFO for depth-first search"},
        };

        for (String[] row : ds) {
            VBox card = new VBox(3);
            card.setPadding(new Insets(8));
            card.setStyle("-fx-background-color: #0f3460; -fx-background-radius: 5;");

            Label name = new Label(row[0]);
            name.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            name.setTextFill(Color.web("#64ffda"));

            Label usage = new Label("Used for: " + row[1]);
            usage.setTextFill(Color.web("#a8b2d8"));
            usage.setFont(Font.font("Arial", 11));

            Label why = new Label(row[2]);
            why.setTextFill(Color.web("#ccd6f6"));
            why.setFont(Font.font("Arial", 10));

            card.getChildren().addAll(name, usage, why);
            content.getChildren().add(card);
        }

        TitledPane pane = new TitledPane("🗂  Data Structures Used", content);
        pane.setExpanded(false);
        return pane;
    }

    /**
     * Builds the bottom status bar.
     */
    private HBox buildStatusBar() {
        HBox bar = new HBox();
        bar.setPadding(new Insets(6, 16, 6, 16));
        bar.setStyle("-fx-background-color: #16213e; " +
                     "-fx-border-color: #0f3460; " +
                     "-fx-border-width: 1 0 0 0;");

        statusLabel = new Label("Initializing...");
        statusLabel.setTextFill(Color.web("#64ffda"));
        statusLabel.setFont(Font.font("Arial", 11));

        bar.getChildren().add(statusLabel);
        return bar;
    }

    /**
     * Updates the status bar message.
     * Called as a callback from child panels.
     * 
     * @param message Status message to display
     */
    public void setStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText("● " + message);
        }
    }

    /**
     * Application entry point.
     * Launches the JavaFX application.
     */
    public static void main(String[] args) {
        launch(args);
    }
}