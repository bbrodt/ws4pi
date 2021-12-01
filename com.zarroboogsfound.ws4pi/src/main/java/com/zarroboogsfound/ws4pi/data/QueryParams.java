package com.zarroboogsfound.ws4pi.data;

import java.util.Deque;
import java.util.Optional;

import io.undertow.server.HttpServerExchange;

public class QueryParams {

	public static Optional<String> get(HttpServerExchange exchange, String name) {
        return Optional.ofNullable(exchange.getQueryParameters().get(name))
                       .map(Deque::getFirst);
    }

	public static Optional<Float> getFloat(HttpServerExchange exchange, String name) {
        return get(exchange, name).map(Float::parseFloat);
    }

	public static Optional<Long> getLong(HttpServerExchange exchange, String name) {
        return get(exchange, name).map(Long::parseLong);
    }

	public static Optional<Integer> getInt(HttpServerExchange exchange, String name) {
        return get(exchange, name).map(Integer::parseInt);
    }

	public static Optional<Boolean> getBool(HttpServerExchange exchange, String name) {
        return get(exchange, name).map(Boolean::parseBoolean);
    }
}