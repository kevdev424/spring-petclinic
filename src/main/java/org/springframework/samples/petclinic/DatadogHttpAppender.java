import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.encoder.Encoder;
import okhttp3.*;

import java.io.IOException;

public class DatadogHttpAppender extends AppenderBase<ILoggingEvent> {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final OkHttpClient CLIENT = new OkHttpClient();
    private static final String DATADOG_LOGS_API = "https://http-intake.logs.datadoghq.com/v1/input/";

    private Encoder<ILoggingEvent> encoder;
    private String apiKey;

    public void setEncoder(Encoder<ILoggingEvent> encoder) {
        this.encoder = encoder;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (encoder == null || apiKey == null || apiKey.isEmpty()) {
            addError("DatadogHttpAppender must have a valid apiKey and encoder.");
            return;
        }

        byte[] logBytes = encoder.encode(event);
        String logJson = new String(logBytes);

        Request request = new Request.Builder()
                .url(DATADOG_LOGS_API + apiKey)
                .post(RequestBody.create(JSON, logJson))
                .build();

        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                addError("Failed to send log to Datadog: " + response.message());
            }
        } catch (IOException e) {
            addError("Error sending log to Datadog", e);
        }
    }
}
