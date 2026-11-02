package core;

public interface Sink<I> {
    default void open() {}
    void consume(I item);
    default void close() {}
}