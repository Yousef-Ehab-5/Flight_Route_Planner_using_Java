package application;

import graph.FlightGraph;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.Airport;
import utils.AirportSearch;
import utils.Csvreader;

import java.util.List;
import java.util.function.Consumer;

/**
 * AirportPanel.java
 *
 * JavaFX panel for Airport Management operations.
 * Allows users to:
 *   - View all airports in a sortable table
 *   - Add new airports
 *   - Remove airports
 *   - Search airports by keyword, city, or country
 *   - Save/Load airport data to/from CSV
 */
public class AirportPanel {

    private final FlightGraph graph;
    private final Consumer<String> statusCallback;

    // TableView data
    private TableView<Airport> airportTable;
    private ObservableList<Airport> airportData;

    // Input fields
    private TextField codeField;
    private TextField nameField;
    private TextField cityField;
    private TextField countryField;
    private TextField searchField;
    private ComboBox<String> searchTypeBox;

    // ── Light Theme Palette ──────────────────────
    private static final String BG_MAIN      = "#f0f4ff"; // soft lavender-white
    private static final String BG_PANEL     = "#ffffff"; // white panels
    private static final String BG_INPUT     = "#f1f5f9"; // slate-100 inputs
    private static final String ACCENT       = "#4f46e5"; // indigo
    private static final String ACCENT_RED   = "#dc2626"; // red for destructive actions
    private static final String ACCENT_GREEN = "#16a34a"; // green
    private static final String ACCENT_BLUE  = "#3b82f6"; // blue for secondary actions
    private static final String ACCENT_SLATE = "#64748b"; // slate for neutral actions
    private static final String TEXT_MAIN    = "#1e293b"; // dark slate
    private static final String TEXT_MUTED   = "#64748b"; // medium slate
    private static final String TEXT_LABEL   = "#475569"; // form labels
    private static final String BORDER_COLOR = "#c7d2fe"; // indigo-200
    private static final String BORDER_INPUT = "#93c5fd"; // blue-300
    private static final String PLACEHOLDER  = "#94a3b8"; // slate-400
    // ────────────────────────────────────────────

    /**
     * Constructor.
     *
     * @param graph          The shared flight graph
     * @param statusCallback Callback to update the main status bar
     */
    public AirportPanel(FlightGraph graph, Consumer<String> statusCallback) {
        this.graph = graph;
        this.statusCallback = statusCallback;
    }

    /**
     * Builds and returns the full airport management panel.
     */
    public ScrollPane buildPanel() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: " + BG_MAIN + ";");

        // Title
        Label title = new Label("🛫  Airport Management");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.web(ACCENT));

        // Search bar section
        HBox searchSection = buildSearchSection();

        // Table
        airportTable = buildAirportTable();
        VBox.setVgrow(airportTable, Priority.ALWAYS);

        // Add airport form
        TitledPane addPane = buildAddAirportPane();

        // Action buttons row
        HBox actionButtons = buildActionButtons();

        root.getChildren().addAll(title, searchSection, airportTable, addPane, actionButtons);

        // Initialize table data
        refreshTable();

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: " + BG_MAIN + "; -fx-background: " + BG_MAIN + ";");
        return scroll;
    }

    /**
     * Builds the search bar section.
     */
    private HBox buildSearchSection() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(10, 12, 10, 12));
        box.setStyle("-fx-background-color: " + BG_PANEL + "; " +
                     "-fx-background-radius: 8; " +
                     "-fx-border-color: " + BORDER_COLOR + "; " +
                     "-fx-border-radius: 8; -fx-border-width: 1;");

        searchTypeBox = new ComboBox<>();
        searchTypeBox.getItems().addAll("Keyword", "City", "Country");
        searchTypeBox.setValue("Keyword");
        searchTypeBox.setStyle(getComboStyle());

        searchField = new TextField();
        searchField.setPromptText("Search airports...");
        searchField.setPrefWidth(250);
        searchField.setStyle(getInputStyle());
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Button searchBtn = styledButton("🔍 Search", ACCENT_BLUE);
        searchBtn.setOnAction(e -> performSearch());

        Button clearBtn = styledButton("✕ Clear", ACCENT_SLATE);
        clearBtn.setOnAction(e -> {
            searchField.clear();
            refreshTable();
        });

        Label searchLabel = new Label("Search:");
        searchLabel.setTextFill(Color.web(TEXT_MUTED));
        searchLabel.setFont(Font.font("Arial", 12));

        box.getChildren().addAll(searchLabel, searchTypeBox, searchField, searchBtn, clearBtn);
        return box;
    }

    /**
     * Builds the airport data table.
     */
    private TableView<Airport> buildAirportTable() {
        TableView<Airport> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(280);
        table.setStyle("-fx-background-color: " + BG_PANEL + "; " +
                       "-fx-control-inner-background: " + BG_PANEL + "; " +
                       "-fx-text-fill: " + TEXT_MAIN + "; " +
                       "-fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 6;");
        table.setPlaceholder(new Label("No airports found"));

        TableColumn<Airport, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        codeCol.setPrefWidth(70);

        TableColumn<Airport, String> nameCol = new TableColumn<>("Airport Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Airport, String> cityCol = new TableColumn<>("City");
        cityCol.setCellValueFactory(new PropertyValueFactory<>("city"));

        TableColumn<Airport, String> countryCol = new TableColumn<>("Country");
        countryCol.setCellValueFactory(new PropertyValueFactory<>("country"));

        airportData = FXCollections.observableArrayList();
        table.setItems(airportData);
        table.getColumns().addAll(codeCol, nameCol, cityCol, countryCol);

        return table;
    }

    /**
     * Builds the "Add Airport" form pane.
     */
    private TitledPane buildAddAirportPane() {
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.setPadding(new Insets(14));

        codeField    = inputField("e.g., LGA");
        nameField    = inputField("e.g., LaGuardia Airport");
        cityField    = inputField("e.g., New York");
        countryField = inputField("e.g., USA");

        addFormRow(form, 0, "IATA Code *:", codeField);
        addFormRow(form, 1, "Airport Name *:", nameField);
        addFormRow(form, 2, "City *:", cityField);
        addFormRow(form, 3, "Country *:", countryField);

        Button addBtn = styledButton("➕ Add Airport", ACCENT);
        addBtn.setOnAction(e -> addAirport());

        Button clearBtn = styledButton("✕ Clear", ACCENT_SLATE);
        clearBtn.setOnAction(e -> clearForm());

        HBox btnRow = new HBox(10, addBtn, clearBtn);
        btnRow.setPadding(new Insets(8, 0, 0, 0));
        form.add(btnRow, 1, 4);

        VBox content = new VBox(8);
        content.getChildren().add(form);
        content.setStyle("-fx-background-color: " + BG_PANEL + ";");

        TitledPane pane = new TitledPane("➕  Add New Airport", content);
        pane.setExpanded(false);
        return pane;
    }

    /**
     * Builds the action buttons row (Remove, Save, Load).
     */
    private HBox buildActionButtons() {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(4, 0, 4, 0));

        Button removeBtn = styledButton("🗑 Remove Selected", ACCENT_RED);
        removeBtn.setOnAction(e -> removeSelectedAirport());

        Button saveBtn = styledButton("💾 Save to CSV", ACCENT_BLUE);
        saveBtn.setOnAction(e -> saveAirports());

        Button loadBtn = styledButton("📂 Reload from CSV", ACCENT_SLATE);
        loadBtn.setOnAction(e -> loadAirports());

        Button viewGraphBtn = styledButton("📊 View Connections", ACCENT_GREEN);
        viewGraphBtn.setOnAction(e -> viewConnections());

        box.getChildren().addAll(removeBtn, saveBtn, loadBtn, viewGraphBtn);
        return box;
    }

    // ════════════════════════════════════════════
    //   ACTIONS
    // ════════════════════════════════════════════

    private void addAirport() {
        String code    = codeField.getText().trim().toUpperCase();
        String name    = nameField.getText().trim();
        String city    = cityField.getText().trim();
        String country = countryField.getText().trim();

        if (code.isEmpty() || name.isEmpty() || city.isEmpty() || country.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Fields", "Please fill all required fields.");
            return;
        }

        if (code.length() < 2 || code.length() > 4) {
            showAlert(Alert.AlertType.WARNING, "Invalid Code", "IATA code should be 2–4 characters.");
            return;
        }

        Airport airport = new Airport(code, name, city, country);
        if (graph.addAirport(airport)) {
            refreshTable();
            clearForm();
            statusCallback.accept("Airport added: " + code + " - " + name);
        } else {
            showAlert(Alert.AlertType.WARNING, "Duplicate", "Airport with code '" + code + "' already exists.");
        }
    }

    private void removeSelectedAirport() {
        Airport selected = airportTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an airport to remove.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Removal");
        confirm.setContentText("Remove airport '" + selected.getCode() + "'? All its flights will also be removed.");
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                graph.removeAirport(selected.getCode());
                refreshTable();
                statusCallback.accept("Removed airport: " + selected.getCode());
            }
        });
    }

    private void performSearch() {
        String query = searchField.getText().trim();
        String type  = searchTypeBox.getValue();
        List<Airport> allAirports = graph.getAllAirports();
        List<Airport> results;

        switch (type) {
            case "City":    results = AirportSearch.searchByCity(allAirports, query);    break;
            case "Country": results = AirportSearch.searchByCountry(allAirports, query); break;
            default:        results = AirportSearch.searchByKeyword(allAirports, query); break;
        }

        airportData.setAll(results);
        statusCallback.accept("Search '" + query + "': found " + results.size() + " airport(s).");
    }

    private void saveAirports() {
        Csvreader.saveAirports(graph.getAllAirports(), Csvreader.AIRPORTS_FILE);
        statusCallback.accept("Airports saved to " + Csvreader.AIRPORTS_FILE);
        showAlert(Alert.AlertType.INFORMATION, "Saved", "Airports saved to CSV successfully.");
    }

    private void loadAirports() {
        List<Airport> airports = Csvreader.readAirports(Csvreader.AIRPORTS_FILE);
        for (Airport a : airports) graph.addAirport(a);
        refreshTable();
        statusCallback.accept("Reloaded airports from CSV. Total: " + graph.getAirportCount());
    }

    private void viewConnections() {
        Airport selected = airportTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an airport to view connections.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Connections from ").append(selected.getCode()).append(":\n\n");

        var flights = graph.getFlightsFrom(selected.getCode());
        if (flights.isEmpty()) {
            sb.append("No outgoing flights from this airport.");
        } else {
            for (var flight : flights) {
                sb.append("→ ").append(flight.getDestination())
                  .append("  |  $").append(String.format("%.0f", flight.getCost()))
                  .append("  |  ").append(flight.getFormattedDuration())
                  .append("  |  ").append(flight.getAirline()).append("\n");
            }
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Connections: " + selected.getCode());
        alert.setHeaderText(selected.toString());
        alert.setContentText(sb.toString());
        alert.getDialogPane().setPrefWidth(500);
        alert.showAndWait();
    }

    // ════════════════════════════════════════════
    //   HELPERS
    // ════════════════════════════════════════════

    private void refreshTable() {
        airportData.setAll(graph.getAllAirports());
    }

    private void clearForm() {
        codeField.clear();
        nameField.clear();
        cityField.clear();
        countryField.clear();
    }

    private void addFormRow(GridPane grid, int row, String labelText, TextField field) {
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
                     "-fx-text-fill: white; " +
                     "-fx-background-radius: 5; " +
                     "-fx-cursor: hand; " +
                     "-fx-padding: 6 14 6 14; " +
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
