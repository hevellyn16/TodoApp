package todoapp;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.*;

public class TaskList extends JScrollPane {
    private static final Logger logger = LoggerFactory.getLogger(TaskList.class);
    private static final String TASKS_FILE = "tasks.json";
    private static final String COMPLETED_FILE = "completed_indices.txt";
    private static final String BACKUP_PREFIX = "backup_";

    private final JList<Task> list;
    private final DefaultListModel<Task> listModel;
    private List<Task> tasks;
    private final List<ProgressUpdateListener> progressListeners;
    private final ScheduledExecutorService scheduler;
    private final ObjectMapper objectMapper;

    public TaskList() {
        this.progressListeners = new ArrayList<>();
        this.tasks = new ArrayList<>();
        this.listModel = new DefaultListModel<>();
        this.list = new JList<>(listModel);
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        initializeUI();
        loadTasks();
        scheduleDailySummary();
    }

    private void initializeUI() {
        list.setCellRenderer(new TaskListRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(40);
        list.setBackground(new Color(245, 245, 245));
        list.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        list.addMouseListener(new TaskListMouseListener());

        setViewportView(list);
        setBorder(BorderFactory.createEmptyBorder());
    }

    // ========== OPERAÇÕES PÚBLICAS ==========

    public void addProgressListener(ProgressUpdateListener listener) {
        progressListeners.add(listener);
    }

    public void addTask(String taskDescription) {
        if (taskDescription == null || taskDescription.trim().isEmpty()) {
            throw new IllegalArgumentException("A descrição da tarefa não pode ser vazia");
        }

        Task newTask = new Task(taskDescription.trim());
        tasks.add(newTask);
        listModel.addElement(newTask);
        saveTasks();
        updateProgress();
        list.ensureIndexIsVisible(tasks.size() - 1);
    }

    public void toggleTaskCompletion(int index) {
        if (index < 0 || index >= tasks.size()) {
            logger.warn("Índice inválido para alternar conclusão: {}", index);
            return;
        }

        Task task = tasks.get(index);
        task.setCompleted(!task.isCompleted());
        task.setCompletionDate(task.isCompleted() ? LocalDate.now() : null);

        list.repaint();
        saveTasks();
        updateProgress();
    }

    public void removeTask(int index) {
        if (index < 0 || index >= tasks.size()) {
            logger.warn("Índice inválido para remoção: {}", index);
            return;
        }

        // Atualiza os índices das tarefas completas
        Map<Integer, LocalDate> newCompleted = new HashMap<>();
        for (Map.Entry<Integer, LocalDate> entry : getCompletedTasks().entrySet()) {
            int i = entry.getKey();
            if (i < index) {
                newCompleted.put(i, entry.getValue());
            } else if (i > index) {
                newCompleted.put(i - 1, entry.getValue());
            }
        }
        setCompletedTasks(newCompleted);

        tasks.remove(index);
        listModel.remove(index);
        saveTasks();
        updateProgress();
    }

    public void clearCompletedTasks() {
        List<Task> pendingTasks = new ArrayList<>();
        List<Task> completedTasks = new ArrayList<>();

        for (Task task : tasks) {
            if (task.isCompleted()) {
                completedTasks.add(task);
            } else {
                pendingTasks.add(task);
            }
        }

        if (!completedTasks.isEmpty()) {
            tasks = pendingTasks;
            refreshTaskList();
            saveTasks();
            updateProgress();
        }
    }

    public void finalizeDay() {
        List<Task> pendingTasks = tasks.stream()
                .filter(task -> !task.isCompleted())
                .collect(Collectors.toList());

        if (pendingTasks.size() != tasks.size()) {
            tasks = pendingTasks;
            refreshTaskList();
            saveTasks();
            updateProgress();
        }
    }

    public int getCompletedCount() {
        return (int) tasks.stream().filter(Task::isCompleted).count();
    }

    public int getTotalTasks() {
        return tasks.size();
    }

    public JList<Task> getList() {
        return list;
    }

    public int getSelectedIndex() {
        return list.getSelectedIndex();
    }

    public String getTaskDescription(int index) {
        if (index < 0 || index >= tasks.size()) {
            throw new IndexOutOfBoundsException("Índice inválido: " + index);
        }
        return tasks.get(index).getDescription();
    }

    public void updateTask(int index, String newDescription) {
        if (index < 0 || index >= tasks.size()) {
            logger.warn("Índice inválido para atualização: {}", index);
            return;
        }

        if (newDescription == null || newDescription.trim().isEmpty()) {
            throw new IllegalArgumentException("A descrição da tarefa não pode ser vazia");
        }

        Task task = tasks.get(index);
        task.setDescription(newDescription.trim());
        listModel.set(index, task);
        saveTasks();
    }

    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // ========== PERSISTÊNCIA ==========

    private Map<Integer, LocalDate> getCompletedTasks() {
        Map<Integer, LocalDate> completed = new HashMap<>();
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).isCompleted()) {
                completed.put(i, tasks.get(i).getCompletionDate());
            }
        }
        return completed;
    }

    private void setCompletedTasks(Map<Integer, LocalDate> completed) {
        for (int i = 0; i < tasks.size(); i++) {
            tasks.get(i).setCompleted(completed.containsKey(i));
            tasks.get(i).setCompletionDate(completed.get(i));
        }
    }

    private void saveTasks() {
        try {
            createBackup();

            // Salvar lista de tarefas
            objectMapper.writeValue(Paths.get(TASKS_FILE).toFile(), tasks);

            // Salvar índices completos
            List<String> completedIndices = new ArrayList<>();
            for (Map.Entry<Integer, LocalDate> entry : getCompletedTasks().entrySet()) {
                completedIndices.add(entry.getKey() + ":" + entry.getValue());
            }

            Files.write(Paths.get(COMPLETED_FILE),
                    completedIndices,
                    StandardCharsets.UTF_8);

        } catch (IOException e) {
            logger.error("Erro ao salvar tarefas", e);
            showErrorDialog("Erro ao salvar tarefas", e);
        }
    }

    private void createBackup() throws IOException {
        if (Files.exists(Paths.get(TASKS_FILE))) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Files.copy(Paths.get(TASKS_FILE),
                    Paths.get(BACKUP_PREFIX + timestamp + "_" + TASKS_FILE),
                    StandardCopyOption.REPLACE_EXISTING);
        }
        if (Files.exists(Paths.get(COMPLETED_FILE))) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Files.copy(Paths.get(COMPLETED_FILE),
                    Paths.get(BACKUP_PREFIX + timestamp + "_" + COMPLETED_FILE),
                    StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void loadTasks() {
        try {
            // Carregar tarefas
            if (Files.exists(Paths.get(TASKS_FILE))) {
                tasks = objectMapper.readValue(Paths.get(TASKS_FILE).toFile(),
                        new TypeReference<List<Task>>() {});

                // Validar dados carregados
                tasks.removeIf(task -> task.getDescription() == null || task.getDescription().trim().isEmpty());
            } else {
                tasks = new ArrayList<>();
            }

            // Carregar tarefas completas
            if (Files.exists(Paths.get(COMPLETED_FILE))) {
                List<String> completedEntries = Files.readAllLines(Paths.get(COMPLETED_FILE));
                for (String entry : completedEntries) {
                    try {
                        String[] parts = entry.split(":");
                        int index = Integer.parseInt(parts[0]);
                        LocalDate date = parts.length > 1 ? LocalDate.parse(parts[1]) : LocalDate.now();

                        if (index >= 0 && index < tasks.size()) {
                            tasks.get(index).setCompleted(true);
                            tasks.get(index).setCompletionDate(date);
                        }
                    } catch (Exception e) {
                        logger.warn("Entrada inválida em completedTasks: {}", entry, e);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Erro ao carregar tarefas", e);
            showErrorDialog("Erro ao carregar tarefas", e);
            tasks = new ArrayList<>();
        } finally {
            refreshTaskList();
        }
    }

    // ========== ATUALIZAÇÃO DE UI ==========

    private void refreshTaskList() {
        listModel.clear();
        tasks.forEach(listModel::addElement);
    }

    private void updateProgress() {
        int total = getTotalTasks();
        int completed = getCompletedCount();
        int progress = (total > 0) ? (completed * 100) / total : 0;

        progressListeners.forEach(listener -> {
            try {
                listener.onProgressUpdated(progress);
            } catch (Exception e) {
                logger.warn("Erro ao atualizar progresso", e);
            }
        });
    }

    private void showErrorDialog(String title, Exception e) {
        SwingUtilities.invokeLater(() -> {
            String message = e.getMessage() != null ? e.getMessage() : e.toString();
            JOptionPane.showMessageDialog(
                    this,
                    title + ":\n" + message,
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        });
    }

    // ========== AGENDAMENTO ==========

    private void scheduleDailySummary() {
        if (scheduler.isShutdown()) {
            logger.warn("Agendador desligado, não é possível agendar resumo diário");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime scheduledTime = LocalDate.now()
                .atTime(19, 0) // 19:00 (7PM)
                .plusDays(now.getHour() >= 19 ? 1 : 0); // Se já passou das 19h, agenda para amanhã

        long initialDelay = Duration.between(now, scheduledTime).toMillis();

        scheduler.scheduleAtFixedRate(() -> {
            try {
                SwingUtilities.invokeLater(this::showDailySummary);
            } catch (Exception e) {
                logger.error("Erro ao mostrar resumo diário", e);
            }
        }, initialDelay, TimeUnit.DAYS.toMillis(1), TimeUnit.MILLISECONDS);
    }

    private void showDailySummary() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Resumo Diário");
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(null);
        dialog.setModal(true);

        JPanel contentPanel = createSummaryPanel();
        dialog.add(contentPanel);
        dialog.setVisible(true);
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(248, 200, 97));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextPane textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setBackground(Color.WHITE);

        StyledDocument doc = textPane.getStyledDocument();
        Style defaultStyle = doc.addStyle("default", null);
        StyleConstants.setFontFamily(defaultStyle, "Segoe UI");
        StyleConstants.setFontSize(defaultStyle, 14);

        Style headerStyle = doc.addStyle("header", defaultStyle);
        StyleConstants.setBold(headerStyle, true);
        StyleConstants.setFontSize(headerStyle, 16);
        StyleConstants.setAlignment(headerStyle, StyleConstants.ALIGN_CENTER);

        try {
            // Cabeçalho
            doc.insertString(doc.getLength(), "Resumo Diário\n\n", headerStyle);

            // Tarefas completas hoje
            long completedToday = tasks.stream()
                    .filter(task -> task.isCompleted() &&
                            task.getCompletionDate() != null &&
                            task.getCompletionDate().equals(LocalDate.now()))
                    .count();

            doc.insertString(doc.getLength(),
                    String.format("Tarefas concluídas hoje: %d\n", completedToday),
                    defaultStyle);

            // Estatísticas totais
            doc.insertString(doc.getLength(),
                    String.format("Total: %d/%d tarefas completas\n\n",
                            getCompletedCount(),
                            getTotalTasks()),
                    defaultStyle);

            // Lista de tarefas
            doc.insertString(doc.getLength(), "Lista de Tarefas:\n", headerStyle);
            for (Task task : tasks) {
                String status = task.isCompleted() ?
                        "✅ " + (task.getCompletionDate() != null ?
                                "(" + task.getCompletionDate() + ")" : "") :
                        "⏳ ";
                doc.insertString(doc.getLength(),
                        String.format("%s%s\n", status, task.getDescription()),
                        defaultStyle);
            }
        } catch (BadLocationException e) {
            logger.error("Erro ao criar resumo", e);
        }

        JScrollPane scrollPane = new JScrollPane(textPane);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("Fechar");
        closeButton.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(panel);
            window.dispose();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ========== CLASSES INTERNAS ==========

    public static class Task implements Serializable {
        private String description;
        private boolean completed;
        private LocalDate completionDate;

        public Task(String description) {
            this.description = description;
            this.completed = false;
        }

        // Getters e Setters
        public String getDescription() { return description; }
        public void setDescription(String description) {
            if (description == null || description.trim().isEmpty()) {
                throw new IllegalArgumentException("Descrição não pode ser vazia");
            }
            this.description = description.trim();
        }

        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
        public LocalDate getCompletionDate() { return completionDate; }
        public void setCompletionDate(LocalDate completionDate) { this.completionDate = completionDate; }
    }

    private class TaskListRenderer implements ListCellRenderer<Task> {
        private final JPanel panel = new JPanel(new BorderLayout(10, 0));
        private final JCheckBox checkBox = new JCheckBox();
        private final JLabel label = new JLabel();

        public TaskListRenderer() {
            panel.setOpaque(true);
            panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

            checkBox.setOpaque(false);
            checkBox.setFocusPainted(false);
            checkBox.setToolTipText("Clique para marcar/desmarcar");

            label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            label.setVerticalAlignment(SwingConstants.CENTER);
            label.setToolTipText("Duplo clique para editar");

            JPanel leftPanel = new JPanel(new BorderLayout());
            leftPanel.setOpaque(false);
            leftPanel.add(checkBox, BorderLayout.WEST);

            panel.add(leftPanel, BorderLayout.WEST);
            panel.add(label, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Task> list, Task task,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            if (task == null) {
                return panel;
            }

            checkBox.setSelected(task.isCompleted());
            label.setText(task.getDescription());

            // Aparência baseada no estado
            if (isSelected) {
                panel.setBackground(new Color(220, 240, 255));
                label.setForeground(Color.BLACK);
            } else {
                panel.setBackground(Color.WHITE);
                if (task.isCompleted()) {
                    label.setForeground(new Color(150, 150, 150));
                    label.setFont(label.getFont().deriveFont(Font.ITALIC));
                } else {
                    label.setForeground(Color.BLACK);
                    label.setFont(label.getFont().deriveFont(Font.PLAIN));
                }
            }

            return panel;
        }
    }

    private class TaskListMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            int index = list.locationToIndex(e.getPoint());
            if (index < 0 || index >= tasks.size()) return;

            Rectangle cellBounds = list.getCellBounds(index, index);
            if (cellBounds.contains(e.getPoint())) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    // Clique na checkbox (área esquerda)
                    if (e.getX() - cellBounds.x < 40) {
                        toggleTaskCompletion(index);
                    }
                    // Duplo clique para editar
                    else if (e.getClickCount() == 2) {
                        editTask(index);
                    }
                }
            }
        }

        private void editTask(int index) {
            Task task = tasks.get(index);
            String newDescription = JOptionPane.showInputDialog(
                    TaskList.this,
                    "Editar tarefa:",
                    task.getDescription());

            if (newDescription != null && !newDescription.trim().isEmpty()) {
                updateTask(index, newDescription.trim());
            }
        }
    }

    // ========== MAIN PARA TESTES ==========

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Todo App - TaskList Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 500);

            TaskList taskList = new TaskList();
            frame.add(taskList, BorderLayout.CENTER);

            // Painel de controle para testes
            JPanel controlPanel = new JPanel(new FlowLayout());

            JButton addButton = new JButton("Adicionar");
            addButton.addActionListener(e -> {
                String task = JOptionPane.showInputDialog("Nova tarefa:");
                if (task != null && !task.trim().isEmpty()) {
                    taskList.addTask(task.trim());
                }
            });

            JButton removeButton = new JButton("Remover");
            removeButton.addActionListener(e -> {
                int index = taskList.getSelectedIndex();
                if (index >= 0) {
                    taskList.removeTask(index);
                } else {
                    JOptionPane.showMessageDialog(frame,
                            "Selecione uma tarefa para remover",
                            "Aviso",
                            JOptionPane.WARNING_MESSAGE);
                }
            });

            controlPanel.add(addButton);
            controlPanel.add(removeButton);
            frame.add(controlPanel, BorderLayout.SOUTH);

            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}