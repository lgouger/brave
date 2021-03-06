package com.github.kristofa.brave;

import java.util.Random;

/**
 * Public Brave api. Makes sure all returned instances share the same trace/span state.
 * <p>
 * This api should be used to create new instances for usage in your applications.
 * 
 * @author kristof
 */
public class Brave {

    private final static ServerAndClientSpanState SERVER_AND_CLIENT_SPAN_STATE = new ServerAndClientSpanStateImpl();
    private final static Random RANDOM_GENERATOR = new Random();

    private Brave() {
        // Only static access.
    }

    /**
     * Gets {@link EndPointSubmitter}. Allows you to set endpoint (ip, port, service name) for this service.
     * <p/>
     * Each annotation that is being submitted (including cs, cr, sr, ss) has an endpoint (host, port, service name)
     * assigned. For a given service/application instance the endpoint only needs to be set once and will be reused for all
     * submitted annotations.
     * <p/>
     * The EndPoint needs to be set using the EndPointSubmitter before any annotation/span is created.
     * 
     * @return {@link EndPointSubmitter}.
     */
    public static EndPointSubmitter getEndPointSubmitter() {
        return new EndPointSubmitterImpl(SERVER_AND_CLIENT_SPAN_STATE);
    }

    /**
     * Gets a simple {@link SpanCollector} which logs spans through slf4j at info level.
     * <p/>
     * Can be used for testing or debugging.
     * 
     * @return A simple {@link SpanCollector} which logs spans through slf4j at info level.
     * @see Brave#getClientTracer(SpanCollector)
     * @see Brave#getServerTracer(SpanCollector)
     */
    public static SpanCollector getLoggingSpanCollector() {
        return new LoggingSpanCollectorImpl();
    }

    /**
     * Gets a {@link TraceFilter} that does not filter. So it will trace every request.
     * <p/>
     * Can be used if tracing every request will not cause too much overhead or for debugging or during development.
     * 
     * @return TraceFilter that does not filtering at all.
     * @see Brave#getClientTracer(SpanCollector, TraceFilter)
     */
    public static TraceFilter getTraceAllTraceFilter() {
        return new TraceAllTraceFilter();
    }

    /**
     * Gets a {@link ClientTracer} that will be initialized with a custom {@link SpanCollector} and a custom
     * {@link TraceFilter}.
     * <p/>
     * The ClientTracer is used to initiate a new span when doing a request to another service. It will generate the cs
     * (client send) and cr (client received) annotations. When the cr annotation is set the span will be submitted to
     * SpanCollector if not filtered by TraceFilter.
     * 
     * @param collector Custom {@link SpanCollector}. Should not be <code>null</code>.
     * @param traceFilter Custom trace filter. Should not be <code>null</code>.
     * @return {@link ClientTracer} instance.
     * @see Brave#getLoggingSpanCollector()
     * @see Brave#getTraceAllTraceFilter()
     */
    public static ClientTracer getClientTracer(final SpanCollector collector, final TraceFilter traceFilter) {
        return new ClientTracerImpl(SERVER_AND_CLIENT_SPAN_STATE, RANDOM_GENERATOR, collector, traceFilter);
    }

    /**
     * Gets a {@link ServerTracer}.
     * <p/>
     * The ServerTracer is used to generate sr (server received) and ss (server send) annotations. When ss annotation is set
     * the span will be submitted to SpanCollector if our span needs to get traced (as decided by ClientTracer).
     * 
     * @param collector Custom {@link SpanCollector}. Should not be <code>null</code>.
     * @return {@link ServerTracer} instance.
     */
    public static ServerTracer getServerTracer(final SpanCollector collector) {
        return new ServerTracerImpl(SERVER_AND_CLIENT_SPAN_STATE, collector);
    }

    /**
     * Can be used to submit application specific annotations to the current server span.
     * 
     * @return Server span {@link AnnotationSubmitter}.
     */
    public static AnnotationSubmitter getServerSpanAnnotationSubmitter() {
        return new ServerTracerImpl(SERVER_AND_CLIENT_SPAN_STATE, new SpanCollector() {

            @Override
            public void collect(final Span span) {
                // Nothing.

            }
        });
    }

    /**
     * Only relevant if you start multiple threads in your server side code and you will use {@link ClientTracer},
     * {@link AnnotationSubmitter} from those threads.
     * 
     * @see ServerSpanThreadBinder
     * @return {@link ServerSpanThreadBinder}.
     */
    public static ServerSpanThreadBinder getServerSpanThreadBinder() {
        return new ServerSpanThreadBinderImpl(SERVER_AND_CLIENT_SPAN_STATE);
    }
}
