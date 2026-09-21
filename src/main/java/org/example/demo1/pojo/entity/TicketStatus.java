package org.example.demo1.pojo.entity;

import java.util.Set;

public final class TicketStatus {
    public static final String PENDING = "PENDING";
    public static final String ACCEPTED = "ACCEPTED";
    public static final String PROCESSING = "PROCESSING";
    public static final String DONE = "DONE";
    public static final String REJECTED = "REJECTED";
    public static final String CANCELLED = "CANCELLED";

    public static final Set<String> IN_PROGRESS = Set.of(PENDING, ACCEPTED, PROCESSING);
    public static final Set<String> TERMINAL = Set.of(DONE, REJECTED, CANCELLED);

    private TicketStatus() {
    }
}
