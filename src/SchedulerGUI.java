/*
SchedulerGUI is the display for the program. It processes user clicks and starts, pauses, and resumes threads.
It displays waiting processes, current system status, CPU activity, and finished Process information.

Written by:
Tristan Boler

CS 490
Fall 2021
*/

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.table.DefaultTableModel;

public class SchedulerGUI extends javax.swing.JFrame {

    /**
     * Creates new form SchedulerGUI
     */
    public SchedulerGUI(List<Process> processList) {
        initComponents();
        // Copy process list
        this.processList = processList;
        // Setup process table
        processQueueModel = (DefaultTableModel) processQueueTable.getModel();
        reportTableModel = (DefaultTableModel) reportTable.getModel();

        // Display CPU status
        updateCpuTextField(null, -1, 1);
        updateCpuTextField(null, -1, 2);


        // Create CPU threads
        cpu1_thread = thread_run.make(s1, processList, this, 1);
        cpu2_thread = thread_run.make(s2,  processList, this, 2);
    }

    // Initial set up for GUI components
    private void initComponents() {

        startButton = new javax.swing.JButton();
        pauseButton = new javax.swing.JButton();
        systemStatusLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        processQueueTable = new javax.swing.JTable();
        processQueueLabel = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        cpu1TextArea = new javax.swing.JTextArea();
        jScrollPane4 = new javax.swing.JScrollPane();
        cpu2TextArea = new javax.swing.JTextArea();
        timeUnitTextField = new javax.swing.JTextField();
        timeUnitLabel = new javax.swing.JLabel();
        timeUnitLabel2 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        reportTable = new javax.swing.JTable();
        throughputLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                systemRunningFlag = false;
            }
        });

        startButton.setText("Start System");
        startButton.setSize(new java.awt.Dimension(80, 25));
        startButton.addActionListener(this::startButtonActionPerformed);

        pauseButton.setText("Pause/Resume System");
        pauseButton.setSize(new java.awt.Dimension(80, 25));
        pauseButton.addActionListener(this::pauseButtonActionPerformed);
        pauseButton.setEnabled(false);

        systemStatusLabel.setText("System Status...");
        systemStatusLabel.setPreferredSize(new java.awt.Dimension(150, 25));
        systemStatusLabel.setSize(new java.awt.Dimension(150, 25));

        processQueueTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{

                },
                new String[]{
                        "Process Name", "Service Time"
                }
        ) {
            final Class[] types = new Class[]{
                    java.lang.String.class, java.lang.Integer.class
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        });
        processQueueTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        jScrollPane2.setViewportView(processQueueTable);
        if (processQueueTable.getColumnModel().getColumnCount() > 0) {
            processQueueTable.getColumnModel().getColumn(0).setResizable(false);
            processQueueTable.getColumnModel().getColumn(1).setResizable(false);
        }

        processQueueLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        processQueueLabel.setText("Waiting Process Queue");
        processQueueLabel.setSize(new java.awt.Dimension(40, 20));

        cpu1TextArea.setEditable(false);
        cpu1TextArea.setColumns(20);
        cpu1TextArea.setLineWrap(true);
        cpu1TextArea.setRows(5);
        cpu1TextArea.setMaximumSize(new java.awt.Dimension(230, 90));
        cpu1TextArea.setPreferredSize(new java.awt.Dimension(230, 90));
        cpu1TextArea.setSize(new java.awt.Dimension(230, 90));
        jScrollPane3.setViewportView(cpu1TextArea);

        cpu2TextArea.setEditable(false);
        cpu2TextArea.setColumns(20);
        cpu2TextArea.setLineWrap(true);
        cpu2TextArea.setRows(5);
        cpu2TextArea.setPreferredSize(new java.awt.Dimension(230, 90));
        cpu2TextArea.setRequestFocusEnabled(false);
        cpu2TextArea.setSize(new java.awt.Dimension(230, 90));
        jScrollPane4.setViewportView(cpu2TextArea);

        timeUnitTextField.setText(String.valueOf(Utility.getExecutionSpeed()));
        timeUnitTextField.setSize(new java.awt.Dimension(50, 20));
        timeUnitTextField.addActionListener(this::timeUnitTextFieldActionPerformed);

        timeUnitLabel.setText("1 time unit =");
        timeUnitLabel.setPreferredSize(new java.awt.Dimension(80, 20));
        timeUnitLabel.setSize(new java.awt.Dimension(80, 20));

        timeUnitLabel2.setText("ms");
        timeUnitLabel2.setSize(new java.awt.Dimension(25, 20));

        reportTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{

                },
                new String[]{
                        "Process Name", "Arrival Time", "Service Time", "Finish Time", "TAT", "nTAT"
                }
        ) {
            final Class[] types = new Class[]{
                    java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Double.class
            };
            final boolean[] canEdit = new boolean[]{
                    false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        reportTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane5.setViewportView(reportTable);
        if (reportTable.getColumnModel().getColumnCount() > 0) {
            reportTable.getColumnModel().getColumn(0).setResizable(false);
            reportTable.getColumnModel().getColumn(1).setResizable(false);
            reportTable.getColumnModel().getColumn(2).setResizable(false);
            reportTable.getColumnModel().getColumn(3).setResizable(false);
            reportTable.getColumnModel().getColumn(4).setResizable(false);
            reportTable.getColumnModel().getColumn(5).setResizable(false);
        }

        // Set preferred width for report table columns
        reportTable.getColumnModel().getColumn(0).setPreferredWidth(85);
        reportTable.getColumnModel().getColumn(1).setPreferredWidth(85);
        reportTable.getColumnModel().getColumn(2).setPreferredWidth(85);
        reportTable.getColumnModel().getColumn(3).setPreferredWidth(85);
        reportTable.getColumnModel().getColumn(4).setPreferredWidth(40);
        reportTable.getColumnModel().getColumn(5).setPreferredWidth(40);
        reportTable.setShowVerticalLines(true);
        reportTable.setShowHorizontalLines(true);

        throughputLabel.setText("Current Throughput:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(40, 40, 40)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addGap(0, 0, Short.MAX_VALUE)
                                                .addComponent(timeUnitLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(timeUnitTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(timeUnitLabel2)
                                                .addGap(62, 62, 62))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(jScrollPane5, javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addGroup(layout.createSequentialGroup()
                                                                                .addComponent(startButton)
                                                                                .addGap(18, 18, 18)
                                                                                .addComponent(pauseButton))
                                                                        .addComponent(processQueueLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(systemStatusLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                                .addGap(30, 30, 30))))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(throughputLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 490, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(50, 50, 50))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(30, 30, 30)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(startButton)
                                        .addComponent(pauseButton)
                                        .addComponent(systemStatusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(31, 31, 31)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addComponent(processQueueLabel)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(timeUnitTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(timeUnitLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(timeUnitLabel2))
                                                .addGap(5, 5, 5)))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(throughputLabel)
                                .addContainerGap(12, Short.MAX_VALUE))
        );

        pack();
    }

    // When start button is clicked
    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            // Start the threads
            cpu1_thread.start();
            cpu2_thread.start();
            // Update system status label
            systemStatusLabel.setText("System Running");
            // Update system running flag
            systemRunningFlag = true;
            // Disable the start button
            startButton.setEnabled(false);
            // Enable the pause button
            pauseButton.setEnabled(true);
        } catch (IllegalMonitorStateException e) {
            System.out.println("Thread already started...");
        }
    }

    // When a new execution speed is entered
    private void timeUnitTextFieldActionPerformed(java.awt.event.ActionEvent evt) {
        // Save new execution speed
        Utility.setExecutionSpeed(Integer.parseInt(timeUnitTextField.getText()));
    }

    // When pause/resume button is clicked
    private void pauseButtonActionPerformed(java.awt.event.ActionEvent evt) {
        // If system is not running
        if (!systemRunningFlag) {
            try {
                // Resume the threads
                cpu1_thread.resume();
                cpu2_thread.resume();
                // Update system running flag
                systemRunningFlag = true;
                // Update system status label
                updateSystemStatusLabel("System Running");
            } catch (IllegalMonitorStateException e) {
                System.out.println("There are no threads running, cannot resume.");
            }
        }
        // Else, system is running
        else {
            // Pause the threads
            cpu1_thread.pause();
            cpu2_thread.pause();
            // Update system running flag
            systemRunningFlag = false;
            // Update system status label
            updateSystemStatusLabel("System Paused");
            // Update CPU text fields
            updateCpuTextField(null, -1, 1);
            updateCpuTextField(null, -1, 2);
        }
    }

    // Refresh the waiting process table
    public void populateProcessTable() {
        Object[] rowData = new Object[2];
        // Clear the table
        for (int rowCount = (processQueueModel.getRowCount() - 1); rowCount >= 0; rowCount--) {
            processQueueModel.removeRow(rowCount);
            processQueueTable.revalidate();
        }
        // Place new data in table
        for (Process process : Utility.getWaitingProcessList()) {
            rowData[0] = process.getProcessId();
            rowData[1] = process.getServiceTime();
            processQueueModel.addRow(rowData);
        }
    }

    // Update the CPU text fields with current processing information
    public void updateCpuTextField(Process currentProcess, int timeRemaining, int threadNumber) {
        // If updating for thread 1
        if (threadNumber == 1) {
            cpu1TextArea.setText("\nCPU 1\n");

            // If system is not running, display process being executed and time remaining
            if (!systemRunningFlag || currentProcess == null) {
                cpu1TextArea.append("Executing: Idle\n");
                cpu1TextArea.append("Time Remaining = n/a");
            }
            // Else, system is running, update with current process and time remaining
            else {
                cpu1TextArea.append("Executing: Process " + currentProcess.getProcessId() + "\n");
                cpu1TextArea.append("Time Remaining = " + timeRemaining);
            }
        }
        // If updating for thread 2
        else if (threadNumber == 2) {
            cpu2TextArea.setText("\nCPU 2\n");

            // If system is not running, display process being executed and time remaining
            if (!systemRunningFlag || currentProcess == null) {
                cpu2TextArea.append("Executing: Idle\n");
                cpu2TextArea.append("Time Remaining = n/a");
            }
            // Else, system is running, update with current process and time remaining
            else {
                cpu2TextArea.append("Executing: Process " + currentProcess.getProcessId() + "\n");
                cpu2TextArea.append("Time Remaining = " + timeRemaining); // determine time remaining
            }
        }
    }

    // Update system status label with passed in string
    public void updateSystemStatusLabel(String newLabel) {
        systemStatusLabel.setText(newLabel);
    }

    // Update report table
    public void updateReportTable() {
        Object[] rowData = new Object[6];
        // Clear table
        for (int rowCount = (reportTableModel.getRowCount() - 1); rowCount >= 0; rowCount--) {
            reportTableModel.removeRow(rowCount);
            reportTable.revalidate();
        }
        // Update table with new information
        for (Process finishedProcess : Utility.getFinishedProcesses()) {
            rowData[0] = finishedProcess.getProcessId();
            rowData[1] = finishedProcess.getArrivalTime();
            rowData[2] = finishedProcess.getServiceTime();
            rowData[3] = finishedProcess.getFinish_time();
            rowData[4] = finishedProcess.getTurnaround_time();
            rowData[5] = finishedProcess.getNorm_turnaround_time();
            reportTableModel.addRow(rowData);
        }
    }

    // Update throughput label with current throughput
    public void setThroughputLabel() {
        // Calculate current throughput
        throughput = (Utility.getFinishedProcesses().size() / (float) Utility.getSystemClock());
        // Create new label
        String newLabel = "Current Throughput: " + throughput + " process/unit of time";
        // Set new label
        throughputLabel.setText(newLabel);
    }

    // Check if program is currently running
    public boolean checkProgramRunning() {
        return programRunning;
    }

    // Check if system is currently running or if it is paused
    public boolean checkSystemRunning() throws InterruptedException {
        // If the system is running
        if (systemRunningFlag) {
            // If both threads have finished, change system to not running
            if (cpu1_thread.finished() && cpu2_thread.finished()) {
                // Set systemRunning flag to false, change status label to complete, and disable pause button
                systemRunningFlag = false;
                updateSystemStatusLabel("All Processes Complete");
                pauseButton.setEnabled(false);
            }
        }
        return systemRunningFlag;
    }

    // Functional variables
    private List<Process> processList;
    private boolean systemRunningFlag = false;
    private boolean programRunning = true;
    private Semaphore semaphore;
    private float throughput = 0;

    // Thread variables
    private AtomicInteger n = new AtomicInteger();
    private AtomicInteger n2 = new AtomicInteger();
    private thread_run cpu1_thread;
    private thread_run cpu2_thread;

    thread_run.Stepper s1 = () -> {
        n.addAndGet(1);
        Thread.sleep(1);
    };
    thread_run.Stepper s2 = () -> {
        n2.addAndGet(1);
        Thread.sleep(1);
    };

    // GUI variables declaration
    private DefaultTableModel processQueueModel;
    private DefaultTableModel reportTableModel;
    private javax.swing.JTextArea cpu1TextArea;
    private javax.swing.JTextArea cpu2TextArea;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JButton pauseButton;
    private javax.swing.JLabel processQueueLabel;
    private javax.swing.JTable processQueueTable;
    private javax.swing.JTable reportTable;
    private javax.swing.JButton startButton;
    private javax.swing.JLabel systemStatusLabel;
    private javax.swing.JLabel throughputLabel;
    private javax.swing.JLabel timeUnitLabel;
    private javax.swing.JLabel timeUnitLabel2;
    private javax.swing.JTextField timeUnitTextField;
    // End of variables declaration
}
