package todoapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TaskInputPanel extends JPanel {
    private JTextField taskInput;
    private JButton addButton;
    private JButton finishButton;
    private TaskList taskList;

    public TaskInputPanel(TaskList taskList) {
        this.taskList = taskList;
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
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, insira uma tarefa.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void setFinishDayAction(ActionListener listener) {
        finishButton.addActionListener(listener);
    }
}