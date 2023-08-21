package com.eternalcode.discordapp.observer;

@FunctionalInterface
public interface Observer<T> {

    void update(T t);

}
