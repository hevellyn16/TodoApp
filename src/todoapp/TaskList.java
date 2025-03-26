package todoapp;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import todoapp.ProgressUpdateListener;

public class TaskList extends JScrollPane {
    private JList<String> list;
    private DefaultListModel<String> listModel;
    private Set<Integer> completedTasks;
    private ArrayList<String> tasks;
    private ProgressUpdateListener progressListener;

    public TaskList(ProgressUpdateListener listener) {
        this.progressListener = listener;
        completedTasks = new HashSet<>();
        tasks = new ArrayList<>();
        listModel = new DefaultListModel<>();
        list = new JList<>(listModel);

        // Configurações visuais e de comportamento
        list.setCellRenderer(new TaskListRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(30);
        list.setBackground(Color.WHITE);
        list.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        list.addMouseListener(new TaskListMouseListener());

        setViewportView(list);
        setBorder(BorderFactory.createEmptyBorder());
    }

    public void addTask(String task) {
        tasks.add(task);
        listModel.addElement(task);
        updateProgress();
    }

    public void toggleTaskCompletion(int index) {
        if (completedTasks.contains(index)) {
            completedTasks.remove(index);
        } else {
            completedTasks.add(index);
        }
        list.repaint();
        updateProgress();
    }

    public void clearCompletedTasks() {
        for (int i = tasks.size() - 1; i >= 0; i--) {
            if (completedTasks.contains(i)) {
                tasks.remove(i);
                listModel.remove(i);
                completedTasks.remove(i);
            }
        }
        updateProgress();
    }

    public int getCompletedCount() {
        return completedTasks.size();
    }

    public int getTotalTasks() {
        return tasks.size();
    }

    private void updateProgress() {
        if (progressListener != null) {
            int total = tasks.size();
            int completed = completedTasks.size();
            int progress = (total > 0) ? (completed * 100) / total : 0;
            progressListener.onProgressUpdated(progress);
        } else {
            System.err.println("ProgressUpdateListener não foi inicializado.");
        }
    }

    public void removeTask(int index) {
        if (index >= 0 && index < tasks.size()) {
            tasks.remove(index);
            listModel.remove(index);
            completedTasks.remove(index);
            updateProgress();
        }
    }

    public JList<String> getList() {
        return list;
    }


    private class TaskListRenderer implements ListCellRenderer<String> {
        private final JPanel panel = new JPanel(new BorderLayout(5, 0));
        private final JCheckBox checkBox = new JCheckBox();
        private final JLabel label = new JLabel();

        public TaskListRenderer() {
            panel.setOpaque(true);
            panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
            checkBox.setOpaque(false);
            checkBox.setFocusPainted(false);
            checkBox.setBorderPainted(false);
            checkBox.setMargin(new Insets(0, 0, 0, 1));
            label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            label.setVerticalAlignment(SwingConstants.CENTER);

            JPanel checkBoxPanel = new JPanel(new GridBagLayout());
            checkBoxPanel.setOpaque(false);
            checkBoxPanel.add(checkBox);

            panel.add(checkBoxPanel, BorderLayout.WEST);
            panel.add(label, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            label.setText(tasks.get(index));
            checkBox.setSelected(completedTasks.contains(index));

            if (isSelected) {
                panel.setBackground(new Color(210, 230, 255));
                label.setForeground(Color.BLACK);
            } else {
                panel.setBackground(Color.WHITE);
                label.setForeground(completedTasks.contains(index) ? new Color(100, 100, 100) : Color.BLACK);
            }
            return panel;
        }
    }

    private class TaskListMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            int index = list.locationToIndex(e.getPoint());
            if (index >= 0) {
                Rectangle cellBounds = list.getCellBounds(index, index);
                if (cellBounds.contains(e.getPoint())) {
                    if (e.getX() - cellBounds.x < 40) {
                        toggleTaskCompletion(index);
                    }
                }
            }
        }
    }
}
