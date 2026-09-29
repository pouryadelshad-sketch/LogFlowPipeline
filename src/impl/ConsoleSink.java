package impl;

import core.Sink;

public class ConsoleSink<T> implements Sink<T> {
    @Override
    public void consume(T item) {
        System.out.println(item);
    }
}