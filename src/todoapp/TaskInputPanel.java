package todoapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class TaskInputPanel extends JPanel {
    private JTextField taskInput;
    private JButton addButton;
    private JButton finishButton;
    private TaskList taskList;
    private ProgressBar progressBar;

    public TaskInputPanel(TaskList taskList, ProgressBar progressBar) {
        this.taskList = taskList;
        this.progressBar = progressBar;

        setLayout(new BorderLayout());
        createComponents();
        setupLayout();
    }

    private void createComponents() {
        taskInput = new JTextField();
        addButton = new JButton("Adicionar");
        finishButton = new JButton("Finalizar Dia");

        addButton.addActionListener(this::handleAddTask);
    }

    private void setupLayout() {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        buttonPanel.add(addButton);
        buttonPanel.add(finishButton);

        add(taskInput, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void handleAddTask(ActionEvent e) {
        String task = taskInput.getText();
        if (!task.isEmpty()) {
            taskList.addTask(task);
            taskInput.setText("");
            updateProgress();
        }
    }

    private void updateProgress() {
        int total = taskList.getTotalTasks();
        int completed = taskList.getCompletedCount();
        progressBar.updateProgress(total > 0 ? (completed * 100) / total : 0);
    }
}