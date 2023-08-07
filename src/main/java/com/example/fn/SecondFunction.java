/*
** HelloFunction version 1.0.
**
** Copyright (c) 2020 Oracle, Inc.
** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
*/

package com.example.fn;

import com.fnproject.fn.api.InvocationContext;
// import io.opentelemetry.context.Context;
import com.fnproject.fn.api.RuntimeContext;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.opentelemetry.sdk.OpenTelemetrySdk;

//import io.opentelemetry.instrumentation.annotations.WithSpan;

public class SecondFunction {

    // OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
    // .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
    // .buildAndRegisterGlobal();

    // Tracer tracer = openTelemetry.getTracer("dynatrace-manual-opentel", "1.0.0");

    // TextMapGetter<JSONObject> getter = new TextMapGetter<>() {
    // @Override
    // public String get(JSONObject carrier, String key) {
    // System.out.println("TextMapGetter - " + key);

    // JSONObject carrier_data = (JSONObject) carrier.get("carrier");
    // String value = carrier_data.get("Value").toString();
    // JSONObject propagationValue = new JSONObject(value);
    // String traceparent =
    // propagationValue.get("Fn-Http-H-Traceparent").toString();

    // if (traceparent != null) {
    // return traceparent;
    // }
    // return null;
    // }

    // @Override
    // public Iterable<String> keys(JSONObject carrier) {
    // return null;
    // }

    // };

    @WithSpan
    public String handleRequest(RuntimeContext runtimeContext, InvocationContext invocationContext, String input) {

        System.out.println("Debugando context");
        System.out.println("all headers: " + invocationContext.getRequestHeaders());
        System.out.println("old header: " + invocationContext.getRequestHeaders().get("Fn-Http-H-Traceparent"));
        System.out.println("new header: " + invocationContext.getRequestHeaders().get("traceparent"));

        // try {
        // TextMapPropagator propagator =
        // GlobalOpenTelemetry.getPropagators().getTextMapPropagator();
        // Context parentContext = propagator.extract(Context.current(),
        // invocationContext, getter);
        // Span span = tracer.spanBuilder("process message")
        // .setParent(parentContext)
        // .setSpanKind(SpanKind.SERVER)
        // .startSpan();

        // System.out.println("parentContext: " + parentContext);

        // try {
        // // Add the attributes defined in the Semantic Conventions
        // span.setAttribute("key", "value");

        // } finally {
        // span.end();
        // }

        System.out.println("Entering Java SecondFunction::handleRequest");
        System.out.println("Value of input is " + input);
        String name = (input == null || input.isEmpty()) ? "world" : input;

        System.out.println("Value of name is " + name);
        System.out.println("Exiting Java SecondFunction::handleRequest");

        try {
            MakeRequest();
        } catch (Exception e) {
            System.out.println(e);
        }

        return "SecondFunction: " + name + "!";
    }

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
