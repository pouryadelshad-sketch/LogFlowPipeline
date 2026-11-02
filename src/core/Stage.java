package core;

public interface Stage<I, O> {
    void process(I input, Emitter<O> emitter);

    // Defaults to FAIL_FAST so older stages don't break
    default ErrorPolicy getErrorPolicy() {
        return ErrorPolicy.FAIL_FAST;
    }
}