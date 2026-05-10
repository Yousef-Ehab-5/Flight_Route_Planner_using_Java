package application;

import graph.FlightGraph;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import model.Flight;

import java.util.*;

/**
 * GraphVisualizationPanel.java - Fresh Light Theme Redesign
 */
public class Graphvisualizatiopanel {

    private FlightGraph graph;
    private Canvas canvas;
    private Map<String, NodePosition> nodePositions;
    private String highlightedNode = null;
    private Set<String> highlightedPath = new HashSet<>();

    public Graphvisualizatiopanel(FlightGraph graph) {
        this.graph = graph;
        this.nodePositions = new HashMap<>();
    }

    public ScrollPane buildPanel() {
        VBox root = new VBox(24);
        root.setPadding(new Insets(32, 40, 40, 40));
        root.getStyleClass().add("page-content");

        Label title = new Label("Network Visualization");
        title.getStyleClass().add("page-title");

        HBox controls = buildControls();
        applySoftShadow(controls);
        
        // Canvas wrapper for styling and shadow
        StackPane canvasWrapper = new StackPane();
        canvasWrapper.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-radius: 12; -fx-border-width: 1;");
        canvasWrapper.setPadding(new Insets(10));
        applySoftShadow(canvasWrapper);

        canvas = new Canvas(1200, 650);
        canvasWrapper.getChildren().add(canvas);
        VBox.setVgrow(canvasWrapper, Priority.ALWAYS);

        TitledPane infoPane = buildInfoPane();
        applySoftShadow(infoPane);

        root.getChildren().addAll(title, controls, canvasWrapper, infoPane);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("edge-to-edge");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scroll;
    }

    private HBox buildControls() {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(16));
        box.setStyle(
            "-fx-background-color: #ffffff; " +
            "-fx-border-color: #e2e8f0; " +
            "-fx-border-width: 1; " +
            "-fx-background-radius: 12; " +
            "-fx-border-radius: 12;"
        );

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle(getButtonStyle("#2563eb")); // Primary Blue
        refreshBtn.setOnAction(e -> drawGraph());

        Button layoutBtn = new Button("📐 Layout");
        layoutBtn.setStyle(getButtonStyle("#059669")); // Green
        layoutBtn.setOnAction(e -> {
            calculateCircularLayout();
            drawGraph();
        });

        Button clearBtn = new Button("✕ Clear");
        clearBtn.setStyle(getButtonStyle("#64748b")); // Slate
        clearBtn.setOnAction(e -> {
            highlightedNode = null;
            highlightedPath.clear();
            drawGraph();
        });

        box.getChildren().addAll(refreshBtn, layoutBtn, clearBtn);
        return box;
    }

    private TitledPane buildInfoPane() {
        VBox content = new VBox(6);
        content.setPadding(new Insets(14));
        content.setStyle("-fx-background-color: #ffffff;");

        Label statsLabel = new Label();
        statsLabel.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px; -fx-font-weight: bold;");
        statsLabel.setWrapText(true);

        int airports = graph.getAirportCount();
        int flights = graph.getFlightCount();
        statsLabel.setText(
            "Airports: " + airports + "  |  Flights: " + flights + "  |  " +
            "Density: " + String.format("%.1f%%", (100.0 * flights) / (airports * (airports - 1)))
        );

        content.getChildren().add(statsLabel);

        TitledPane pane = new TitledPane("ℹ Network Info", content);
        pane.setExpanded(false);
        pane.setStyle("-fx-background-color: transparent; -fx-text-fill: #0f172a; -fx-font-weight: bold;");
        return pane;
    }

    private void drawGraph() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        // Clear background (White to match card)
        gc.setFill(Color.web("#ffffff"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (nodePositions.isEmpty()) {
            calculateCircularLayout();
        }

        drawEdges(gc);
        drawNodes(gc);
        drawLegend(gc);
    }

    private void drawEdges(GraphicsContext gc) {
        gc.setStroke(Color.web("#cbd5e1")); // Light muted gray
        gc.setLineWidth(1.5);

        for (String from : graph.getAllAirportCodes()) {
            NodePosition fromPos = nodePositions.get(from);
            if (fromPos == null) continue;

            for (Flight flight : graph.getFlightsFrom(from)) {
                String to = flight.getDestination();
                NodePosition toPos = nodePositions.get(to);
                if (toPos == null) continue;

                boolean inPath = highlightedPath.contains(from) && highlightedPath.contains(to);
                if (inPath) {
                    gc.setStroke(Color.web("#10b981")); // Emerald green for path
                    gc.setLineWidth(3.0);
                } else {
                    gc.setStroke(Color.web("#cbd5e1"));
                    gc.setLineWidth(1.5);
                }

                drawArrow(gc, fromPos.x, fromPos.y, toPos.x, toPos.y);
            }
        }
    }

    private void drawNodes(GraphicsContext gc) {
        for (String code : graph.getAllAirportCodes()) {
            NodePosition pos = nodePositions.get(code);
            if (pos == null) continue;

            double radius = 22;
            Color fillColor;
            Color strokeColor;

            if (code.equals(highlightedNode)) {
                fillColor = Color.web("#f59e0b"); // Amber/Orange
                strokeColor = Color.web("#b45309");
                radius = 28;
            } else if (highlightedPath.contains(code)) {
                fillColor = Color.web("#10b981"); // Emerald Green
                strokeColor = Color.web("#047857");
                radius = 26;
            } else {
                fillColor = Color.web("#e2e8f0"); // Light Slate Gray
                strokeColor = Color.web("#94a3b8");
            }

            gc.setFill(fillColor);
            gc.fillOval(pos.x - radius, pos.y - radius, radius * 2, radius * 2);

            gc.setStroke(strokeColor);
            gc.setLineWidth(2);
            gc.strokeOval(pos.x - radius, pos.y - radius, radius * 2, radius * 2);

            // Dark text for better readability on light nodes, white text on colored nodes
            if (fillColor.equals(Color.web("#e2e8f0"))) {
                gc.setFill(Color.web("#0f172a"));
            } else {
                gc.setFill(Color.WHITE);
            }
            
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(code, pos.x, pos.y + 4);
        }
    }

    private void drawArrow(GraphicsContext gc, double fromX, double fromY, double toX, double toY) {
        double dx = toX - fromX;
        double dy = toY - fromY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance == 0) return;

        double ux = dx / distance;
        double uy = dy / distance;

        double startX = fromX + ux * 25;
        double startY = fromY + uy * 25;

        double endX = toX - ux * 25;
        double endY = toY - uy * 25;

        gc.strokeLine(startX, startY, endX, endY);

        double arrowSize = 8;
        double angle = Math.atan2(uy, ux);

        double ax1 = endX - arrowSize * Math.cos(angle - Math.PI / 6);
        double ay1 = endY - arrowSize * Math.sin(angle - Math.PI / 6);
        double ax2 = endX - arrowSize * Math.cos(angle + Math.PI / 6);
        double ay2 = endY - arrowSize * Math.sin(angle + Math.PI / 6);

        gc.fillPolygon(new double[]{endX, ax1, ax2}, new double[]{endY, ay1, ay2}, 3);
    }

    private void drawLegend(GraphicsContext gc) {
        double x = 20;
        double y = canvas.getHeight() - 95;

        gc.setFill(Color.web("#ffffff"));
        gc.fillRoundRect(x - 5, y - 5, 200, 80, 10, 10);

        gc.setStroke(Color.web("#e2e8f0"));
        gc.setLineWidth(1);
        gc.strokeRoundRect(x - 5, y - 5, 200, 80, 10, 10);

        gc.setFill(Color.web("#0f172a"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("Legend", x + 5, y + 12);

        // Standard Airport
        gc.setFill(Color.web("#e2e8f0"));
        gc.fillOval(x + 8, y + 20, 12, 12);
        gc.setStroke(Color.web("#94a3b8"));
        gc.setLineWidth(1.5);
        gc.strokeOval(x + 8, y + 20, 12, 12);
        gc.setFill(Color.web("#475569"));
        gc.setFont(Font.font("Segoe UI", 11));
        gc.fillText("Airport", x + 30, y + 31);

        // Highlighted Path
        gc.setFill(Color.web("#10b981"));
        gc.fillOval(x + 8, y + 43, 12, 12);
        gc.setFill(Color.web("#475569"));
        gc.fillText("In Route", x + 30, y + 54);

        // Edge
        gc.setStroke(Color.web("#cbd5e1"));
        gc.setLineWidth(2);
        gc.strokeLine(x + 8, y + 66, x + 20, y + 66);
        gc.setFill(Color.web("#475569"));
        gc.fillText("Flight", x + 30, y + 70);
    }

    private void calculateCircularLayout() {
        List<String> codes = new ArrayList<>(graph.getAllAirportCodes());
        int count = codes.size();
        
        double centerX = canvas.getWidth() / 2;
        double centerY = canvas.getHeight() / 2;
        
        double marginX = 100;
        double marginY = 100;
        double radiusX = (canvas.getWidth() / 2) - marginX;
        double radiusY = (canvas.getHeight() / 2) - marginY;
        double radius = Math.min(radiusX, radiusY);

        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            nodePositions.put(codes.get(i), new NodePosition(x, y));
        }
    }

    public void highlightPath(List<String> path) {
        highlightedPath.clear();
        highlightedPath.addAll(path);
        drawGraph();
    }

    public void highlightNode(String node) {
        highlightedNode = node;
        drawGraph();
    }

    private String getButtonStyle(String color) {
        return "-fx-background-color: " + color + "; " +
               "-fx-text-fill: white; " +
               "-fx-font-family: 'Segoe UI'; " +
               "-fx-font-weight: bold; " +
               "-fx-background-radius: 6; " +
               "-fx-padding: 8 16 8 16; " +
               "-fx-cursor: hand;";
    }
    
    private void applySoftShadow(Node node) {
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.05));
        shadow.setRadius(15);
        shadow.setOffsetY(4);
        node.setEffect(shadow);
    }

    private static class NodePosition {
        double x, y;
        NodePosition(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}