package todoapp;

import javax.swing.*;
import java.awt.*;

public class GreenProductivityApp {
    private JFrame frame;
    private TaskList taskList;
    private TaskInputPanel inputPanel;
    private ProgressBar progressBar;

    public GreenProductivityApp() {
        initializeFrame();
        createComponents();
        setupLayout();
    }

    private void initializeFrame() {
        frame = new JFrame("Green Productivity");
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
    }

    private void createComponents() {
        progressBar = new ProgressBar();
        taskList = new TaskList();
        inputPanel = new TaskInputPanel(taskList, progressBar);
    }

    private void setupLayout() {
        frame.add(new HeaderPanel(), BorderLayout.NORTH);
        frame.add(taskList, BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.SOUTH);
        frame.add(progressBar, BorderLayout.PAGE_START);
    }

    public void show() {
        frame.setVisible(true);
    }
}