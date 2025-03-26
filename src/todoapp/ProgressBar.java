package todoapp;

import javax.swing.*;
import java.awt.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ProgressBar extends JProgressBar {
    public ProgressBar() {
        super(0, 100);
        setStringPainted(true);
        setPreferredSize(new Dimension(300, 20));
        setForeground(new Color(129, 174, 45));
        setBackground(new Color(20, 20, 20));
        setBorder(BorderFactory.createLineBorder(Color.darkGray, 1));

        // Personalizar UI para definir a cor do texto
        setUI(new BasicProgressBarUI() {
            @Override
            protected void paintString(Graphics g, int x, int y, int width, int height, int amountFull, Insets b) {
                g.setColor(Color.WHITE); // Define a cor do texto como branco
                super.paintString(g, x, y, width, height, amountFull, b);
            }
        });
    }

    public void updateProgress(int targetValue) {
        Timer timer = new Timer(10, new ActionListener() { // ⏳ Intervalo de 10ms para suavizar a animação
            int currentValue = getValue(); // Obtém o valor atual da barra

            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentValue < targetValue) {
                    currentValue++; // 🔼 Aumenta progressivamente
                } else if (currentValue > targetValue) {
                    currentValue--; // 🔽 Diminui progressivamente
                } else {
                    ((Timer) e.getSource()).stop(); // 🛑 Para quando atingir o alvo
                }

                setValue(currentValue);
                setString(currentValue + "% completado");
            }
        });

        timer.start(); // 🚀 Inicia a animação
    }
}
