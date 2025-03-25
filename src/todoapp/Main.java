package todoapp;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GreenProductivityApp app = new GreenProductivityApp();
            app.show();
        });
    }
}