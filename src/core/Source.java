package core;

public interface Source<O> {
    default void open() {}
    boolean hasNext();
    O next();
    default void close() {}
}