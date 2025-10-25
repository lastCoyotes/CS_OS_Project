/*
Process represents each process being run by the system. Data for each process is either from the input file or
calculated at run time.

Written by:
Tristan Boler

CS 490
Fall 2021
*/

public class Process {
    // Set the arrival time of the process
    public void setArrivalTime(int new_arrival_time) {
        arrival_time = new_arrival_time;
    }

    // Get the arrival time (when it enters the waiting list and becomes available to run) of the process
    public int getArrivalTime() {
        return arrival_time;
    }

    // Set the process ID
    public void setProcessId(char new_process_id) {
        process_id = new_process_id;
    }

    // Get the process ID
    public char getProcessId() {
        return process_id;
    }

    // Set the service time (how many time units it takes to run) of the process
    public void setServiceTime(int new_service_time) {
        service_time = new_service_time;
    }

    // Get the service time (how many time units it takes to run) of the process
    public int getServiceTime() {
        return service_time;
    }

    // Set the priority of the process
    public void setPriority(int new_priority) {
        priority = new_priority;
    }

    // Get priority of the process
    public int getPriority() {
        return priority;
    }

    // Set the finish time (the unit of time the process was completed)
    public void setFinish_time(int finish_time) {
        this.finish_time = finish_time;
    }

    // Get the finish time (the unit of time the process was completed)
    public int getFinish_time() {
        return finish_time;
    }

    // Set the turnaround time (elapsed time from when the process arrived to when it finished)
    public void setTurnaround_time() {
        turnaround_time = finish_time - arrival_time;
    }

    // Get the turnaround time (elapsed time from when the process arrived to when it finished)
    public int getTurnaround_time() {
        return turnaround_time;
    }

    // Set normalized turnaround time (turnaround time divided by service time)
    public void setNorm_turnaround_time() {
        norm_turnaround_time = (double) turnaround_time / (double) service_time;
    }

    // Get normalized turnaround time (turnaround time divided by service time)
    public double getNorm_turnaround_time() {
        return norm_turnaround_time;
    }

    // Variables
    private int arrival_time;
    private char process_id;
    private int service_time;
    private int priority;
    private int finish_time = 0;
    private int turnaround_time = 0;
    private double norm_turnaround_time = 0.0;
}
