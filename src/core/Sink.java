package core;

public interface Sink<I> {
    void consume(I item);
}