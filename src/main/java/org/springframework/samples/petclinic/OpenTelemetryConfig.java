package org.springframework.samples.petclinic;

import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

import java.util.jar.Attributes;

public class OpenTelemetryConfig {

	public static OpenTelemetrySdk setup() {



		OtlpGrpcSpanExporter otlpExporter = OtlpGrpcSpanExporter.builder()
			.setEndpoint("http://datadoghq.com")
			.addHeader("DD-APPLICATION-KEY", "${{ secrets.DATADOG_APP_KEY  }}")
			.addHeader("DD-API-KEY","${{ secrets.DATADOG_API_KEY }}")
			.setCompression("gzip")
			.build();

		SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
			.addSpanProcessor(BatchSpanProcessor.builder(otlpExporter).build())
			.build();

		OpenTelemetrySdk openTelemetrySdk =
			OpenTelemetrySdk.builder()
				.setTracerProvider(sdkTracerProvider)
				// install the W3C Trace Context propagator
				.setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
				.build();

		// it's always a good idea to shutdown the SDK when your process exits.
		Runtime.getRuntime()
			.addShutdownHook(
				new Thread(
					() -> {
						System.err.println(
							"*** forcing the Span Exporter to shutdown and process the remaining spans");
						sdkTracerProvider.shutdown();
						System.err.println("*** Trace Exporter shut down");
					}));

		return openTelemetrySdk;
	}
}
