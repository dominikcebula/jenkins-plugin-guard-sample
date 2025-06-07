package com.dominikcebula.jenkins.plugins.guard;

import hudson.Extension;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

import java.io.PrintStream;

import static java.util.Objects.requireNonNull;

@Extension
public class GuardPluginRunListener extends RunListener<Run<?, ?>> {

    @Override
    public void onStarted(Run<?, ?> run, TaskListener listener) {
        PrintStream logger = listener.getLogger();

        logger.println("[GUARD] Pre-check running");
        logger.println("[GUARD] ❌ failing build – aborting");

        failJobRun(run, logger);
    }

    @Override
    public void onCompleted(Run<?, ?> run, TaskListener listener) {
        PrintStream logger = listener.getLogger();

        logger.println("[GUARD] Post-check → " + run.getResult());
    }

    private void failJobRun(Run<?, ?> run, PrintStream logger) {
        run.setResult(Result.FAILURE);
        try {
            requireNonNull(run.getExecutor()).interrupt();
        } catch (Exception e) {
            logger.println("[GUARD] Failed to interrupt build: " + e.getMessage());
            throw e;
        }
    }
}
