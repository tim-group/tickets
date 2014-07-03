package com.timgroup.tickets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Ticket {
    private final TicketMacGenerator macGenerator;
    private final Map<Character, List<String>> data = new LinkedHashMap<Character, List<String>>();

    public Ticket(TicketMacGenerator macGenerator) {
        this.macGenerator = macGenerator;
    }

    public String marshal() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Character, List<String>> e : data.entrySet()) {
            for (String value : e.getValue()) {
                if (builder.length() > 0) {
                    builder.append(',');
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
        data.clear();
        for (String part : Arrays.asList(parts)) {
            if (part.isEmpty()) {
                continue;
            }
            char key = part.charAt(0);
            if (key == 'x') {
                continue;
            }
            String value = decodeValue(part.substring(1));
            add(key, value);
        }
    }

    public List<String> get(char key) {
        if (!data.containsKey(key)) {
            return Collections.emptyList();
        }
        return data.get(key);
    }

    public String getOne(char key) {
        if (!data.containsKey(key)) {
            return null;
        }
        List<String> list = data.get(key);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public Set<Character> keySet() {
        return data.keySet();
    }

    public void add(char key, String value) {
        if (key == 'x') {
            throw new IllegalArgumentException("key 'x' is reserved for the MAC");
        }
        List<String> list = data.get(key);
        if (list == null) {
            list = new ArrayList<String>();
            data.put(key, list);
        }
        list.add(value);
    }

    public void set(char key, String value) {
        if (key == 'x') {
            throw new IllegalArgumentException("key 'x' is reserved for the MAC");
        }
        data.put(key, new ArrayList<String>(Arrays.asList(value)));
    }

    public void set(char key, List<String> value) {
        if (key == 'x') {
            throw new IllegalArgumentException("key 'x' is reserved for the MAC");
        }
        data.put(key, new ArrayList<String>(value));
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
        return ch <= 32 || ch >= 127 || ch == '+' || ch == '=' || ch == ',';
    }
}
