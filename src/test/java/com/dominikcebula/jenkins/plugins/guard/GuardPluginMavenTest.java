package com.dominikcebula.jenkins.plugins.guard;

import hudson.model.*;
import hudson.tasks.Shell;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class GuardPluginMavenTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void shouldPassChecks() throws Exception {
        // Create a Maven-like project with a valid CHG_NUMBER parameter
        FreeStyleProject project = jenkins.createFreeStyleProject("test-maven-pass");

        // Add a shell step to simulate Maven build
        project.getBuildersList().add(new Shell("echo 'Simulating Maven build: mvn clean install'"));

        // Add CHG_NUMBER parameter with valid value
        ParametersDefinitionProperty params = new ParametersDefinitionProperty(
                new StringParameterDefinition("CHG_NUMBER", "CHG123", "Change Request Number")
        );
        project.addProperty(params);

        // Run the job
        FreeStyleBuild jobRun = project.scheduleBuild2(0).get();

        // Verify the job passed
        jenkins.assertBuildStatus(Result.SUCCESS, jobRun);
        jenkins.assertLogContains("[GUARD] ✅ Pre-check Successful", project.getLastBuild());
        jenkins.assertLogContains("[GUARD] ✅ Post-check Successful", project.getLastBuild());
    }

    @Test
    public void shouldFailChecksBecauseOfMissingChangeRequestNumber() throws Exception {
        // Create a Maven-like project without CHG_NUMBER parameter
        FreeStyleProject project = jenkins.createFreeStyleProject("test-maven-missing-param");

        // Add a shell step to simulate Maven build
        project.getBuildersList().add(new Shell("echo 'Simulating Maven build: mvn clean install'"));

        // Run the job
        FreeStyleBuild jobRun = project.scheduleBuild2(0).get();

        // Verify the job log contains the expected message
        jenkins.assertBuildStatus(Result.ABORTED, jobRun);
        jenkins.assertLogContains("[GUARD] ❌ Pre-check Failed – Change Request Number parameter is empty", project.getLastBuild());
    }

    @Test
    public void shouldFailChecksBecauseOfEmptyChangeRequestNumber() throws Exception {
        // Create a Maven-like project with an empty CHG_NUMBER parameter
        FreeStyleProject project = jenkins.createFreeStyleProject("test-maven-empty-param");

        // Add a shell step to simulate Maven build
        project.getBuildersList().add(new Shell("echo 'Simulating Maven build: mvn clean install'"));

        // Add CHG_NUMBER parameter with empty value
        ParametersDefinitionProperty params = new ParametersDefinitionProperty(
                new StringParameterDefinition("CHG_NUMBER", "", "Change Request Number")
        );
        project.addProperty(params);

        // Run the job
        FreeStyleBuild jobRun = project.scheduleBuild2(0).get();

        // Verify the job log contains the expected message
        jenkins.assertBuildStatus(Result.ABORTED, jobRun);
        jenkins.assertLogContains("[GUARD] ❌ Pre-check Failed – Change Request Number parameter is empty", project.getLastBuild());
    }

    @Test
    public void shouldFailChecksBecauseOfNonPrefixedChangeRequestNumber() throws Exception {
        // Create a Maven-like project with a non-prefixed CHG_NUMBER parameter
        FreeStyleProject project = jenkins.createFreeStyleProject("test-maven-non-prefixed-param");

        // Add a shell step to simulate Maven build
        project.getBuildersList().add(new Shell("echo 'Simulating Maven build: mvn clean install'"));

        // Add CHG_NUMBER parameter with non-prefixed value
        ParametersDefinitionProperty params = new ParametersDefinitionProperty(
                new StringParameterDefinition("CHG_NUMBER", "123456", "Change Request Number")
        );
        project.addProperty(params);

        // Run the job
        FreeStyleBuild jobRun = project.scheduleBuild2(0).get();

        // Verify the job log contains the expected message
        jenkins.assertBuildStatus(Result.ABORTED, jobRun);
        jenkins.assertLogContains("[GUARD] ❌ Pre-check Failed – Change Request Number parameter should start with CHG", project.getLastBuild());
    }
}
