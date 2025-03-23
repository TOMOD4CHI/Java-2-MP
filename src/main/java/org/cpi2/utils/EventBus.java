package org.cpi2.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;


public class EventBus {
    
    private static final Map<String, Set<Consumer<Object>>> subscribers = new HashMap<>();
    

    public static void subscribe(String eventType, Consumer<Object> handler) {
        subscribers.computeIfAbsent(eventType, k -> new HashSet<>()).add(handler);
    }
    
    public static void unsubscribe(String eventType, Consumer<Object> handler) {
        if (subscribers.containsKey(eventType)) {
            subscribers.get(eventType).remove(handler);
            if (subscribers.get(eventType).isEmpty()) {
                subscribers.remove(eventType);
            }
        }
    }
    
    public static void publish(String eventType, Object data) {
        if (subscribers.containsKey(eventType)) {
            subscribers.get(eventType).forEach(handler -> handler.accept(data));
        }
    }
}