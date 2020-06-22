package cn.thens.xflow.flow;

import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

/**
 * @author 7hens
 */
public class CompositeException extends RuntimeException {
    private final List<Throwable> exceptions;

    public CompositeException(List<Throwable> exceptions) {
        this.exceptions = Collections.unmodifiableList(exceptions);
    }

    public List<Throwable> getExceptions() {
        return exceptions;
    }

    @Override
    public void printStackTrace(PrintStream s) {
        printStackTrace(new PrintWriter(new OutputStreamWriter(s)));
    }

    @Override
    public void printStackTrace(PrintWriter s) {
        s.println("CompositeException: {");
        for (Throwable exception : exceptions) {
            exception.printStackTrace(s);
        }
        s.println("}: CompositeException");
    }
}
