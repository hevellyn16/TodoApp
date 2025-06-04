package todoapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class TaskInputPanel extends JPanel {
    private JButton openActionsButton; // botão redondo com "+"
    private TaskList taskList;

    public TaskInputPanel(TaskList taskList) {
        this.taskList = taskList;
        setLayout(new BorderLayout());
        createFloatingButton();

    }



    private void createFloatingButton() {
        openActionsButton = new JButton("+");
        openActionsButton.setFont(new Font("Arial", Font.BOLD, 28));
        openActionsButton.setForeground(Color.WHITE);
        openActionsButton.setBackground(new Color(70, 130, 180));
        openActionsButton.setFocusPainted(false);
        openActionsButton.setBorderPainted(false);
        openActionsButton.setContentAreaFilled(true);
        openActionsButton.setOpaque(true);
        openActionsButton.setPreferredSize(new Dimension(60, 60));
        openActionsButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Torna o botão visualmente redondo
        openActionsButton.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void installUI(JComponent c) {
                super.installUI(c);
                c.setBorder(BorderFactory.createEmptyBorder());
                c.setBackground(new Color(70, 130, 180));
                c.setForeground(Color.WHITE);
            }

            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c.getBackground());
                g2.fillOval(0, 0, c.getWidth(), c.getHeight());
                super.paint(g, c);
            }
        });

        // Posiciona o botão no canto inferior direito
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        wrapper.setOpaque(false);
        this.setOpaque(false);
        wrapper.add(openActionsButton);
        add(wrapper, BorderLayout.SOUTH);

        openActionsButton.addActionListener(e -> openTaskManagerWindow());
    }

    private void openTaskManagerWindow() {
        JFrame taskFrame = new JFrame("Gerenciar Tarefas");
        taskFrame.setSize(450, 300);
        taskFrame.setLocationRelativeTo(null);
        taskFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTextField taskInput = new JTextField();
        taskInput.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        taskInput.setPreferredSize(new Dimension(300, 40));

        JButton addButton = new JButton("Adicionar");

        // Estilo dos botões
        JButton[] buttons = {addButton};
        for (JButton btn : buttons) {
            btn.setFont(new Font(Font.SERIF, Font.BOLD, 16));
            btn.setPreferredSize(new Dimension(160, 40));
            btn.setBorder(BorderFactory.createEtchedBorder());
        }

        addButton.setBackground(new Color(207, 196, 177));

        // Painel com o campo de texto e os botões
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        panel.add(taskInput, gbc);

        gbc.gridy = 1;
        gbc.gridwidth = 2; // ocupa toda a linha
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE; // evita esticar
        panel.add(addButton, gbc);

        // Ações dos botões
        addButton.addActionListener(e -> {
            String task = taskInput.getText();
            if (!task.isEmpty()) {
                taskList.addTask(task);
                taskInput.setText("");
            } else {
                JOptionPane.showMessageDialog(null, "Por favor, insira uma tarefa.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        taskFrame.add(panel);
        taskFrame.setVisible(true); // Mostrar a janela
    }
}
