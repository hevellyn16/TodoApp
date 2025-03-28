package todoapp;

import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.awt.datatransfer.Transferable;

public class TaskList extends JScrollPane {
    private JList<String> list;
    private DefaultListModel<String> listModel;
    private ArrayList<String> tasks;
    private ProgressUpdateListener progressListener;
    private Map<Integer, Date> completedTasks;
    private static final String FILE_MAIN = "tasks.txt";
    private static final String FILE_HISTORY = "tasks_history.txt";

    public TaskList(ProgressUpdateListener listener) {
        this.progressListener = listener;
        completedTasks = new HashMap<>();
        tasks = new ArrayList<>();
        listModel = new DefaultListModel<>();
        list = new JList<>(listModel);

        loadTasks(); // 🚀 Carrega as tarefas ao iniciar

        scheduleDailySummary();

        list.setCellRenderer(new TaskListRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(30);
        list.setBackground(Color.WHITE);
        list.setDragEnabled(true);
        list.setDropMode(DropMode.INSERT);
        list.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.stringFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                try {
                    String data = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
                    int index = list.locationToIndex(support.getDropLocation().getDropPoint());
                    listModel.add(index, data);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        });

        list.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        list.addMouseListener(new TaskListMouseListener());

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = list.locationToIndex(e.getPoint());
                if (index >= 0 && e.getClickCount() == 2) {
                    editTask(index);
                }
            }
        });


        setViewportView(list);
        setBorder(BorderFactory.createEmptyBorder());
    }

    private void saveMainFile(){
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_MAIN))) {
            for (int i = 0; i < tasks.size(); i++) {
                if (!completedTasks.containsKey(i)) {
                    writer.write(tasks.get(i));
                    writer.newLine();
                }
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    private void saveHistoryFile(){
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_HISTORY))) {
            for (int i = 0; i < tasks.size(); i++) {
                String task = tasks.get(i);
                boolean isCompleted = completedTasks.containsKey(i);
                Date completedDate = completedTasks.get(i);

                String dateStr = (completedDate != null) ? String.valueOf(completedDate.getTime()) : "null";

                writer.write(task + ";" + isCompleted + ";" + dateStr);
                writer.newLine();
            }
        } catch(IOException e){
            e.printStackTrace();
        }
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

    public void addTask(String task) {
        tasks.add(task);
        listModel.addElement(task);
        saveHistoryFile();
        saveMainFile();// 🔄 Salva no arquivo
        updateProgress();
    }

    public void toggleTaskCompletion(int index) {
        if (completedTasks.containsKey(index)) {
            completedTasks.remove(index);
        } else {
            completedTasks.put(index, new Date());
        }
        list.repaint();
        saveHistoryFile();
        saveMainFile();// 🔄 Salva no arquivo
        updateProgress();
    }

    public void removeTask(int index) {
        if (index >= 0 && index < tasks.size()) {
            tasks.remove(index);
            listModel.remove(index);
            completedTasks.remove(index);
            saveHistoryFile();
            saveMainFile();// 🔄 Atualiza o arquivo
            updateProgress();
        }
    }

    public void clearCompletedTasks() {
        Iterator<Map.Entry<Integer, Date>> iterator = completedTasks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Date> entry = iterator.next();
            int index = entry.getKey();
            if (index < tasks.size()) {
                tasks.remove(index);
                listModel.remove(index);
                iterator.remove();
            }
        }
        saveMainFile(); // 🔄 Atualiza o arquivo
        updateProgress();
    }

    public void finalizeDay() {
        // Salva as tarefas antes de modificar a exibição
        saveHistoryFile();

        // Criar um novo modelo contendo apenas as tarefas pendentes
        List<String> pendingTasks = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            if (!completedTasks.containsKey(i)) {
                pendingTasks.add(tasks.get(i));
            }
        }

        // Atualizar a lista original com apenas as tarefas pendentes
        tasks.clear();
        tasks.addAll(pendingTasks);

        // Atualizar a interface
        listModel.clear();
        for (String task : tasks) {
            listModel.addElement(task);
        }

        completedTasks.clear(); // Esvazia o mapa de tarefas concluídas para o novo dia

        saveMainFile();
        updateProgress();
    }

    private void editTask(int index) {
        String currentTask = tasks.get(index);
        String newTask = JOptionPane.showInputDialog("Editar tarefa:", currentTask);
        if (newTask != null && !newTask.trim().isEmpty()) {
            tasks.set(index, newTask);
            listModel.set(index, newTask);
            saveHistoryFile();
            saveMainFile();
        }
    }

    private class TaskTransferHandler extends TransferHandler {
        @Override
        protected Transferable createTransferable(JComponent c) {
            JList<?> source = (JList<?>) c;
            return new StringSelection(source.getSelectedValue().toString());
        }

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        @Override
        public boolean canImport(TransferSupport support) {
            return support.isDataFlavorSupported(DataFlavor.stringFlavor);
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }
            try {
                JList.DropLocation dropLocation = (JList.DropLocation) support.getDropLocation();
                int index = dropLocation.getIndex();
                String data = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
                int oldIndex = tasks.indexOf(data);

                if (oldIndex != -1) {
                    tasks.remove(oldIndex);
                    tasks.add(index, data);
                    listModel.remove(oldIndex);
                    listModel.add(index, data);
                    saveMainFile();
                    saveHistoryFile();
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    private void loadTasks() {
        File file = new File(FILE_MAIN);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length >= 2) {
                    String task = parts[0];
                    boolean isCompleted = Boolean.parseBoolean(parts[1]);
                    String dateStr = parts[2];

                    tasks.add(task);
                    listModel.addElement(task);

                    if (isCompleted && !"null".equals(dateStr)) {
                        completedTasks.put(tasks.size() - 1, new Date(Long.parseLong(dateStr)));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        updateProgress(); // Atualiza a barra de progresso após carregar as tarefas!
    }

    public int getCompletedCount() {
        return completedTasks.size();
    }

    public int getTotalTasks() {
        return tasks.size();
    }

    public JList<String> getList() {
        return list;
    }

    public int getSelectedIndex() {
        return list.getSelectedIndex();
    }

    public String getTask(int index) {
        return tasks.get(index);
    }

    public void updateTask(int index, String newTask) {
        tasks.set(index, newTask);
        listModel.set(index, newTask);
        saveMainFile();
        saveHistoryFile();
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
            checkBox.setSelected(completedTasks.containsKey(index));

            if (isSelected) {
                panel.setBackground(new Color(210, 230, 255));
                label.setForeground(Color.BLACK);
            } else {
                panel.setBackground(Color.WHITE);
                label.setForeground(completedTasks.containsKey(index) ? new Color(100, 100, 100) : Color.BLACK);
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

    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private void scheduleDailySummary() {
        Timer timer = new Timer();
        Calendar now = Calendar.getInstance();
        Calendar scheduledTime = Calendar.getInstance();

        scheduledTime.set(Calendar.HOUR_OF_DAY, 19);
        scheduledTime.set(Calendar.MINUTE, 0);
        scheduledTime.set(Calendar.SECOND, 0);

        //Se já passou do horário, agenda para amanhã
        if (now.after(scheduledTime)) {
            scheduledTime.add(Calendar.DAY_OF_YEAR, 1);
        }

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> showAllTasks());
            }
        }, scheduledTime.getTime(), 24 * 60 * 60 * 1000); //Executa a cada 24 horas
    }

    public void showAllTasks() {
        JFrame frame = new JFrame("Todas as tarefas");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Painel principal com padding
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(248, 200, 97)); // Cor de fundo amarelada
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Painel arredondado para o conteúdo
        JPanel roundedPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30); // Bordas arredondadas
                g2.dispose();
            }

            @Override
            public Insets getInsets() {
                return new Insets(15, 15, 15, 15); // Padding interno
            }
        };
        roundedPanel.setBackground(Color.WHITE);
        roundedPanel.setOpaque(false);

        // JTextPane para o conteúdo
        JTextPane textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setBackground(new Color(255, 255, 255, 0)); // Fundo transparente
        textPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Configurar estilos
        StyledDocument doc = textPane.getStyledDocument();
        Style defaultStyle = doc.addStyle("default", null);
        StyleConstants.setFontFamily(defaultStyle, "Segoe UI Emoji");
        StyleConstants.setFontSize(defaultStyle, 16);
        StyleConstants.setSpaceBelow(defaultStyle, 10);

        Style headerStyle = doc.addStyle("header", defaultStyle);
        StyleConstants.setBold(headerStyle, true);
        StyleConstants.setFontSize(headerStyle, 18);
        StyleConstants.setAlignment(headerStyle, StyleConstants.ALIGN_CENTER);

        // Adicionar conteúdo
        try {
            doc.insertString(doc.getLength(), "Lista de tarefas:\n\n", headerStyle);

            File file = new File(FILE_HISTORY);
            if (file.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(";");
                        if (parts.length >= 3) {
                            String task = parts[0];
                            boolean isCompleted = Boolean.parseBoolean(parts[1]);
                            String dateStr = parts[2];

                            String status = isCompleted ? "🍀 Concluída" : "🔴 Pendente";
                            doc.insertString(doc.getLength(), "• " + task + " - " + status + "\n\n", defaultStyle);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Adicionar componentes ao painel arredondado
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // Remove borda
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        textPane.setBackground(Color.WHITE);
        roundedPanel.add(scrollPane, BorderLayout.CENTER);

        // Botão de fechar estilizado
        JButton closeButton = new JButton("FECHAR");
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeButton.setForeground(Color.WHITE);
        closeButton.setBackground(new Color(20, 20, 20));
        closeButton.setFocusPainted(false);
        closeButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(20, 20, 20), 1),
                BorderFactory.createEmptyBorder(8, 25, 8, 25)
        ));
        closeButton.addActionListener(e -> frame.dispose());

        // Painel do botão
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(248, 200, 97));
        buttonPanel.add(closeButton);

        // Adicionar componentes ao painel principal
        panel.add(roundedPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}