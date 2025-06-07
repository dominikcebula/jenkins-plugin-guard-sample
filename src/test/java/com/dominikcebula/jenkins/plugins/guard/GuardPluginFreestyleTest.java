package com.dominikcebula.jenkins.plugins.guard;

import hudson.model.*;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.concurrent.ExecutionException;

public class GuardPluginFreestyleTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void shouldPassChecks() throws Exception {
        // Create a freestyle project with a valid CHG_NUMBER parameter
        FreeStyleProject project = jenkins.createFreeStyleProject("test-freestyle-pass");

        // Add CHG_NUMBER parameter with valid value
        ParametersDefinitionProperty params = new ParametersDefinitionProperty(
                new StringParameterDefinition("CHG_NUMBER", "CHG123", "Change Request Number")
        );
        project.addProperty(params);

        // Run the job
        Run<?, ?> jobRun = scheduleJob(project);

        // Verify the job passed
        jenkins.assertBuildStatus(Result.SUCCESS, jobRun);
        jenkins.assertLogContains("[GUARD] ✅ Pre-check Successful", project.getLastBuild());
        jenkins.assertLogContains("[GUARD] ✅ Post-check Successful", project.getLastBuild());
    }

    @Test
    public void shouldFailChecksBecauseOfMissingChangeRequestNumber() throws Exception {
        // Create a freestyle project without CHG_NUMBER parameter
        FreeStyleProject project = jenkins.createFreeStyleProject("test-freestyle-missing-param");

        // Run the job
        Run<?, ?> jobRun = scheduleJob(project);

        // Verify the job log contains the expected message
        jenkins.assertBuildStatus(Result.FAILURE, jobRun);
        jenkins.assertLogContains("[GUARD] ❌ Pre-check Failed – Change Request Number parameter is empty", project.getLastBuild());
    }

    @Test
    public void shouldFailChecksBecauseOfEmptyChangeRequestNumber() throws Exception {
        // Create a freestyle project with an empty CHG_NUMBER parameter
        FreeStyleProject project = jenkins.createFreeStyleProject("test-freestyle-empty-param");

        // Add CHG_NUMBER parameter with empty value
        ParametersDefinitionProperty params = new ParametersDefinitionProperty(
                new StringParameterDefinition("CHG_NUMBER", "", "Change Request Number")
        );
        project.addProperty(params);

        // Run the job
        Run<?, ?> jobRun = scheduleJob(project);

        // Verify the job log contains the expected message
        jenkins.assertBuildStatus(Result.FAILURE, jobRun);
        jenkins.assertLogContains("[GUARD] ❌ Pre-check Failed – Change Request Number parameter is empty", project.getLastBuild());
    }

    @Test
    public void shouldFailChecksBecauseOfNonPrefixedChangeRequestNumber() throws Exception {
        // Create a freestyle project with a non-prefixed CHG_NUMBER parameter
        FreeStyleProject project = jenkins.createFreeStyleProject("test-freestyle-non-prefixed-param");

        // Add CHG_NUMBER parameter with non-prefixed value
        ParametersDefinitionProperty params = new ParametersDefinitionProperty(
                new StringParameterDefinition("CHG_NUMBER", "123456", "Change Request Number")
        );
        project.addProperty(params);

        // Run the job
        Run<?, ?> jobRun = scheduleJob(project);

        // Verify the job log contains the expected message
        jenkins.assertBuildStatus(Result.FAILURE, jobRun);
        jenkins.assertLogContains("[GUARD] ❌ Pre-check Failed – Change Request Number parameter should start with CHG", project.getLastBuild());
    }

    private Run<?, ?> scheduleJob(FreeStyleProject project) throws ExecutionException, InterruptedException {
        return project.scheduleBuild2(0).get();
    }
}
