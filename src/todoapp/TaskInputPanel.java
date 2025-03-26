package todoapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class TaskInputPanel extends JPanel {
    private JTextField taskInput;
    private JButton addButton;
    private JButton finishButton;
    private JButton deleteButton; // Botão para excluir tarefa
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
        deleteButton = new JButton("Excluir Tarefa");

        // Personalizando botões
        addButton.setBackground(new Color(207, 196, 177));
        addButton.setForeground(Color.black);
        finishButton.setBackground(new Color(20, 20, 20));
        finishButton.setForeground(Color.WHITE);
        deleteButton.setBackground(new Color(255, 100, 100));
        deleteButton.setForeground(Color.black);

        Font buttonFont = new Font(Font.SANS_SERIF, Font.BOLD, 16);
        addButton.setFont(buttonFont);
        finishButton.setFont(buttonFont);
        deleteButton.setFont(buttonFont);

        addButton.setPreferredSize(new Dimension(120, 40));
        finishButton.setPreferredSize(new Dimension(120, 40));
        deleteButton.setPreferredSize(new Dimension(120, 40));

        addButton.setBorder(BorderFactory.createEtchedBorder());
        finishButton.setBorder(BorderFactory.createEtchedBorder());
        deleteButton.setBorder(BorderFactory.createEtchedBorder());

        addButton.setToolTipText("Adicionar nova tarefa");
        finishButton.setToolTipText("Finalizar o dia");
        deleteButton.setToolTipText("Excluir tarefa selecionada");

        // Ação para adicionar tarefa ao pressionar Enter
        taskInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleAddTask(new ActionEvent(taskInput, ActionEvent.ACTION_PERFORMED, null));
                }
            }
        });

        // Ação para adicionar tarefa ao clicar no botão
        addButton.addActionListener(this::handleAddTask);

        // Ação para excluir tarefa
        deleteButton.addActionListener(e -> {
            int selectedIndex = taskList.getList().getSelectedIndex(); // Obtenha o índice da tarefa selecionada
            if (selectedIndex != -1) {
                taskList.removeTask(selectedIndex); // Remova a tarefa
            } else {
                JOptionPane.showMessageDialog(this, "Por favor, selecione uma tarefa para excluir.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void setupLayout() {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3)); // Alterado para 3 colunas
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton); // Adiciona o botão de exclusão
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