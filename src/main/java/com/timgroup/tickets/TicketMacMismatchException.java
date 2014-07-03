package com.timgroup.tickets;

public class TicketMacMismatchException extends InvalidTicketException {
    private static final long serialVersionUID = 1L;

    public TicketMacMismatchException(String ourMac, String theirMac) {
        super("MAC invalid: " + theirMac);
    }
}
