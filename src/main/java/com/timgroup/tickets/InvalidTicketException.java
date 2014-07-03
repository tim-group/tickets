package com.timgroup.tickets;

public class InvalidTicketException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidTicketException(String message) {
        super(message);
    }
}
