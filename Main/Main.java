package main;


import application.Maindashboard;

/**
 * Main.java
 * 
 * Application entry point for the Flight Route Planner.
 * 
 * This class simply delegates to the JavaFX application class.
 * 
 * To run from Eclipse:
 *   Right-click Main.java → Run As → Java Application
 * 
 * NOTE: JavaFX VM arguments required:
 *   --module-path /path/to/javafx-sdk/lib
 *   --add-modules javafx.controls,javafx.fxml
 * 
 * See run_instructions.txt for full setup guide.
 */
public class Main {

    /**
     * Program entry point.
     * Launches the JavaFX application.
     * 
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        // Launch the JavaFX application
        // This calls Application.launch() which invokes MainDashboard.start()
        Maindashboard.main(args);
    }
}
