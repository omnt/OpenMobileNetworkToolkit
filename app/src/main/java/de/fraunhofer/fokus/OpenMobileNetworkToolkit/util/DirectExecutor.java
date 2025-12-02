package de.fraunhofer.fokus.OpenMobileNetworkToolkit.util;

import java.util.concurrent.Executor;

/**
 * An Executor that executes submitted tasks immediately on the calling thread.
 */
public class DirectExecutor implements Executor {

    /**
     * Executes the given command (Runnable) immediately on the current thread.
     * @param command The Runnable to execute.
     */
    @Override
    public void execute(Runnable command) {
        command.run();
    }
}