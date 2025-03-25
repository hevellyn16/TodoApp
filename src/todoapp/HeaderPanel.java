package todoapp;

import javax.swing.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class HeaderPanel extends JPanel {
    public HeaderPanel() {
        add(new JLabel(getFormattedDate()));
    }

    private String getFormattedDate() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"));
    }
}