package org.springframework.samples.petclinic;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;

public class DatadogHttpLogAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
	private String apiKey;
	private StatsDClient statsDClient;

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	@Override
	protected void append(ILoggingEvent event) {
		if (statsDClient == null) {
			statsDClient = new NonBlockingStatsDClient("log", "localhost", 8125);
		}
		String message = event.getFormattedMessage();
		statsDClient.recordEvent("log.message", message, "dd.api_key:" + apiKey);
	}
}
