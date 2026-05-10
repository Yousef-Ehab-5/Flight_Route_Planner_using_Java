package application;

import graph.FlightGraph;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
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
import utils.Csvreader;

import java.util.List;
import java.util.function.Consumer;

/**
 * FlightPanel.java
 *
 * JavaFX panel for Flight (Edge) Management.
 * Allows users to:
 *   - View all flights in a sortable table
 *   - Add new flights between existing airports
 *   - Remove selected flights
 *   - Filter flights by source or destination airport
 *   - Save/Load flight data to/from CSV
 */
public class Flightpanel {

    private final FlightGraph graph;
    private final Consumer<String> statusCallback;

    // Table
    private TableView<Flight> flightTable;
    private ObservableList<Flight> flightData;

    // Input fields
    private ComboBox<String> srcBox;
    private ComboBox<String> dstBox;
    private TextField costField;
    private TextField durationField;
    private TextField airlineField;

    // Filter
    private ComboBox<String> filterBox;
    private TextField filterField;

    // ── Light Theme Palette ──────────────────────
    private static final String BG_MAIN      = "#f0f4ff";
    private static final String BG_PANEL     = "#ffffff";
    private static final String BG_INPUT     = "#f1f5f9";
    private static final String ACCENT       = "#4f46e5";
    private static final String ACCENT_RED   = "#dc2626";
    private static final String ACCENT_BLUE  = "#3b82f6";
    private static final String ACCENT_SLATE = "#64748b";
    private static final String TEXT_MAIN    = "#1e293b";
    private static final String TEXT_LABEL   = "#475569";
    private static final String BORDER_COLOR = "#c7d2fe";
    private static final String BORDER_INPUT = "#93c5fd";
    private static final String PLACEHOLDER  = "#94a3b8";
    // ────────────────────────────────────────────

    public Flightpanel(FlightGraph graph, Consumer<String> statusCallback) {
        this.graph = graph;
        this.statusCallback = statusCallback;
    }

    /**
     * Builds and returns the flight management panel.
     */
    public ScrollPane buildPanel() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: " + BG_MAIN + ";");

        Label title = new Label("🛬  Flight Management");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.web(ACCENT));

        // Filter section
        HBox filterSection = buildFilterSection();

        // Table
        flightTable = buildFlightTable();
        VBox.setVgrow(flightTable, Priority.ALWAYS);

        // Add flight form
        TitledPane addPane = buildAddFlightPane();

        // Action buttons
        HBox actionButtons = buildActionButtons();

        root.getChildren().addAll(title, filterSection, flightTable, addPane, actionButtons);

        refreshTable();

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: " + BG_MAIN + "; -fx-background: " + BG_MAIN + ";");
        return scroll;
    }

    // ════════════════════════════════════════════
    //   COMPONENT BUILDERS
    // ════════════════════════════════════════════

    private HBox buildFilterSection() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(10, 12, 10, 12));
        box.setStyle("-fx-background-color: " + BG_PANEL + "; " +
                     "-fx-background-radius: 8; " +
                     "-fx-border-color: " + BORDER_COLOR + "; " +
                     "-fx-border-radius: 8; -fx-border-width: 1;");

        filterBox = new ComboBox<>();
        filterBox.getItems().addAll("All Flights", "By Source", "By Destination");
        filterBox.setValue("All Flights");
        filterBox.setStyle(getComboStyle());

        filterField = new TextField();
        filterField.setPromptText("Airport code...");
        filterField.setPrefWidth(150);
        filterField.setStyle(getInputStyle());

        Button filterBtn = styledButton("🔍 Filter", ACCENT_BLUE);
        filterBtn.setOnAction(e -> applyFilter());

        Button clearBtn = styledButton("✕ Show All", ACCENT_SLATE);
        clearBtn.setOnAction(e -> {
            filterField.clear();
            filterBox.setValue("All Flights");
            refreshTable();
        });

        box.getChildren().addAll(filterBox, filterField, filterBtn, clearBtn);
        return box;
    }

    private TableView<Flight> buildFlightTable() {
        TableView<Flight> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(280);
        table.setStyle("-fx-background-color: " + BG_PANEL + "; " +
                       "-fx-control-inner-background: " + BG_PANEL + "; " +
                       "-fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 6;");
        table.setPlaceholder(new Label("No flights found"));

        // Source column
        TableColumn<Flight, String> srcCol = new TableColumn<>("From");
        srcCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSource()));
        srcCol.setPrefWidth(65);

        // Destination column
        TableColumn<Flight, String> dstCol = new TableColumn<>("To");
        dstCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDestination()));
        dstCol.setPrefWidth(65);

        // Cost column
        TableColumn<Flight, Double> costCol = new TableColumn<>("Cost ($)");
        costCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getCost()).asObject());
        costCol.setPrefWidth(80);

        // Duration column
        TableColumn<Flight, String> durCol = new TableColumn<>("Duration");
        durCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormattedDuration()));
        durCol.setPrefWidth(80);

        // Airline column
        TableColumn<Flight, String> airlineCol = new TableColumn<>("Airline");
        airlineCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAirline()));

        flightData = FXCollections.observableArrayList();
        table.setItems(flightData);
        table.getColumns().addAll(srcCol, dstCol, costCol, durCol, airlineCol);

        return table;
    }

    private TitledPane buildAddFlightPane() {
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.setPadding(new Insets(14));

        // Populate airport codes for dropdowns
        List<Airport> airports = graph.getAllAirports();
        ObservableList<String> codes = FXCollections.observableArrayList();
        airports.stream()
                .map(Airport::getCode)
                .sorted()
                .forEach(codes::add);

        srcBox = new ComboBox<>(codes);
        srcBox.setPromptText("Select source");
        srcBox.setStyle(getComboStyle());
        srcBox.setPrefWidth(200);

        dstBox = new ComboBox<>(codes);
        dstBox.setPromptText("Select destination");
        dstBox.setStyle(getComboStyle());
        dstBox.setPrefWidth(200);

        costField     = inputField("e.g., 450");
        durationField = inputField("e.g., 180  (minutes)");
        airlineField  = inputField("e.g., Emirates");

        addFormRow(form, 0, "Source Airport *:", srcBox);
        addFormRow(form, 1, "Destination *:", dstBox);
        addFormRow(form, 2, "Cost (USD) *:", costField);
        addFormRow(form, 3, "Duration (min) *:", durationField);
        addFormRow(form, 4, "Airline:", airlineField);

        Button addBtn = styledButton("➕ Add Flight", ACCENT);
        addBtn.setOnAction(e -> addFlight());

        Button clearBtn = styledButton("✕ Clear", ACCENT_SLATE);
        clearBtn.setOnAction(e -> clearForm());

        form.add(new HBox(10, addBtn, clearBtn), 1, 5);

        VBox content = new VBox(form);
        content.setStyle("-fx-background-color: " + BG_PANEL + ";");

        TitledPane pane = new TitledPane("➕  Add New Flight", content);
        pane.setExpanded(false);
        return pane;
    }

    private HBox buildActionButtons() {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);

        Button removeBtn = styledButton("🗑 Remove Selected", ACCENT_RED);
        removeBtn.setOnAction(e -> removeSelectedFlight());

        Button saveBtn = styledButton("💾 Save to CSV", ACCENT_BLUE);
        saveBtn.setOnAction(e -> saveFlights());

        Button loadBtn = styledButton("📂 Reload from CSV", ACCENT_SLATE);
        loadBtn.setOnAction(e -> loadFlights());

        box.getChildren().addAll(removeBtn, saveBtn, loadBtn);
        return box;
    }

    // ════════════════════════════════════════════
    //   ACTIONS
    // ════════════════════════════════════════════

    private void addFlight() {
        String src     = srcBox.getValue();
        String dst     = dstBox.getValue();
        String costStr = costField.getText().trim();
        String durStr  = durationField.getText().trim();
        String airline = airlineField.getText().trim();

        if (src == null || dst == null || costStr.isEmpty() || durStr.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Fields", "Source, destination, cost, and duration are required.");
            return;
        }

        if (src.equals(dst)) {
            showAlert(Alert.AlertType.WARNING, "Invalid Route", "Source and destination cannot be the same airport.");
            return;
        }

        try {
            double cost     = Double.parseDouble(costStr);
            int    duration = Integer.parseInt(durStr);

            if (cost <= 0 || duration <= 0) {
                showAlert(Alert.AlertType.WARNING, "Invalid Values", "Cost and duration must be positive numbers.");
                return;
            }

            String airlineName = airline.isEmpty() ? "Unknown" : airline;
            Flight flight = new Flight(src, dst, cost, duration, airlineName);

            if (graph.addFlight(flight)) {
                refreshTable();
                clearForm();
                statusCallback.accept("Flight added: " + src + " → " + dst + " ($" + (int) cost + ")");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not add flight. Check airport codes.");
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Cost and duration must be valid numbers.");
        }
    }

    private void removeSelectedFlight() {
        Flight selected = flightTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a flight to remove.");
            return;
        }

        boolean removed = graph.removeFlight(selected.getSource(), selected.getDestination());
        if (removed) {
            refreshTable();
            statusCallback.accept("Removed flight: " + selected.getSource() + " → " + selected.getDestination());
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not remove selected flight.");
        }
    }

    private void applyFilter() {
        String type  = filterBox.getValue();
        String query = filterField.getText().trim().toUpperCase();

        if (type.equals("All Flights") || query.isEmpty()) {
            refreshTable();
            return;
        }

        List<Flight> all = graph.getAllFlights();
        List<Flight> filtered;

        if (type.equals("By Source")) {
            filtered = all.stream()
                .filter(f -> f.getSource().equalsIgnoreCase(query))
                .collect(java.util.stream.Collectors.toList());
        } else {
            filtered = all.stream()
                .filter(f -> f.getDestination().equalsIgnoreCase(query))
                .collect(java.util.stream.Collectors.toList());
        }

        flightData.setAll(filtered);
        statusCallback.accept("Filter '" + query + "': " + filtered.size() + " flight(s) found.");
    }

    private void saveFlights() {
        Csvreader.saveFlights(graph.getAllFlights(), Csvreader.FLIGHTS_FILE);
        statusCallback.accept("Flights saved to " + Csvreader.FLIGHTS_FILE);
        showAlert(Alert.AlertType.INFORMATION, "Saved", "Flights saved to CSV successfully.");
    }

    private void loadFlights() {
        List<Flight> flights = Csvreader.readFlights(Csvreader.FLIGHTS_FILE);
        for (Flight f : flights) graph.addFlight(f);
        refreshTable();
        statusCallback.accept("Reloaded flights from CSV. Total: " + graph.getFlightCount());
    }

    // ════════════════════════════════════════════
    //   HELPERS
    // ════════════════════════════════════════════

    private void refreshTable() {
        flightData.setAll(graph.getAllFlights());
        statusCallback.accept("Showing " + flightData.size() + " flights.");
    }

    private void clearForm() {
        srcBox.setValue(null);
        dstBox.setValue(null);
        costField.clear();
        durationField.clear();
        airlineField.clear();
    }

    private void addFormRow(GridPane grid, int row, String labelText, javafx.scene.Node field) {
        Label label = new Label(labelText);
        label.setTextFill(Color.web(TEXT_LABEL));
        label.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        grid.add(label, 0, row);
        grid.add(field, 1, row);
        GridPane.setHgrow(field, Priority.ALWAYS);
    }

    private TextField inputField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle(getInputStyle());
        return tf;
    }

    private Button styledButton(String text, String bgColor) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + bgColor + "; " +
                     "-fx-text-fill: white; -fx-background-radius: 5; " +
                     "-fx-cursor: hand; -fx-padding: 6 14 6 14; " +
                     "-fx-font-weight: bold;");
        return btn;
    }

    private String getInputStyle() {
        return "-fx-background-color: " + BG_INPUT + "; " +
               "-fx-text-fill: " + TEXT_MAIN + "; " +
               "-fx-prompt-text-fill: " + PLACEHOLDER + "; " +
               "-fx-background-radius: 4; " +
               "-fx-border-color: " + BORDER_INPUT + "; -fx-border-radius: 4;";
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
