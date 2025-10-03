package com.ufg.hemoubiquitous_monitor.exception;

public class InvalidHemoglobinException extends Exception {
    public InvalidHemoglobinException() {
        super("The observation is not a valid hemoglobin");
    }
}
