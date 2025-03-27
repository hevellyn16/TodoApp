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
    private JButton viewCompletedButton; // Novo botão para ver tarefas concluídas
    private TaskList taskList;

    public TaskInputPanel(TaskList taskList) {
        this.taskList = taskList;
        setLayout(new BorderLayout());
        createComponents();
        setupLayout();
    }

    private void createComponents() {
        taskInput = new JTextField();
        addButton = new JButton("Adicionar tarefa");
        finishButton = new JButton("Finalizar dia");
        deleteButton = new JButton("Excluir tarefa");
        viewCompletedButton = new JButton("Ver tarefas concluídas"); // Inicializa o novo botão

        // Personalizando botões
        addButton.setBackground(new Color(207, 196, 177));
        addButton.setForeground(new Color(20, 20, 20));
        finishButton.setBackground(new Color(129, 174, 45));
        finishButton.setForeground(new Color(20, 20, 20));
        deleteButton.setBackground(new Color(20, 20, 20));
        deleteButton.setForeground(Color.white);
        viewCompletedButton.setBackground(new Color(236, 179, 16)); // Define cor azul para destaque
        viewCompletedButton.setForeground(new Color(20, 20, 20));

        Font buttonFont = new Font(Font.SERIF, Font.BOLD, 16);
        addButton.setFont(buttonFont);
        finishButton.setFont(buttonFont);
        deleteButton.setFont(buttonFont);
        viewCompletedButton.setFont(buttonFont);

        addButton.setPreferredSize(new Dimension(120, 40));
        finishButton.setPreferredSize(new Dimension(120, 40));
        deleteButton.setPreferredSize(new Dimension(120, 40));
        viewCompletedButton.setPreferredSize(new Dimension(120, 40));

        addButton.setBorder(BorderFactory.createEtchedBorder());
        finishButton.setBorder(BorderFactory.createEtchedBorder());
        deleteButton.setBorder(BorderFactory.createEtchedBorder());
        viewCompletedButton.setBorder(BorderFactory.createEtchedBorder());

        addButton.setToolTipText("Adicionar nova tarefa");
        finishButton.setToolTipText("Finalizar o dia");
        deleteButton.setToolTipText("Excluir tarefa selecionada");
        viewCompletedButton.setToolTipText("Ver tarefas concluídas");

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

        // Ação para visualizar tarefas concluídas
        viewCompletedButton.addActionListener(e -> taskList.showCompletedTasksSummary());
    }

    private void setupLayout() {
        JPanel topButtonPanel = new JPanel(new GridLayout(1, 2)); // Painel superior com dois botões
        topButtonPanel.add(addButton);
        topButtonPanel.add(deleteButton);

        JPanel bottomButtonPanel = new JPanel(new GridLayout(1, 2)); // Painel inferior com os outros dois botões
        bottomButtonPanel.add(finishButton);
        bottomButtonPanel.add(viewCompletedButton);

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(topButtonPanel, BorderLayout.NORTH);
        buttonPanel.add(bottomButtonPanel, BorderLayout.SOUTH);

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