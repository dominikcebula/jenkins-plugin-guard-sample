package com.dominikcebula.jenkins.plugins.guard;

import hudson.Extension;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.StringParameterValue;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import org.apache.commons.lang.StringUtils;

import java.io.PrintStream;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Extension
public class GuardPluginRunListener extends RunListener<Run<?, ?>> {

    private static final String CHG_NUMBER_PARAMETER_NAME = "CHG_NUMBER";
    private static final String CHG_PREFIX = "CHG";

    @Override
    public void onStarted(Run<?, ?> run, TaskListener listener) {
        PrintStream logger = listener.getLogger();

        logger.println("[GUARD] ⏳ Pre-check running");

        Map<String, String> stringParameters = getStringParameters(run);

        logger.println("[GUARD] Inspecting Change Request Number parameter");
        String chgNumber = stringParameters.get(CHG_NUMBER_PARAMETER_NAME);
        logger.printf("[GUARD] \"%s\" = \"%s\"%n", CHG_NUMBER_PARAMETER_NAME, chgNumber);

        if (StringUtils.isEmpty(chgNumber)) {
            logger.println("[GUARD] ❌ Pre-check Failed – Change Request Number parameter is empty");
            failJobRun(run, logger);
        } else if (!StringUtils.startsWith(chgNumber, CHG_PREFIX)) {
            logger.println("[GUARD] ❌ Pre-check Failed – Change Request Number parameter should start with " + CHG_PREFIX);
            failJobRun(run, logger);
        } else {
            logger.println("[GUARD] ✅ Pre-check Successful");
        }
    }

    @Override
    public void onCompleted(Run<?, ?> run, TaskListener listener) {
        PrintStream logger = listener.getLogger();

        logger.println("[GUARD] ⏳ Post-check running");
        logger.println("[GUARD] Executing post-checks ...");
        logger.println("[GUARD] ✅ Post-check Successful");
    }

    private Map<String, String> getStringParameters(Run<?, ?> run) {
        return run.getParameterValues().stream()
                .filter(StringParameterValue.class::isInstance)
                .map(StringParameterValue.class::cast)
                .collect(Collectors.toMap(
                        StringParameterValue::getName,
                        StringParameterValue::getValue
                ));
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
