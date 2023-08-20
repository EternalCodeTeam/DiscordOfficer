package com.eternalcode.discordapp.observer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ObserverRegistry {

    private final Map<Class<?>, Set<Observer<?>>> observers = new HashMap<>();

    public <E> void observe(Class<E> eventType, Observer<E> observer) {
        this.observers.computeIfAbsent(eventType, key -> new HashSet<>()).add(observer);
    }

    @SuppressWarnings("unchecked")
    public <E> void publish(E event) {
        Class<E> typeEvent = (Class<E>) event.getClass();

        for (Observer<?> observer : this.observers.computeIfAbsent(typeEvent, key -> new HashSet<>())) {
            Observer<E> eventObserver = (Observer<E>) observer;
            eventObserver.update(event);
        }
    }

}
