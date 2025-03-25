package todoapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;

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

        list.setCellRenderer(new TaskListRenderer());
        list.addMouseListener(new TaskListMouseListener());

        setViewportView(list);
    }

    public void addTask(String task) {
        tasks.add(task);
        listModel.addElement("[ ] " + task);
        updateProgress();
    }

    public void toggleTaskCompletion(int index) {
        if (completedTasks.contains(index)) {
            completedTasks.remove(index);
        } else {
            completedTasks.add(index);
        }
        listModel.set(index, (completedTasks.contains(index) ? "[✓] " : "[ ] ") + tasks.get(index));
        updateProgress();
    }

    private void updateProgress() {
        if (progressListener != null) {
            int total = tasks.size();
            int completed = completedTasks.size();
            int progress = total > 0 ? (completed * 100) / total : 0;
            progressListener.onProgressUpdated(progress);
        }
    }

    public int getCompletedCount() {
        return completedTasks.size();
    }

    public int getTotalTasks() {
        return tasks.size();
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

    private class TaskListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (completedTasks.contains(index)) {
                label.setText("[✓] " + tasks.get(index));
                label.setForeground(Color.GRAY);
                label.setBackground(isSelected ? new Color(220, 220, 220) : Color.WHITE);
            } else {
                label.setText("[ ] " + tasks.get(index));
                label.setForeground(Color.BLACK);
                label.setBackground(isSelected ? new Color(200, 230, 255) : Color.WHITE);
            }
            return label;
        }
    }

    private class TaskListMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            int index = list.locationToIndex(e.getPoint());
            if (index >= 0) {
                Rectangle cellBounds = list.getCellBounds(index, index);
                if (cellBounds.contains(e.getPoint())) {
                    // Verifica se o clique foi na área da checkbox (primeiros 20 pixels)
                    if (e.getX() - cellBounds.x < 20) {
                        toggleTaskCompletion(index);
                    }
                }
            }
        }
    }

    public interface ProgressUpdateListener {
        void onProgressUpdated(int progress);
    }
}