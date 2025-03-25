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

        // Personalizando botões
        addButton.setBackground(new Color(207, 196, 177));
        addButton.setForeground(Color.black);
        finishButton.setBackground(new Color(20, 20, 20));
        finishButton.setForeground(Color.WHITE);

        Font buttonFont = new Font(Font.SANS_SERIF, Font.BOLD, 16);
        addButton.setFont(buttonFont);
        finishButton.setFont(buttonFont);

        addButton.setPreferredSize(new Dimension(120, 40));
        finishButton.setPreferredSize(new Dimension(120, 40));

        addButton.setBorder(BorderFactory.createEtchedBorder());
        finishButton.setBorder(BorderFactory.createEtchedBorder());

        addButton.setToolTipText("Adicionar nova tarefa");
        finishButton.setToolTipText("Finalizar o dia");

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