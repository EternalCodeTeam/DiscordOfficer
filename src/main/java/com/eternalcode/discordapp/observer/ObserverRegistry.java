package com.eternalcode.discordapp.observer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ObserverRegistry {

    private final Map<Class<?>, Set<Observer<?>>> observers = new HashMap<>();

    public <EVENT> void observe(Class<EVENT> eventType, Observer<EVENT> observer) {
        this.observers.computeIfAbsent(eventType, key -> new HashSet<>()).add(observer);
    }

    @SuppressWarnings("unchecked")
    public <EVENT> void publish(EVENT event) {
        Class<EVENT> typeEvent = (Class<EVENT>) event.getClass();

        for (Observer<?> observer : this.observers.computeIfAbsent(typeEvent, key -> new HashSet<>())) {
            Observer<EVENT> eventObserver = (Observer<EVENT>) observer;
            eventObserver.update(event);
        }
    }

}
