/*
thread_run handles the execution of each thread, representing an individual CPU. It will run until the original
process list is empty and there are no waiting processes. If there is a waiting process, it will grab the process
from the list, execute for the service time of the process, checking for pauses every unit of time, and adds the process
to the finished list when completed.

Written by:
Tristan Boler

CS 490
Fall 2021
*/

import java.util.List;

// Generates a thread that opens a process, runs it, and then looks for another until no more processes remain
public abstract class thread_run implements Runnable {
    @Override
    // The core run mechanism.
    public void run() {
        // Track my current thread.
        me = Thread.currentThread();
        try {
            // While there is work to be done
            while (!finished()) {
                // If thread has been paused, sleep for one cycle at a time until resumed.
                while (threadPaused){
                    Thread.sleep(Utility.getExecutionSpeed());
                }
                // If waitingProcess list is not empty
                if (!Utility.getWaitingProcessList().isEmpty()) {
                    // Get a process from waiting list
                    currentProcess = Utility.getNextWaitingProcess();
                    // Remove process from waiting list
                    Utility.removeWaitingProcess();
                    // Update GUI process table
                    GUI.populateProcessTable();
                    // Update CPU text field
                    currentServiceTime = currentProcess.getServiceTime();
                    GUI.updateCpuTextField(currentProcess, currentServiceTime, thread_no);

                    // Reset working counter
                    workCounter = 0;
                    // Run process for necessary amount of time, checking for pauses after each unit of time
                    // While the process still requires more work
                    while (workCounter < currentProcess.getServiceTime()) {
                        // If thread has been paused, sleep for one cycle at a time until resumed.
                        while (threadPaused){
                            Thread.sleep(Utility.getExecutionSpeed());
                        }
                        // Get current time
                        processStartTime = System.currentTimeMillis();
                        // Update CPU text field
                        GUI.updateCpuTextField(currentProcess, currentServiceTime - workCounter, thread_no);

                        // If the loop took less than the time set for one cycle
                        processElapsedTime = System.currentTimeMillis() - processStartTime;
                        if (processElapsedTime < Utility.getExecutionSpeed()) {
                            // Sleep for any remaining time
                            Thread.sleep(Utility.getExecutionSpeed() - processElapsedTime);
                        }
                        // Increase unit of work done
                        workCounter +=1;
                        // Record current time in case Process is finished
                        tempFinishTime = Utility.getSystemClock();
                    }

                    // Add process to finished list
                    currentProcess.setFinish_time(tempFinishTime);
                    currentProcess.setTurnaround_time();
                    currentProcess.setNorm_turnaround_time();
                    Utility.addFinishedProcess(currentProcess);
                    // Update GUI report table
                    GUI.updateReportTable();
                    step();
                }
                // If there are no waiting processes, sleep for a cycle
                else {
                    Thread.sleep(Utility.getExecutionSpeed());
                }
                // If all processes have finished running, shut down the thread
                if (processList.isEmpty() && Utility.getWaitingProcessList().isEmpty()) {
                    GUI.updateCpuTextField(null, -1, thread_no);
                    cancel();
                }
            }
        } catch (
                Throwable ex) {
            // Just fall out when exception is thrown.
            ex.printStackTrace();
        }
    }


    // Check if the work is finished
    public boolean finished() {
        return cancelled || me.isInterrupted();
    }

    // Pause the thread
    public void pause() {
        threadPaused = true;
    }

    // Resume the thread
    public void resume() {
        threadPaused = false;
    }

    // Stop
    public void cancel() {
        // Stop everything.
        cancelled = true;
    }

    // Start the thread
    public void start() {
        // Wrap me in a thread and start
        new Thread(this).start();
    }

    // Get the exception that was thrown to stop the thread or null if the thread was cancelled.
    public Exception getThrown() {
        return thrown;
    }

    // Expose my Thread.
    public Thread getThread() {
        return me;
    }


    // Any thrown exception stops the whole process.
    public abstract void step() throws Exception;

    // Factory to wrap a Stepper in a Pauseable Thread
    public static thread_run make(Stepper stepper,
                                  List<Process> processList,
                                  SchedulerGUI GUI,
                                  int thread_no) {
        // That's the thread they can pause/resume.
        return new StepperThread(stepper, processList, GUI, thread_no);
    }

    // One of these must be used.
    public interface Stepper {
        // A Stepper has a step method.
        // Any exception thrown causes the enclosing thread to stop.
        void step() throws Exception;
    }

    // Holder for a Stepper.
    private static class StepperThread extends thread_run {
        private final Stepper stepper;

        StepperThread(Stepper stepper, List<Process> inputList,
                      SchedulerGUI GUI, int thread_no) {
            this.stepper = stepper;
            this.processList = inputList;
            this.GUI = GUI;
            this.thread_no = thread_no;
        }

        @Override
        public void step() throws Exception {
            stepper.step();
        }
    }

    // Variables
    List<Process> processList;
    // Flag to cancel the whole process.
    private volatile boolean cancelled = false;
    // The exception that cause it to finish.
    private Exception thrown = null;
    // The thread that is me.
    private Thread me = null;
    protected SchedulerGUI GUI;
    protected int thread_no;
    private Process currentProcess = null;
    private int workCounter = 0;
    private int tempFinishTime = 0;
    private int currentServiceTime = 0;
    private long processStartTime = 0;
    private long processElapsedTime = 0;
    private  boolean threadPaused = false;
}
