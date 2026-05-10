package application;

import algorithms.Dfstraversal;
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
import java.util.*;

/**
 * DFSVisualizerPanel.java - Fresh Light Theme Redesign
 */
public class Dfsvisulaizerpannel {

    private FlightGraph graph;
    private java.util.function.Consumer<String> statusCallback;
    
    private Canvas nodeCanvas;
    private Canvas treeCanvas;
    private Map<String, NodeVisual> nodes;
    private List<String> visitOrder;
    private Dfstraversal dfs;
    
    private int currentStep = 0;
    private Timer animationTimer;
    private boolean isPlaying = false;
    
    private Label stepLabel;
    private Label infoLabel;
    private TextArea detailsArea;
    private ProgressBar progressBar;
    private Button pauseBtn;
    private Button resumeBtn;

    public Dfsvisulaizerpannel(FlightGraph graph, java.util.function.Consumer<String> statusCallback) {
        this.graph = graph;
        this.statusCallback = statusCallback;
        this.dfs = new Dfstraversal();
        this.nodes = new HashMap<>();
        this.visitOrder = new ArrayList<>();
    }

    public ScrollPane buildPanel() {
        VBox root = new VBox(24); 
        root.setPadding(new Insets(32, 40, 40, 40));
        root.getStyleClass().add("page-content");

        Label title = new Label("DFS Algorithm Visualizer");
        title.getStyleClass().add("page-title");

        HBox controls = buildControlPanel();
        applySoftShadow(controls);
        
        HBox canvasContainer = new HBox(20);
        canvasContainer.setPadding(new Insets(16));
        canvasContainer.setStyle(
            "-fx-background-color: #ffffff; " +
            "-fx-border-color: #e2e8f0; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 12; " +
            "-fx-background-radius: 12;"
        );
        canvasContainer.setMinHeight(550);
        HBox.setHgrow(canvasContainer, Priority.ALWAYS);
        applySoftShadow(canvasContainer);

        // Graph canvas
        VBox graphBox = new VBox(10);
        Label graphLabel = new Label("📊 Graph Traversal");
        graphLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        graphLabel.setTextFill(Color.web("#0f172a"));
        
        nodeCanvas = new Canvas(600, 500);
        StackPane nodeCanvasWrapper = new StackPane(nodeCanvas);
        nodeCanvasWrapper.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");
        VBox.setVgrow(nodeCanvasWrapper, Priority.ALWAYS);
        graphBox.getChildren().addAll(graphLabel, nodeCanvasWrapper);
        HBox.setHgrow(graphBox, Priority.ALWAYS);

        // DFS Tree canvas
        VBox treeBox = new VBox(10);
        Label treeLabel = new Label("🌳 DFS Tree Structure");
        treeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        treeLabel.setTextFill(Color.web("#0f172a"));
        
        treeCanvas = new Canvas(600, 500);
        StackPane treeCanvasWrapper = new StackPane(treeCanvas);
        treeCanvasWrapper.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");
        VBox.setVgrow(treeCanvasWrapper, Priority.ALWAYS);
        treeBox.getChildren().addAll(treeLabel, treeCanvasWrapper);
        HBox.setHgrow(treeBox, Priority.ALWAYS);

        canvasContainer.getChildren().addAll(graphBox, treeBox);

        HBox infoPanel = buildInfoPanel();
        applySoftShadow(infoPanel);

        root.getChildren().addAll(title, controls, canvasContainer, infoPanel);
        VBox.setVgrow(canvasContainer, Priority.ALWAYS);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("edge-to-edge");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scroll;
    }

    private HBox buildControlPanel() {
        HBox box = new HBox(16);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(16));
        box.setStyle(
            "-fx-background-color: #ffffff; " +
            "-fx-border-color: #e2e8f0; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8;"
        );

        Label srcLabel = new Label("Source:");
        srcLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        srcLabel.setTextFill(Color.web("#475569"));

        ComboBox<String> sourceCombo = new ComboBox<>();
        sourceCombo.getItems().addAll(graph.getAllAirportCodes());
        if (!sourceCombo.getItems().isEmpty()) {
            sourceCombo.setValue(sourceCombo.getItems().get(0));
        }
        sourceCombo.setPrefWidth(120);
        sourceCombo.setStyle(getComboStyle());

        Button startBtn = styledButton("▶ START", "#2563eb"); // Blue
        startBtn.setOnAction(e -> startAnimation(sourceCombo.getValue()));

        pauseBtn = styledButton("⏸ PAUSE", "#ef4444"); // Red
        pauseBtn.setDisable(true);
        pauseBtn.setOnAction(e -> pauseAnimation());

        resumeBtn = styledButton("▶ RESUME", "#059669"); // Green
        resumeBtn.setDisable(true);
        resumeBtn.setOnAction(e -> resumeAnimation());

        Button nextBtn = styledButton("⊳ NEXT", "#475569"); // Slate
        nextBtn.setOnAction(e -> nextStep());

        Button resetBtn = styledButton("↻ RESET", "#94a3b8"); // Light Slate
        resetBtn.setOnAction(e -> resetAnimation());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        stepLabel = new Label("Step: 0 / 0");
        stepLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        stepLabel.setTextFill(Color.web("#0f172a"));

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(150);
        progressBar.setStyle("-fx-accent: #2563eb;");

        box.getChildren().addAll(
            srcLabel, sourceCombo, startBtn, pauseBtn, 
            resumeBtn, nextBtn, resetBtn, spacer, stepLabel, progressBar
        );

        return box;
    }

    private HBox buildInfoPanel() {
        HBox box = new HBox(20);
        box.setPadding(new Insets(16));
        box.setStyle(
            "-fx-background-color: #ffffff; " +
            "-fx-border-color: #e2e8f0; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8;"
        );

        VBox statsBox = new VBox(10);
        Label statsTitle = new Label("📊 Execution Stats");
        statsTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        statsTitle.setTextFill(Color.web("#0f172a"));

        infoLabel = new Label("Nodes: 0 / " + graph.getAirportCount());
        infoLabel.setFont(Font.font("Segoe UI", 12));
        infoLabel.setTextFill(Color.web("#475569"));

        statsBox.getChildren().addAll(statsTitle, infoLabel);
        statsBox.setMinWidth(220);

        detailsArea = new TextArea();
        detailsArea.setEditable(false);
        detailsArea.setWrapText(true);
        detailsArea.setPrefHeight(100);
        detailsArea.setStyle(
            "-fx-control-inner-background: #f8fafc; " +
            "-fx-text-fill: #0f172a; " +
            "-fx-font-family: 'Consolas', monospace; " +
            "-fx-font-size: 13; " +
            "-fx-border-color: #e2e8f0; " +
            "-fx-border-radius: 6;"
        );
        detailsArea.setText("Select source and click START to begin.");
        HBox.setHgrow(detailsArea, Priority.ALWAYS);

        box.getChildren().addAll(statsBox, detailsArea);
        return box;
    }

    // --- Core Logic ---

    private void startAnimation(String source) {
        if (source == null || source.isEmpty()) {
            statusCallback.accept("Select source airport");
            return;
        }
        currentStep = 0;
        visitOrder.clear();
        nodes.clear();
        isPlaying = true;
        visitOrder.addAll(dfs.traverseFromSource(graph, source));
        if (visitOrder.isEmpty()) {
            detailsArea.setText("❌ No nodes reachable from " + source);
            return;
        }
        calculateLayout();
        for (String code : nodes.keySet()) nodes.get(code).state = "unvisited";
        pauseBtn.setDisable(false);
        resumeBtn.setDisable(true);
        detailsArea.setText("▶ DFS started from " + source + "\nVisiting " + visitOrder.size() + " nodes...");
        statusCallback.accept("DFS Animation: " + visitOrder.size() + " nodes");
        startAutoAnimation();
    }

    private void startAutoAnimation() {
        if (animationTimer != null) animationTimer.cancel();
        animationTimer = new Timer();
        animationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                javafx.application.Platform.runLater(() -> {
                    if (isPlaying && currentStep < visitOrder.size()) updateStep();
                    else if (isPlaying && currentStep >= visitOrder.size()) completeAnimation();
                });
            }
        }, 600, 600);
    }

    private void nextStep() {
        if (currentStep < visitOrder.size()) updateStep();
    }

    private void updateStep() {
        if (currentStep < visitOrder.size()) {
            String current = visitOrder.get(currentStep);
            nodes.get(current).state = "visiting";
            stepLabel.setText("Step: " + (currentStep + 1) + " / " + visitOrder.size());
            infoLabel.setText("Nodes Explored: " + (currentStep + 1) + " / " + graph.getAirportCount());
            progressBar.setProgress((double) (currentStep + 1) / visitOrder.size());
            if (currentStep > 0) nodes.get(visitOrder.get(currentStep - 1)).state = "visited";
            
            StringBuilder sb = new StringBuilder();
            sb.append("Current: ").append(current).append("\n\nVisited Order:\n");
            for (int i = 0; i <= currentStep; i++) {
                sb.append((i + 1)).append(". ").append(visitOrder.get(i));
                if (i == currentStep) sb.append(" ◄");
                sb.append("\n");
            }
            detailsArea.setText(sb.toString());
            draw();
            currentStep++;
        }
    }

    private void completeAnimation() {
        isPlaying = false;
        if (animationTimer != null) animationTimer.cancel();
        for (String code : visitOrder) nodes.get(code).state = "visited";
        draw();
        detailsArea.setText("✅ Complete!\nTotal nodes: " + visitOrder.size());
        pauseBtn.setDisable(true);
        resumeBtn.setDisable(false);
    }

    private void pauseAnimation() {
        isPlaying = false;
        if (animationTimer != null) animationTimer.cancel();
        pauseBtn.setDisable(true);
        resumeBtn.setDisable(false);
    }

    private void resumeAnimation() {
        isPlaying = true;
        pauseBtn.setDisable(false);
        resumeBtn.setDisable(true);
        startAutoAnimation();
    }

    private void resetAnimation() {
        isPlaying = false;
        currentStep = 0;
        visitOrder.clear();
        nodes.clear();
        if (animationTimer != null) animationTimer.cancel();
        stepLabel.setText("Step: 0 / 0");
        infoLabel.setText("Nodes: 0 / " + graph.getAirportCount());
        progressBar.setProgress(0);
        detailsArea.setText("Select source and click START.");
        pauseBtn.setDisable(true);
        resumeBtn.setDisable(true);
        draw();
    }

    private void calculateLayout() {
        List<String> codes = new ArrayList<>(graph.getAllAirportCodes());
        int count = codes.size();
        double centerX = nodeCanvas.getWidth() / 2;
        double centerY = nodeCanvas.getHeight() / 2;
        double radius = Math.min(centerX, centerY) - 80;
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            nodes.put(codes.get(i), new NodeVisual(codes.get(i), x, y));
        }
    }

    private void draw() {
        drawGraph();
        drawDFSTree();
    }

    private void drawGraph() {
        GraphicsContext gc = nodeCanvas.getGraphicsContext2D();
        gc.setFill(Color.web("#ffffff")); // White Background
        gc.fillRect(0, 0, nodeCanvas.getWidth(), nodeCanvas.getHeight());
        if (nodes.isEmpty()) return;
        
        gc.setStroke(Color.web("#cbd5e1")); // Light slate lines
        gc.setLineWidth(1.5);
        for (String code : nodes.keySet()) {
            NodeVisual nv = nodes.get(code);
            for (var flight : graph.getFlightsFrom(code)) {
                NodeVisual target = nodes.get(flight.getDestination());
                if (target != null) gc.strokeLine(nv.x, nv.y, target.x, target.y);
            }
        }
        for (NodeVisual nv : nodes.values()) drawNode(gc, nv);
    }

    private void drawNode(GraphicsContext gc, NodeVisual nv) {
        double radius = 22;
        Color fillColor;
        Color strokeColor;
        Color textColor = Color.web("#ffffff");

        if (nv.state.equals("visiting")) { 
            fillColor = Color.web("#eab308"); // Yellow/Amber
            strokeColor = Color.web("#a16207");
            textColor = Color.web("#0f172a");
            radius = 28; 
        }
        else if (nv.state.equals("visited")) { 
            fillColor = Color.web("#10b981"); // Emerald Green
            strokeColor = Color.web("#047857");
        }
        else { 
            fillColor = Color.web("#e2e8f0"); // Slate 200
            strokeColor = Color.web("#94a3b8"); // Slate 400
            textColor = Color.web("#0f172a");
        }

        gc.setFill(fillColor);
        gc.fillOval(nv.x - radius, nv.y - radius, radius * 2, radius * 2);
        
        gc.setStroke(strokeColor);
        gc.setLineWidth(2);
        gc.strokeOval(nv.x - radius, nv.y - radius, radius * 2, radius * 2);
        
        gc.setFill(textColor);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(nv.code, nv.x, nv.y + 4);
    }

    private void drawDFSTree() {
        GraphicsContext gc = treeCanvas.getGraphicsContext2D();
        gc.setFill(Color.web("#ffffff"));
        gc.fillRect(0, 0, treeCanvas.getWidth(), treeCanvas.getHeight());
        if (visitOrder.isEmpty()) return;
        
        double startX = 50, startY = 30, levelHeight = 50, nodeWidth = 60;
        int level = 0, nodesPerLevel = 1, nodeIndex = 0;
        
        for (int i = 0; i < visitOrder.size(); i++) {
            String node = visitOrder.get(i);
            double x = startX + (nodeIndex % nodesPerLevel) * (nodeWidth + 20);
            double y = startY + level * levelHeight;
            
            Color fillColor;
            Color strokeColor;
            Color textColor = Color.web("#ffffff");

            if (i < currentStep) {
                fillColor = Color.web("#10b981"); // Visited
                strokeColor = Color.web("#047857");
            } else if (i == currentStep) {
                fillColor = Color.web("#eab308"); // Visiting
                strokeColor = Color.web("#a16207");
                textColor = Color.web("#0f172a");
            } else {
                fillColor = Color.web("#e2e8f0"); // Unvisited
                strokeColor = Color.web("#94a3b8");
                textColor = Color.web("#0f172a");
            }

            gc.setFill(fillColor);
            gc.fillOval(x - 15, y - 15, 30, 30);
            
            gc.setStroke(strokeColor);
            gc.setLineWidth(2);
            gc.strokeOval(x - 15, y - 15, 30, 30);
            
            gc.setFill(textColor);
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(node, x, y + 4);
            nodeIndex++;
            
            if (nodeIndex >= nodesPerLevel && i < visitOrder.size() - 1) {
                level++; nodesPerLevel = Math.min(nodesPerLevel + 1, 8); nodeIndex = 0;
            }
        }
        
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        gc.setFill(Color.web("#475569"));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("DFS Visit Order Tree", 10, treeCanvas.getHeight() - 15);
    }

    // ════════════════════════════════════════════
    //   STYLING HELPERS
    // ════════════════════════════════════════════

    private Button styledButton(String text, String bgColor) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-background-color: " + bgColor + "; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 8 16 8 16; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand;"
        );
        return btn;
    }

    private String getComboStyle() {
        return "-fx-background-color: #f8fafc; " +
               "-fx-text-fill: #0f172a; " +
               "-fx-border-color: #e2e8f0; " +
               "-fx-border-radius: 6;";
    }

    private void applySoftShadow(Node node) {
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.05));
        shadow.setRadius(15);
        shadow.setOffsetY(4);
        node.setEffect(shadow);
    }

    private static class NodeVisual {
        String code; double x, y;
        String state = "unvisited";
        NodeVisual(String code, double x, double y) { this.code = code; this.x = x; this.y = y; }
    }
}