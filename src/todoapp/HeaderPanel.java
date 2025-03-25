package todoapp;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class HeaderPanel extends JPanel {
    public HeaderPanel() {
        setBackground(new Color(128, 52, 7));
        setLayout(new FlowLayout(FlowLayout.CENTER));

        JLabel dateLabel = new JLabel(getFormattedDate());
        dateLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 20));
        dateLabel.setForeground(Color.white);// Adjust font and size
        add(dateLabel);


        setPreferredSize(new Dimension(100, 50));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(getBackground());
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 30, 30)); // Rounded corners
    }

    private String getFormattedDate() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"));
    }
}