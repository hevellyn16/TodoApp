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

    public TaskList() {
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
    }

    public void toggleTaskCompletion(int index) {
        if (completedTasks.contains(index)) {
            completedTasks.remove(index);
        } else {
            completedTasks.add(index);
        }
        listModel.set(index, (completedTasks.contains(index) ? "[✓] " : "[ ] ") + tasks.get(index));
    }

    public int getCompletedCount() {
        return completedTasks.size();
    }

    public int getTotalTasks() {
        return tasks.size();
    }

    private class TaskListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (completedTasks.contains(index)) {
                label.setText("[✓] " + tasks.get(index));
                label.setForeground(Color.DARK_GRAY);
                label.setBackground(new Color(255, 230, 230));
            } else {
                label.setText("[ ] " + tasks.get(index));
                label.setForeground(Color.BLACK);
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
                if (cellBounds.contains(e.getPoint()) && e.getX() - cellBounds.x < 20) {
                    toggleTaskCompletion(index);
                }
            }
        }
    }
}