/*
Main driver for the program. Sets up process list from input file, GUI, and semaphore.
Contains a loop that runs while program and determines when to increase the clock,
when a process has arrived and adds it to the waiting list, and updates the GUI.

Written by:
Tristan Boler

CS 490
Fall 2021
*/

import java.util.*;
import java.util.concurrent.Semaphore;

public class main extends Thread {
    public static void main(String[] args) throws InterruptedException {
        // Create a main thread to help with execution timing
        Thread mainThread = Thread.currentThread();

        // Get process list from input file
        List<Process> processList = Utility.readFile();
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(SchedulerGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SchedulerGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SchedulerGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SchedulerGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        // Create GUI
        SchedulerGUI gui = new SchedulerGUI(processList);
        // List to store processes removed from input list when placed in waiting list
        List<Process> removedProcesses = new ArrayList<>();
        Process tempProcess = null;

        // Do initial check for any processes that arrive at time 0
        for (int i = 0; i < processList.size(); i++) {
            tempProcess = processList.get(i);
            // If a process has arrived
            if (tempProcess.getArrivalTime() <= Utility.getSystemClock()) {
                // Add the process to the waiting list
                Utility.addWaitingProcess(tempProcess);
                // Update the waiting process table
                gui.populateProcessTable();
                // Remove the process from original list
                processList.remove(tempProcess);
            }
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                gui.setVisible(true);
            }
        });

        // Variables to determine timing of "clock cycle"
        long startTime = 0;
        long elapsedTime = 0;

        // While the program is running, loop every "clock cycle" and do certain actions
        while(gui.checkProgramRunning()) {
            // Get current time
            startTime = System.currentTimeMillis();
            // If system is running
            if (gui.checkSystemRunning()) {
                // Increase "clock"
                Utility.increaseSystemClock();
                // Check if any processes are available to add to waiting queue
                for (int i = 0; i < processList.size(); i++) {
                    tempProcess = processList.get(i);
                    // If a process has arrived
                    if (tempProcess.getArrivalTime() <= Utility.getSystemClock()) {
                        // Add the process to the waiting list
                        Utility.addWaitingProcess(tempProcess);
                        // Update the waiting process table
                        gui.populateProcessTable();
                        processList.remove(tempProcess);
                    }
                }

                // Calculate and display current throughput
                gui.setThroughputLabel();
            }

            // If the loop took less than the time set for one cycle
            elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime < Utility.getExecutionSpeed()) {
                // Sleep for any remaining time
                mainThread.sleep(Utility.getExecutionSpeed() - elapsedTime);
            }
        }
    }
}
