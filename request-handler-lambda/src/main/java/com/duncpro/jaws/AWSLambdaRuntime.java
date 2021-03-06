package com.duncpro.jaws;

import com.amazonaws.services.lambda.runtime.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Represents a single invocation of an AWS Lambda function.
 * This object is created when the invocation begins and is released after the invocation finishes.
 */
public class AWSLambdaRuntime {
    private final Queue<Runnable> earlyShutdownHooks = new ConcurrentLinkedQueue<>();
    private final Queue<Runnable> lateShutdownHooks = new ConcurrentLinkedQueue<>();
    private final Supplier<Integer> remainingTime;

    public AWSLambdaRuntime(Supplier<Integer> remainingTime) {
        this.remainingTime = remainingTime;
    }

    public static AWSLambdaRuntime fromLambdaContext(Context context) {
        return new AWSLambdaRuntime(context::getRemainingTimeInMillis);
    }

    /**
     * Register a function to run immediately before the Lambda invocation finishes. This class is intended to provide
     * a mechanism which is equivalent to the JVM {@link Runtime#addShutdownHook(Thread)} method. Consider using
     * this method to cleanup database connections and close HTTP clients.
     *
     * The provided runnable is executed immediately after
     * {@link LNTRequestHandler#handleRequest(Object, Context, AWSLambdaRuntime)} returns normally or exceptionally.
     * Shutdown hooks are not executed if the Lambda function times out before
     * {@link LNTRequestHandler#handleRequest(Object, Context, AWSLambdaRuntime)} finishes executing.
     */
    public void addShutdownHook(Runnable run) {
        addShutdownHook(run, ShutdownHookPriority.EARLY);
    }

    public void addShutdownHook(Runnable run, ShutdownHookPriority priority) {
        switch (priority) {
            case EARLY:
                earlyShutdownHooks.add(run);
                break;
            case LATE:
                lateShutdownHooks.add(run);
                break;
        }
    }

    private void runShutdownHooksOrdered(ExecutorService executor) {
        Stream.generate(earlyShutdownHooks::poll)
                .takeWhile(Objects::nonNull)
                .map(hook -> CompletableFuture.runAsync(hook, executor))
                .forEach(CompletableFuture::join);

        Stream.generate(lateShutdownHooks::poll)
                .takeWhile(Objects::nonNull)
                .map(hook -> CompletableFuture.runAsync(hook, executor))
                .forEach(CompletableFuture::join);
    }

    /**
     * Executes all registered shutdown hooks simultaneously. This function blocks until all registered shutdown
     * hooks have completed.
     */
    public void runShutdownHooks() {
        // This executor will run until all hooks have finished
        ExecutorService masterExecutor = Executors.newSingleThreadExecutor();

        ExecutorService hookExecutor = Executors.newCachedThreadPool();
        masterExecutor.submit(() -> runShutdownHooksOrdered(hookExecutor));
        masterExecutor.shutdown();


        boolean didFinishExecuting = false;
        try {
            didFinishExecuting = masterExecutor.awaitTermination(remainingTime.get().longValue(),
                            TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.error("The main thread was interrupted during the shutdown procedure." +
                    " Some shutdown hooks might not finish executing.", e);
        }

        if (!didFinishExecuting) {
            hookExecutor.shutdownNow();
            logger.error("One or more shutdown tasks did not finish executing before the Lambda function timed out." +
                    " Consider increasing the timeout duration of the function inside of the CDK script.");
        }
    }

    /**
     * Returns the duration (in milliseconds) until this Lambda instance is killed by AWS.
     * This is an acceptable timeout to use inside of shutdown hooks when closing connections and releasing resources.
     */
    public int getDurationUntilLambdaTimeout() {
        return remainingTime.get();
    }

    private static final Logger logger = LoggerFactory.getLogger(AWSLambdaRuntime.class);
}
