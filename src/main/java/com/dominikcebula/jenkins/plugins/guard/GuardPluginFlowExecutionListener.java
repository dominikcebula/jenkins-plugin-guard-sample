package com.dominikcebula.jenkins.plugins.guard;

import hudson.Extension;
import hudson.model.Result;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.flow.FlowExecutionListener;
import org.jenkinsci.plugins.workflow.flow.FlowExecutionOwner;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.io.IOException;
import java.io.PrintStream;

@Extension
public class GuardPluginFlowExecutionListener extends FlowExecutionListener {

    @Override
    public void onCreated(FlowExecution flowExecution) {
        try {
            FlowExecutionOwner owner = flowExecution.getOwner();
            WorkflowRun workflowRun = (WorkflowRun) owner.getExecutable();
            TaskListener taskListener = owner.getListener();
            PrintStream logger = taskListener.getLogger();

            logger.println("[GUARD] Pre-check running");
            logger.println("[GUARD] ❌ failing pipeline – aborting");
            failPipelineExecution(flowExecution, logger, workflowRun);
        } catch (IOException ioe) {
            System.err.println("[GUARD] IOException before log available: " + ioe.getMessage());
        }
    }

    @Override
    public void onCompleted(FlowExecution flowExecution) {
        try {
            WorkflowRun run = (WorkflowRun) flowExecution.getOwner().getExecutable();
            PrintStream logger = flowExecution.getOwner().getListener().getLogger();

            logger.println("[GUARD] Post-check → " + run.getResult());
        } catch (IOException ioe) {
            System.err.println("[GUARD] IOException after build: " + ioe.getMessage());
        }
    }

    private void failPipelineExecution(FlowExecution flowExecution, PrintStream logger, WorkflowRun workflowRun) throws IOException {
        try {
            flowExecution.interrupt(Result.FAILURE);
        } catch (InterruptedException ie) {
            logger.println("[GUARD] interrupt interrupted: " + ie.getMessage());
            workflowRun.setResult(Result.FAILURE);
        }
    }
}
