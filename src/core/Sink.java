package core;

public interface Sink<I> {
    void consume(I item);
    default void open() { }
    default void close() { }
}