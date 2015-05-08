package com.timgroup.tickets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TicketFactory {
    private final TicketMacGenerator macGenerator;
    private final char separator;
    private final char[] escapeOthers;
    private final char unicodeEscape;
    private final char reservedEscape;

    public TicketFactory(TicketMacGenerator macGenerator) {
        this(macGenerator, ',', '=', '+', null);
    }

    public TicketFactory(TicketMacGenerator macGenerator, char separator, char unicodeEscape, char reservedEscape, char[] escapeOthers) {
        this.macGenerator = macGenerator;
        this.separator = separator;
        this.unicodeEscape = unicodeEscape;
        this.reservedEscape = reservedEscape;
        this.escapeOthers = escapeOthers == null ? "".toCharArray() : new String(escapeOthers).toCharArray();
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
            builder.append(separator);
        }
        builder.append('x').append(macGenerator.generateMAC(payload));
        return builder.toString();
    }

    public static List<String> split(CharSequence input, char separator) {
        if (input.length() == 0) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<String>();
        int lastpos = -1;
        for (int pos = 0; pos < input.length(); pos++) {
            char ch = input.charAt(pos);
            if (ch == separator) {
                if (lastpos >= 0) {
                    result.add(input.subSequence(lastpos, pos).toString());
                    lastpos = -1;
                }
                // don't add empty parts
            }
            else if (lastpos < 0) {
                lastpos = pos;
            }
        }
        if (lastpos == 0) {
            return Collections.singletonList(input.toString());
        }
        if (lastpos >= 0) {
            result.add(input.subSequence(lastpos, input.length()).toString());
        }
        return result;
    }

    public Ticket unmarshal(String input) throws InvalidTicketException {
        List<String> parts = split(input, separator);
        if (parts.isEmpty()) {
            throw new InvalidTicketException("Ticket string does not end with MAC (looks empty)");
        }
        String macPart = parts.get(parts.size() - 1);
        if (!macPart.startsWith("x")) {
            throw new InvalidTicketException("Ticket string does not end with MAC");
        }
        String theirMac = macPart.substring(1);
        String ourMac;
        if (parts.size() == 1) {
            ourMac = macGenerator.generateMAC("");
        } else {
            String payload = input.substring(0, input.length() - macPart.length() - 1);
            ourMac = macGenerator.generateMAC(payload);
        }
        if (!ourMac.equals(theirMac)) {
            throw new TicketMacMismatchException(ourMac, theirMac);
        }
        Ticket ticket = new Ticket();
        for (String part : parts) {
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
                output.append(String.format("%c%04x", unicodeEscape, c));
            } else if (isReserved(c)) {
                output.append(String.format("%c%02x", reservedEscape, c));
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
        if (ch <= 32 || ch >= 127 || ch == separator || ch == unicodeEscape || ch == reservedEscape) {
            return true;
        }
        for (char c : escapeOthers) {
            if (ch == c) {
                return true;
            }
        }
        return false;
    }
}
