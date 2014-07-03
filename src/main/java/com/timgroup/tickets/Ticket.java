package com.timgroup.tickets;

public class Ticket {
    private final TicketMacGenerator macGenerator;

    public Ticket(TicketMacGenerator macGenerator) {
        this.macGenerator = macGenerator;
    }

    public String marshal() {
        return "x" + macGenerator.generateMAC("");
    }

    public void unmarshal(String input) throws InvalidTicketException {
        String[] parts = input.split(",");
        String macPart = parts[parts.length - 1];
        if (macPart.length() == 0 || !macPart.startsWith("x")) {
            throw new InvalidTicketException("Ticket string does not end with MAC");
        }
        String theirMac = macPart.substring(1);
        String ourMac;
        if (parts.length == 1) {
            ourMac = macGenerator.generateMAC("");
        } else {
            String payload = input.substring(input.length() - macPart.length() - 1);
            ourMac = macGenerator.generateMAC(payload);
        }
        if (!ourMac.equals(theirMac)) {
            throw new TicketMacMismatchException(ourMac, theirMac);
        }
    }
}
