package todoapp;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class GreenProductivityApp {
    private JFrame frame;
    private JTextArea taskArea;
    private JTextField taskInput;
    private JButton addButton;
    private JButton finishButton;
    private JProgressBar progressBar;
    private ArrayList<String> tasks;

    public GreenProductivityApp() {
        tasks = new ArrayList<>();

        // Configurações da Janela
        frame = new JFrame("Green Productivity");
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Painel Superior
        JPanel headerPanel = new JPanel();
        headerPanel.add(new JLabel("Thursday, 13 February"));
        frame.add(headerPanel, BorderLayout.NORTH);

        // Área de Tarefas
        taskArea = new JTextArea();
        frame.add(new JScrollPane(taskArea), BorderLayout.CENTER);

        // Input de Tarefa e Botões
        JPanel inputPanel = new JPanel();
        taskInput = new JTextField(20);
        addButton = new JButton("+");
        finishButton = new JButton("FINISH DAY");
        progressBar = new JProgressBar(0, 100);

        // Adicionando Ação para o Botão
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTask();
            }
        });

        // Layout do Painel de Entrada
        inputPanel.add(taskInput);
        inputPanel.add(addButton);
        inputPanel.add(finishButton);
        frame.add(inputPanel, BorderLayout.SOUTH);

        // Adicionando a barra de progresso
        frame.add(progressBar, BorderLayout.NORTH);

        // Mostrar a Janela
        frame.setVisible(true);
    }

    private void addTask() {
        String task = taskInput.getText();
        if (!task.isEmpty()) {
            tasks.add(task);
            taskArea.append(task + "\n");
            taskInput.setText("");
            updateProgress();
        }
    }

    private void updateProgress() {
        progressBar.setValue((tasks.size() * 100) / 5); // Exemplo com 5 tarefas
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GreenProductivityApp::new);
    }
}