package MyRunner;

import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.*;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.UUID;

public class CustomTestNGCucumberListener implements ConcurrentEventListener, ITestListener {

    private  final String SESSION_ID = UUID.randomUUID().toString();
    private  final String GIT_METADATA = fetchGitMetadata();
    private  final String BUILD_NAME = getBuildName();
    private static final String REPORT_PATH = "target/cucumber-reports/cucumber.json";

    @Override
    public void onStart(ITestContext context) {
        sendToServer("Test run started with build: " + BUILD_NAME, "INFO");
    }

    @Override
    public void onFinish(ITestContext context) {
        sendToServer("Suite finished. Uploading Cucumber JSON report.", "INFO");
        uploadCucumberReport();
    }

    @Override
    public void onTestStart(ITestResult result) {
        String scenarioFile = "src/test/resources/" + result.getTestClass().getName() + ".feature";
        sendToServer("Test started: " + result.getName() + " | Source: " + scenarioFile, "INFO");
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        sendToServer("Test passed: " + result.getName(), "SUCCESS");
        uploadExecutionLogs(result);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        sendToServer("Test failed: " + result.getName() + "\nError: " + result.getThrowable(), "ERROR");
        uploadExecutionLogs(result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        sendToServer("Test skipped: " + result.getName(), "WARNING");
        uploadExecutionLogs(result);
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestCaseStarted.class, event -> sendToServer("Scenario Started: " + event.getTestCase().getName(), "INFO"));
        publisher.registerHandlerFor(TestCaseFinished.class, event -> sendToServer("Scenario Finished: " + event.getTestCase().getName() + " | Status: " + event.getResult().getStatus(), "INFO"));
    }

    private void sendToServer(String message, String status) {
        try {
            long threadId = Thread.currentThread().getId();
            String timestamp = Instant.now().toString();

            String jsonInputString = String.format(
                "{ \"session_id\": \"%s\", \"thread_id\": \"%d\", \"timestamp\": \"%s\", \"status\": \"%s\", \"message\": \"%s\", \"git_meta\": %s }",
                SESSION_ID, threadId, timestamp, status, message, GIT_METADATA
            );

            System.out.println("[Log]: " + jsonInputString);

            // URL url = new URL("https://your-server-api.com/logs");
            // HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // connection.setRequestMethod("POST");
            // connection.setRequestProperty("Content-Type", "application/json");
            // connection.setDoOutput(true);

            // connection.getOutputStream().write(jsonInputString.getBytes("utf-8"));
            // connection.getOutputStream().flush();
            // connection.getOutputStream().close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private  void uploadExecutionLogs(ITestResult result) {
        // Implement logic to upload logs related to the test execution
    }

    private  void uploadCucumberReport() {
        try {
            File reportFile = new File(REPORT_PATH);
            if (reportFile.exists()) {
                sendToServer("Uploading Cucumber JSON report: " + REPORT_PATH, "INFO");
                // Implement report upload logic here
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private  String getBuildName() {
        String buildName = System.getProperty("LAMBDATEST_BUILD_NAME");
        if (buildName == null) buildName = System.getenv("LAMBDATEST_BUILD_NAME");
        if (buildName == null) buildName = fetchGitMetadata().split("repo\": ")[1].split(",")[0];
        return buildName;
    }

    private static String fetchGitMetadata() {
        try {
            String repoName = executeCommand("git config --get remote.origin.url");
            String commitId = executeCommand("git rev-parse HEAD");
            String branch = executeCommand("git rev-parse --abbrev-ref HEAD");
            String user = executeCommand("git config user.name");
            String tag = executeCommand("git describe --tags");
            String prDetails = executeCommand("git log --merges -n 1 --pretty=format:'%s'");

            return String.format(
                "{ \"repo\": \"%s\", \"commit\": \"%s\", \"branch\": \"%s\", \"user\": \"%s\", \"tag\": \"%s\", \"pr\": \"%s\" }",
                repoName, commitId, branch, user, tag, prDetails
            );
        } catch (Exception e) {
            return "{}";
        }
    }

    private static String executeCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return reader.readLine();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
