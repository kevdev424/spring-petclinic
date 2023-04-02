import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.context.Context;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

@WebFilter
@Order(1)
public class TraceIdMDCFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Span span = Span.fromContext(Context.current());
        String traceId = span.getSpanContext().getTraceId();
        if (!TraceId.getInvalid().equals(traceId)) {
            MDC.put("dd.trace_id", traceId);
            MDC.put("dd.span_id", span.getSpanContext().getSpanId());
        }

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove("dd.trace_id");
            MDC.remove("dd.span_id");
        }
    }
}
