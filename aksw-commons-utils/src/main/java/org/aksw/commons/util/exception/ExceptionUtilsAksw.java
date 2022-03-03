package org.aksw.commons.util.exception;

import java.net.UnknownHostException;
import java.nio.channels.ClosedChannelException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Utils for dealing with typical situtations w.r.t. Exceptions.
 *
 *
 * @author raven
 *
 */
public class ExceptionUtilsAksw {

    /**
     * Utility function to rethrow an exception unless a condition is met; in that case
     * it is silently swallowed.
     *
     * <pre>
     * rethrowUnless(e, ExceptionUtils::isBrokenPipeException, Foo:isIoException)
     * </pre>
     *
     * @param <T>
     * @param t
     * @param predicates
     */
    @SafeVarargs
    public static <T extends Throwable> void rethrowUnless(T t, Predicate<? super T> ... predicates) {
        boolean anyMatch = Arrays.asList(predicates).stream()
                .anyMatch(p -> p.test(t));

        if(!anyMatch) {
            throw new RuntimeException(t);
        }
    }

    public static boolean isBrokenPipeException(Throwable t) {
        String str = org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage(t);
        boolean result = str != null && str.toLowerCase().contains("broken pipe");
        return result;
    }

    public static boolean isConnectionRefusedException(Throwable t) {
        String str = org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage(t);
        boolean result = str != null && str.toLowerCase().contains("connection refused");
        return result;
    }

    public static boolean isUnknownHostException(Throwable t) {
        boolean result = isRootCauseInstanceOf(t, UnknownHostException.class);
        return result;
    }

//    public static boolean isConnectException(Throwable t) {
//        Throwable rootCause = org.apache.commons.lang3.exception.ExceptionUtils.getRootCause(t);
//        boolean result = rootCause != null && rootCause instanceof ConnectException;
//        return result;
//    }
    public static Predicate<Throwable> isRootCauseInstanceOf(Class<?> superClass) {
        return arg -> {
            boolean r = isRootCauseInstanceOf(arg, superClass);
            return r;
        };
    }

    public static boolean isRootCauseInstanceOf(Throwable t, Class<?> superClass) {
        Throwable rootCause = org.apache.commons.lang3.exception.ExceptionUtils.getRootCause(t);
        boolean r = rootCause != null && superClass.isInstance(rootCause);
        return r;
    }


    public static void rethrowIfNotBrokenPipe(Throwable t) {
        rethrowUnless(t, ExceptionUtilsAksw::isBrokenPipeException);
    }



    @SafeVarargs
    public static <T extends Throwable> void forwardRootCauseUnless(T t, Consumer<? super Throwable> handler, Predicate<? super T> ... predicates) {
        boolean anyMatch = Arrays.asList(predicates).stream()
                .anyMatch(p -> p.test(t));

        if(!anyMatch) {
            Throwable rootCause = org.apache.commons.lang3.exception.ExceptionUtils.getRootCause(t);
            handler.accept(rootCause);
        }
    }

    /**
     * Forward the root cause message of the throwable 't' to 'handler' unless any of the 'predicates' evaluates
     * to true. Useful to e.g. silently suppress broken pipe exceptions in shell scripting pipes.
     *
     * The handler is typically something like logger::warn or logger::error.
     *
     */
    @SafeVarargs
    public static <T extends Throwable> void forwardRootCauseMessageUnless(T t, Consumer<? super String> handler, Predicate<? super T> ... predicates) {
        boolean anyMatch = Arrays.asList(predicates).stream()
                .anyMatch(p -> p.test(t));

        if(!anyMatch) {
            String rootCauseMsg = org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage(t);
            handler.accept(rootCauseMsg);
        }
    }


    /** Check the stack trace for whether it contains an instance of any of the given exceptions
        classes and return that instance. Arguments are checked in order. */
    @SafeVarargs
    public static Optional<Throwable> unwrap(Throwable given, Class<? extends Throwable>... priorities) {
        Optional<Throwable> result = unwrap(given, Arrays.asList(priorities));
        return result;
    }

    public static Optional<Throwable> unwrap(Throwable given, List<Class<? extends Throwable>> priorities) {

        // Iterate the classes by priority and return the first match
        Optional<Throwable> result = priorities.stream()
            .map(priority -> ExceptionUtils.indexOfType(given, priority))
            .filter(index -> index >= 0)
            .map(ExceptionUtils.getThrowableList(given)::get)
            .findFirst();

        return result;
    }

    public static boolean isClosedChannelException(Throwable t) {
        boolean result = t instanceof ClosedChannelException;
        return result;
    }

    @SafeVarargs
    public static void rethrowUnlessRootCauseMatches(
            Throwable e,
            Consumer<? super Predicate<? super Throwable>> firstMatchingConditionCallback,
            Predicate<? super Throwable>... conditions) {
        Throwable rootCause = ExceptionUtils.getRootCause(e);

        Predicate<? super Throwable> match = Arrays.asList(conditions).stream()
            .filter(p -> p.test(rootCause))
            .findFirst()
            .orElse(null);

        if(match == null) {
            throw new RuntimeException(e);
        } else {
            firstMatchingConditionCallback.accept(match);
        }
    }

    @SafeVarargs
    public static void rethrowUnlessRootCauseMatches(Throwable e, Predicate<? super Throwable>... conditions) {
        rethrowUnlessRootCauseMatches(e, condition -> {}, conditions);
    }
}
