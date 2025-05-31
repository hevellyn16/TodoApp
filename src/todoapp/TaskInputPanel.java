package todoapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class TaskInputPanel extends JPanel {
    private JButton openActionsButton; // botão redondo com "+"
    private JButton finishButton;      // botão de finalizar dia (precisa estar acessível)
    private ActionListener finishDayListener; // armazena listener se botão ainda não foi criado
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
        taskFrame.setSize(500, 350);
        taskFrame.setLocationRelativeTo(null);
        taskFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

//        Tarefa
        JTextField taskInput = new JTextField();
        taskInput.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        taskInput.setPreferredSize(new Dimension(300, 40));

//        Tag
        JTextField tagsInput = new JTextField();
        tagsInput.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        tagsInput.setPreferredSize(new Dimension(300, 30));
        tagsInput.setToolTipText("Tags separadas por vírgula");

//        Prioridade
        String[] prioridadeOptions = {"1 (Alta)", "2 (Média)", "3 (Baixa)"};
        JComboBox<String> prioridadeBox = new JComboBox<>(prioridadeOptions);
        prioridadeBox.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        prioridadeBox.setPreferredSize(new Dimension(150, 30));

        JButton addButton = new JButton("Adicionar");
        // Adiciona a tarefa ao pressionar Enter
        taskInput.addActionListener(e -> {
            String task = taskInput.getText();
            if (!task.isEmpty()) {
                taskList.addTask(task);
                taskInput.setText("");
            } else {
                JOptionPane.showMessageDialog(null, "Por favor, insira uma tarefa.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton deleteButton = new JButton("Excluir");
        finishButton = new JButton("Finalizar dia");
        JButton viewAllButton = new JButton("Ver todas");

        // Estilo dos botões
        JButton[] buttons = {addButton, deleteButton, finishButton, viewAllButton};
        for (JButton btn : buttons) {
            btn.setFont(new Font(Font.SERIF, Font.BOLD, 16));
            btn.setPreferredSize(new Dimension(250, 70));
            btn.setBorder(BorderFactory.createEtchedBorder());
        }

        addButton.setBackground(new Color(207, 196, 177));
        deleteButton.setBackground(Color.BLACK);
        deleteButton.setForeground(Color.WHITE);
        finishButton.setBackground(new Color(129, 174, 45));
        viewAllButton.setBackground(new Color(236, 179, 16));

        // Painel com os campos e botões
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 8, 10, 8);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;

// Título: Nome da tarefa
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(new JLabel("Nome da tarefa:"), gbc);

// Campo: Nome da tarefa
        gbc.gridy++;
        panel.add(taskInput, gbc);

// Título: Tags
        gbc.gridy++;
        panel.add(new JLabel("Tags (separadas por vírgulas):"), gbc);

// Campo: Tags
        gbc.gridy++;
        panel.add(tagsInput, gbc);

// Título: Prioridade
        gbc.gridy++;
        panel.add(new JLabel("Prioridade:"), gbc);

// Campo: Prioridade
        gbc.gridy++;
        panel.add(prioridadeBox, gbc);

// Linha com botões "Adicionar" e "Excluir"
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        panel.add(addButton, gbc);
        gbc.gridx = 1;
        panel.add(deleteButton, gbc);

// Linha com botões "Finalizar dia" e "Ver todas"
        gbc.gridy++;
        gbc.gridx = 0;
        panel.add(finishButton, gbc);
        gbc.gridx = 1;
        panel.add(viewAllButton, gbc);

// Painel externo que centraliza tudo
        JPanel innerPanel = new JPanel(new GridBagLayout());
        innerPanel.setPreferredSize(new Dimension(400, 450)); // menor largura
        innerPanel.add(panel);

        taskFrame.getContentPane().removeAll(); // Limpa se tiver algo
        taskFrame.getContentPane().add(innerPanel); // Só esse painel agora
        taskFrame.setVisible(true);

        // Ações dos botões
        addButton.addActionListener(e -> {
            String desc = taskInput.getText();
            String tagsStr = tagsInput.getText();
            int priority = prioridadeBox.getSelectedIndex() + 1;

            if (!desc.isEmpty()) {
                taskList.addTask(desc, tagsStr, priority);
                taskInput.setText("");
                tagsInput.setText("");
                prioridadeBox.setSelectedIndex(1);
            } else {
                JOptionPane.showMessageDialog(null, "Por favor, insira uma tarefa.", "Erro", JOptionPane.ERROR_MESSAGE);
            }

        });

        deleteButton.addActionListener(e -> {
            String task = taskInput.getText();
            if (!task.isEmpty()) {
                boolean removed = taskList.removeTask(task);
                if (!removed) {
                    JOptionPane.showMessageDialog(null, "Tarefa não encontrada.", "Erro", JOptionPane.ERROR_MESSAGE);
                } else {
                    taskInput.setText("");
                }
            }
        });


        viewAllButton.addActionListener(e -> taskList.showAllTasks());

        // Aplica listener se já foi definido
        if (finishDayListener != null) {
            finishButton.addActionListener(finishDayListener);
        }

        innerPanel.add(panel);
        taskFrame.setVisible(true);
    }

    public void setFinishDayAction(ActionListener listener) {
        this.finishDayListener = listener;
        if (finishButton != null) {
            finishButton.addActionListener(listener);
        }
    }
}
