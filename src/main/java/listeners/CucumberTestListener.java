package listeners;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.After;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.*;
import org.apache.commons.io.IOUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class CucumberTestListener implements ConcurrentEventListener {
    private CloseableHttpClient httpClient = HttpClients.createDefault();
    private Map<Long, String> sessionIds = new HashMap<>();
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneOffset.UTC);

    public CucumberTestListener() {}

    @Before
    public void beforeScenario(Scenario scenario) {
       System.out.println(scenario);
        // WebDriver driver = getDriver(); // Ensure this method correctly fetches the current driver instance

        // if (driver instanceof RemoteWebDriver) {
            sessionIds.put(Thread.currentThread().getId(), "no session");
        // }
    }

    @After
    public void afterScenario(Scenario scenario) {
        sessionIds.remove(Thread.currentThread().getId());
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        // Register for all the relevant events with appropriate handlers
        publisher.registerHandlerFor(TestRunStarted.class, this::handleEvent);
        // Continue registration for other events as previously outlined
    }

    private void handleEvent(Event event) {
        JSONObject data = new JSONObject();
        data.put("time", dtf.format(LocalDateTime.now(ZoneOffset.UTC)));
        data.put("event", event.getClass().getSimpleName());
        data.put("threadId", Thread.currentThread().getId());
        data.put("sessionId", sessionIds.getOrDefault(Thread.currentThread().getId(), "no session"));
        data.put("eventData", extractEventData(event));
        sendDataToAPI(data);
    }

    private String extractEventData(Event event) {
        // Convert event details to a string or JSON, depending on the event type
        // This may involve using reflection or specific getters to serialize event data
        if (event instanceof TestCaseFinished) {
            Result result = ((TestCaseFinished) event).getResult();
            return "Status: " + result.getStatus() + ", Duration: " + result.getDuration().toMillis() + " ms";
        }
        // Add more cases for different event types as needed
        return event.toString(); // Default serialization
    }

    private void sendDataToAPI(JSONObject data) {
        try {
            HttpPost request = new HttpPost("https://webhook.site/02c23b22-9618-48ff-a61d-c89ea58ca38c");
            StringEntity params = new StringEntity(data.toString());
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            httpClient.execute(request);
        } catch (Exception e) {
            System.err.println("Failed to send data to API: " + e.getMessage());
        }
    }
}
