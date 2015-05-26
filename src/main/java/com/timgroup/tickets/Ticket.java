package com.timgroup.tickets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Ticket {
    final Map<Character, List<String>> data = new LinkedHashMap<Character, List<String>>();

    public List<String> get(char key) {
        if (!data.containsKey(key)) {
            return Collections.emptyList();
        }
        return data.get(key);
    }

    public Optional<String> getOne(char key) {
        if (!data.containsKey(key)) {
            return Optional.empty();
        }
        List<String> list = data.get(key);
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(list.get(0));
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
}
