package org.springframework.samples.petclinic;

import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

public class OpenTelemetryConfig {

	public static OpenTelemetrySdk setup() {

		OtlpJsonLoggingSpanExporter.create();

		SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
			.addSpanProcessor(SimpleSpanProcessor.create(LoggingSpanExporter.create()))
			.addSpanProcessor(SimpleSpanProcessor.create(OtlpJsonLoggingSpanExporter.create()))
			.build();

		OpenTelemetrySdk openTelemetrySdk =
			OpenTelemetrySdk.builder()
				.setTracerProvider(sdkTracerProvider)
				.build();

		return openTelemetrySdk;
	}
}
