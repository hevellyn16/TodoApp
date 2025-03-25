package todoapp;

import javax.swing.*;
import java.awt.*;

public class GreenProductivityApp {
    private JFrame frame;
    private TaskList taskList;
    private TaskInputPanel inputPanel;
    private ProgressBar progressBar;
    private HeaderPanel headerPanel;

    public GreenProductivityApp() {
        initializeFrame();
        createComponents();
        setupLayout();
        setupEventHandlers();
    }

    private void initializeFrame() {
        frame = new JFrame("Green Productivity");
        frame.setSize(400, 500);  // Aumentei a altura para melhor visualização
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(5, 5));  // Adicionado espaçamento
        frame.getContentPane().setBackground(new Color(240, 240, 240));
    }

    private void createComponents() {
        progressBar = new ProgressBar();
        taskList = new TaskList(progress -> {
            progressBar.updateProgress(progress);
        });
        inputPanel = new TaskInputPanel(taskList);
        headerPanel = new HeaderPanel();
    }

    private void setupLayout() {
        // Adiciona margem interna
        JPanel contentPane = new JPanel(new BorderLayout(5, 5));
        contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        contentPane.add(headerPanel, BorderLayout.NORTH);
        contentPane.add(taskList, BorderLayout.CENTER);
        contentPane.add(inputPanel, BorderLayout.SOUTH);

        frame.add(progressBar, BorderLayout.PAGE_START);
        frame.add(contentPane, BorderLayout.CENTER);
    }

    private void setupEventHandlers() {
        // Configura o botão "Finalizar Dia" para limpar tarefas concluídas
        inputPanel.setFinishDayAction(e -> {
            taskList.clearCompletedTasks();
            JOptionPane.showMessageDialog(frame, "Dia finalizado! Tarefas concluídas removidas.");
        });
    }

    public void show() {
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);  // Centraliza a janela
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GreenProductivityApp app = new GreenProductivityApp();
            app.show();
        });
    }
}