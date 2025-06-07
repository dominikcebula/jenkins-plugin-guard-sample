package com.dominikcebula.jenkins.plugins.guard;

import hudson.model.*;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class GuardPluginPipelineTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void shouldPassChecks() throws Exception {
        // Create a pipeline job with a valid CHG_NUMBER parameter
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-pipeline-pass");

        // Define the parameter
        ParametersDefinitionProperty paramsDef = new ParametersDefinitionProperty(
                new StringParameterDefinition("CHG_NUMBER", "CHG123", "Change Request Number")
        );
        job.addProperty(paramsDef);

        job.setDefinition(new CpsFlowDefinition(
                "pipeline {\n" +
                        "  agent any\n" +
                        "  stages {\n" +
                        "    stage('Test') {\n" +
                        "      steps {\n" +
                        "        echo 'Running test with CHG_NUMBER=${params.CHG_NUMBER}'\n" +
                        "      }\n" +
                        "    }\n" +
                        "  }\n" +
                        "}", true));

        // Run the job with the parameter
        WorkflowRun run = job.scheduleBuild2(0, new ParametersAction(
                new StringParameterValue("CHG_NUMBER", "CHG123")
        )).get();

        // Verify the job passed
        jenkins.assertBuildStatus(Result.SUCCESS, run);
        jenkins.assertLogContains("[GUARD] ✅ Pre-check Successful", run);
        jenkins.assertLogContains("[GUARD] ✅ Post-check Successful", run);
    }

    @Test
    public void shouldFailChecksBecauseOfMissingChangeRequestNumber() throws Exception {
        // Create a pipeline job without CHG_NUMBER parameter
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-pipeline-missing-param");
        job.setDefinition(new CpsFlowDefinition(
                "pipeline {\n" +
                        "  agent any\n" +
                        "  stages {\n" +
                        "    stage('Test') {\n" +
                        "      steps {\n" +
                        "        echo 'Running test without CHG_NUMBER parameter'\n" +
                        "      }\n" +
                        "    }\n" +
                        "  }\n" +
                        "}", true));

        // Run the job without any parameters
        WorkflowRun run = job.scheduleBuild2(0).get();

        // Verify the job was aborted
        jenkins.assertBuildStatus(Result.ABORTED, run);
        jenkins.assertLogContains("[GUARD] ❌ Pre-check Failed – Change Request Number parameter is empty", run);
    }

    @Test
    public void shouldFailChecksBecauseOfEmptyChangeRequestNumber() throws Exception {
        // Create a pipeline job with an empty CHG_NUMBER parameter
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-pipeline-empty-param");

        // Define the parameter
        ParametersDefinitionProperty paramsDef = new ParametersDefinitionProperty(
                new StringParameterDefinition("CHG_NUMBER", "", "Change Request Number")
        );
        job.addProperty(paramsDef);

        job.setDefinition(new CpsFlowDefinition(
                "pipeline {\n" +
                        "  agent any\n" +
                        "  stages {\n" +
                        "    stage('Test') {\n" +
                        "      steps {\n" +
                        "        echo 'Running test with empty CHG_NUMBER parameter'\n" +
                        "      }\n" +
                        "    }\n" +
                        "  }\n" +
                        "}", true));

        // Run the job with an empty parameter
        WorkflowRun run = job.scheduleBuild2(0, new ParametersAction(
                new StringParameterValue("CHG_NUMBER", "")
        )).get();

        // Verify the job was aborted
        jenkins.assertBuildStatus(Result.ABORTED, run);
        jenkins.assertLogContains("[GUARD] ❌ Pre-check Failed – Change Request Number parameter is empty", run);
    }

    @Test
    public void shouldFailChecksBecauseOfNonPrefixedChangeRequestNumber() throws Exception {
        // Create a pipeline job with a non-prefixed CHG_NUMBER parameter
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-pipeline-non-prefixed-param");

        // Define the parameter
        ParametersDefinitionProperty paramsDef = new ParametersDefinitionProperty(
                new StringParameterDefinition("CHG_NUMBER", "123456", "Change Request Number")
        );
        job.addProperty(paramsDef);

        job.setDefinition(new CpsFlowDefinition(
                "pipeline {\n" +
                        "  agent any\n" +
                        "  stages {\n" +
                        "    stage('Test') {\n" +
                        "      steps {\n" +
                        "        echo 'Running test with non-prefixed CHG_NUMBER=${params.CHG_NUMBER}'\n" +
                        "      }\n" +
                        "    }\n" +
                        "  }\n" +
                        "}", true));

        // Run the job with a non-prefixed parameter
        WorkflowRun run = job.scheduleBuild2(0, new ParametersAction(
                new StringParameterValue("CHG_NUMBER", "123456")
        )).get();

        // Verify the job was aborted
        jenkins.assertBuildStatus(Result.ABORTED, run);
        jenkins.assertLogContains("[GUARD] ❌ Pre-check Failed – Change Request Number parameter should start with CHG", run);
    }
}
