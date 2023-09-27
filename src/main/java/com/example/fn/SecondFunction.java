/*
** HelloFunction version 1.0.
**
** Copyright (c) 2020 Oracle, Inc.
** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
*/

package com.example.fn;

import java.util.List;

import com.fnproject.fn.api.InvocationContext;

import com.fnproject.fn.api.RuntimeContext;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributeKey;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Scope;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;

//import io.opentelemetry.instrumentation.annotations.WithSpan;

public class SecondFunction {

    // The getter will be used for incoming requests
    TextMapGetter<InvocationContext> getter = new TextMapGetter<>() {
        @Override
        public String get(InvocationContext carrier, String key) {
            if (carrier.getRequestHeaders().get("Fn-Http-H-Traceparent").isPresent()) {
                return carrier.getRequestHeaders().get("Fn-Http-H-Traceparent").get();
            }
            return null;
        }

        @Override
        public Iterable<String> keys(InvocationContext carrier) {
            return null;
        }
    };

    public String handleRequest(RuntimeContext runtimeContext, InvocationContext invocationContext, String input) {

        Context extractedContext = GlobalOpenTelemetry.getPropagators().getTextMapPropagator()
                .extract(Context.current(), invocationContext, getter);

        try (Scope scope = extractedContext.makeCurrent()) {
            String name = (input == null || input.isEmpty()) ? "world" : input;

            Tracer tracer = GlobalOpenTelemetry.getTracer("manual", "1.0.1");
            Span serverSpan = tracer.spanBuilder("OCI Handle Request")
                    .setSpanKind(SpanKind.SERVER)
                    .startSpan();
            serverSpan.setAttribute(SemanticAttributes.HTTP_METHOD, "GET"); // TODO Add attributes

            try {
                String responseCatFact = MakeRequest();
                serverSpan.addEvent("Finished Outgoing Request",
                        Attributes.of(AttributeKey.stringKey("CatFact"), responseCatFact));
            } catch (Exception e) {
                System.out.println(e);
                serverSpan.setStatus(StatusCode.ERROR, e.getMessage());

            } finally {
                serverSpan.end();
                System.out.println("Value of name is " + name);
                System.out.println("Exiting Java SecondFunction::handleRequest");
                return "SecondFunction: " + name + "!";
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return "SecondFunction: end of the file";
    }

    @WithSpan
    public String MakeRequest() throws Exception {
        // avoid creating several instances, should be singleon
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://catfact.ninja/fact")
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

}
