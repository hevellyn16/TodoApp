package todoapp;

import javax.swing.*;

public class ProgressBar extends JProgressBar {
    public ProgressBar() {
        super(0, 100);
        setStringPainted(true);
    }

    public void updateProgress(int value) {
        setValue(value);
        setString(value + "% completado");
    }
}