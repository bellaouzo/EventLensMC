package dev.bellaouzo.eventlens.domain.exceptions;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public final class ExceptionInbox {

    public static final int DEFAULT_CAPACITY = 64;

    private final int capacity;
    private final ArrayDeque<ExceptionInboxEntry> entries = new ArrayDeque<>();

    public ExceptionInbox() {
        this(DEFAULT_CAPACITY);
    }

    public ExceptionInbox(int capacity) {
        this.capacity = Math.max(1, capacity);
    }

    public synchronized void add(ExceptionInboxEntry entry) {
        entries.addFirst(entry);
        while (entries.size() > capacity) {
            entries.removeLast();
        }
    }

    public synchronized List<ExceptionInboxEntry> list() {
        return List.copyOf(entries);
    }

    public synchronized List<ExceptionInboxEntry> page(int page, int pageSize) {
        List<ExceptionInboxEntry> all = list();
        int from = Math.max(0, (page - 1) * pageSize);
        if (from >= all.size()) {
            return List.of();
        }
        return new ArrayList<>(all.subList(from, Math.min(from + pageSize, all.size())));
    }

    public synchronized int size() {
        return entries.size();
    }
}
