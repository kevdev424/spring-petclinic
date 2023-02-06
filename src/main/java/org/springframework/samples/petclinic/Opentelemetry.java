package org.springframework.samples.petclinic;

import com.sun.net.httpserver.HttpExchange;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapSetter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

public class Opentelemetry {
	private static void initOpenTelemetry() {
		Resource serviceName = Optional.ofNullable(System.getenv("spring-petclinic"))
			.map(n -> Attributes.of(AttributeKey.stringKey("service.name"), n))
			.map(Resource::create)
			.orElseGet(Resource::empty);

		Resource envResourceAttributes = Resource.create(Stream.of(Optional.ofNullable(System.getenv("service.name=spring-petclinic,service.version=1.0.1")).orElse("").split(","))
			.filter(pair -> pair != null && pair.length() > 0 && pair.contains("="))
			.map(pair -> pair.split("="))
			.filter(pair -> pair.length == 2)
			.collect(Attributes::builder, (b, p) -> b.put(p[0], p[1]), (b1, b2) -> b1.putAll(b2.build()))
			.build()
		);

		Resource dtMetadata = Resource.empty();
		for (String name : new String[] {"dt_metadata_e617c525669e072eebe3d0f08212e8f2.properties", "/var/lib/datadog/enrichment/dt_metadata.properties"}) {
			try {
				Properties props = new Properties();
				props.load(name.startsWith("/var") ? new FileInputStream(name) : new FileInputStream(Files.readAllLines(Paths.get(name)).get(0)));
				dtMetadata = dtMetadata.merge(Resource.create(props.entrySet().stream()
					.collect(Attributes::builder, (b, e) -> b.put(e.getKey().toString(), e.getValue().toString()), (b1, b2) -> b1.putAll(b2.build()))
					.build())
				);
			} catch (IOException e) {}
		}

		SpanExporter exporter = OtlpHttpSpanExporter.builder()
			.setEndpoint("https://${{DATADOG_URL.secrets}}") //TODO Replace <URL> to your SaaS/Managed-URL as mentioned in the next step
			.addHeader("Authorization", "Api-Token ${{DATADOG_API.secrets}}") //TODO Replace <TOKEN> with your API Token as mentioned in the next step
			.build();

		SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
			.setResource(Resource.getDefault().merge(envResourceAttributes).merge(serviceName).merge(dtMetadata))
			.setSampler(Sampler.alwaysOn())
			.addSpanProcessor(BatchSpanProcessor.builder(exporter).build())
			.build();

		OpenTelemetrySdk.builder()
			.setTracerProvider(sdkTracerProvider)
			.setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
			.buildAndRegisterGlobal();
		Runtime.getRuntime().addShutdownHook(new Thread(sdkTracerProvider::close));
	}

	public void init() {
		initOpenTelemetry();
	}

	Tracer tracer = GlobalOpenTelemetry
		.getTracerProvider()
		.tracerBuilder("petclinic") //TODO Replace with the name of your tracer
		.build();

	public void handle(HttpExchange httpExchange) {
		//Extract the SpanContext and other elements from the request
		Context extractedContext = GlobalOpenTelemetry.getPropagators().getTextMapPropagator()
			.extract(Context.current(), httpExchange, getter);
		try (Scope scope = extractedContext.makeCurrent()) {
			//This will automatically propagate context by creating child spans within the extracted context
			Span serverSpan = tracer.spanBuilder("petclinic-span") //TODO Replace with the name of your span
				.setSpanKind(SpanKind.SERVER) //TODO Set the kind of your span
				.startSpan();
			serverSpan.setAttribute(SemanticAttributes.HTTP_METHOD, "GET"); //TODO Add attributes
			serverSpan.end();
		}
	}

	//The getter will be used for incoming requests
	TextMapGetter<HttpExchange> getter =
		new TextMapGetter<>() {
			@Override
			public String get(HttpExchange carrier, String key) {
				if (carrier.getRequestHeaders().containsKey(key)) {
					return carrier.getRequestHeaders().get(key).get(0);
				}
				return null;
			}

			@Override
			public Iterable<String> keys(HttpExchange carrier) {
				return carrier.getRequestHeaders().keySet();
			}
		};

	//The setter will be used for outgoing requests
	TextMapSetter<HttpURLConnection> setter =
		(carrier, key, value) -> {
			assert carrier != null;
			// Insert the context as Header
			carrier.setRequestProperty(key, value);
		};

	public void makeOutgoingRequest() {
		URL url = null; //TODO Replace with the URL of the service to be called
		try {
			url = new URL("https://localhost:8080/");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		Span outGoing = tracer.spanBuilder("petclinic-client-span") //TODO Replace with the name of your span
			.setSpanKind(SpanKind.CLIENT) //TODO Set the kind of your span
			.startSpan();
		try (Scope scope = outGoing.makeCurrent()) {
			outGoing.setAttribute(SemanticAttributes.HTTP_METHOD, "GET"); //TODO Add attributes
			HttpURLConnection transportLayer = null;
			try {
				transportLayer = (HttpURLConnection) url.openConnection();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			// Inject the request with the *current*  Context, which contains our current span
			GlobalOpenTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), transportLayer, setter);
			// Make outgoing call
		} finally {
			outGoing.end();
		}
	}
}
