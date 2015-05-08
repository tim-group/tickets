package com.timgroup.tickets;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TicketFactory {
    private final TicketMacGenerator macGenerator;
    private final char separator;
    private final char[] escape;

    public TicketFactory(TicketMacGenerator macGenerator, char separator, char[] escape) {
        this.macGenerator = macGenerator;
        this.separator = separator;
        this.escape = escape == null ? "".toCharArray() : new String(escape).toCharArray();
    }

    public String marshal(Ticket ticket) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Character, List<String>> e : ticket.data.entrySet()) {
            for (String value : e.getValue()) {
                if (builder.length() > 0) {
                    builder.append(separator);
                }
                builder.append(e.getKey().charValue());
                encodeValue(value, builder);
            }
        }
        String payload = builder.toString();
        if (!payload.isEmpty()) {
            builder.append(',');
        }
        builder.append('x').append(macGenerator.generateMAC(payload));
        return builder.toString();
    }

    public Ticket unmarshal(String input) throws InvalidTicketException {
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
            String payload = input.substring(0, input.length() - macPart.length() - 1);
            ourMac = macGenerator.generateMAC(payload);
        }
        if (!ourMac.equals(theirMac)) {
            throw new TicketMacMismatchException(ourMac, theirMac);
        }
        Ticket ticket = new Ticket();
        for (String part : Arrays.asList(parts)) {
            if (part.isEmpty()) {
                continue;
            }
            char key = part.charAt(0);
            if (key == 'x') {
                continue;
            }
            String value = decodeValue(part.substring(1));
            ticket.add(key, value);
        }
        return ticket;
    }

    private void encodeValue(CharSequence value, StringBuilder output) {
        output.ensureCapacity(output.length() + value.length());
        for (int i = 0; i < value.length(); i++) {
            int c = value.charAt(i);
            if (c >= 256) {
                output.append(String.format("=%04x", c));
            } else if (isReserved(c)) {
                output.append(String.format("+%02x", c));
            } else {
                output.append((char) c);
            }
        }
    }

    private String decodeValue(CharSequence input) {
        StringBuilder builder = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            int c = input.charAt(i);
            if (c == '=') {
                int decoded = Integer.parseInt(input.subSequence(i + 1, i + 5).toString(), 16);
                builder.append((char) decoded);
                i += 4;
            } else if (c == '+') {
                int decoded = Integer.parseInt(input.subSequence(i + 1, i + 3).toString(), 16);
                builder.append((char) decoded);
                i += 2;
            } else {
                builder.append((char) c);
            }
        }
        return builder.toString();
    }

    private boolean isReserved(int ch) {
        if (ch <= 32 || ch >= 127 || ch == separator) {
            return true;
        }
        for (char c : escape) {
            if (ch == c) {
                return true;
            }
        }
        return false;
    }
}
