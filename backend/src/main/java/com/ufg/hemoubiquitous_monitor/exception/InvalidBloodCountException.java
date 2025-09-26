package com.ufg.hemoubiquitous_monitor.exception;

public class InvalidBloodCountException extends Exception {
    public InvalidBloodCountException() {
        super("The observation is not a valid Blood Count");
    }
}
