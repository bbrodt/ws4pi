package com.zarroboogsfound.ws4pi.data;

import java.nio.ByteBuffer;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

public class ContentTypeSenders {

    public static  void sendJson(HttpServerExchange exchange, String json) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send(json);
    }

    public static  void sendJson(HttpServerExchange exchange, byte[] bytes) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send(ByteBuffer.wrap(bytes));
    }

    public static  void sendXml(HttpServerExchange exchange, String xml) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/xml");
        exchange.getResponseSender().send(xml);
    }

    public static  void sendHtml(HttpServerExchange exchange, String html) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");
        exchange.getResponseSender().send(html);
    }

    public static  void sendText(HttpServerExchange exchange, String text) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send(text);
    }

    public static  void sendBytes(HttpServerExchange exchange, byte[] bytes) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/octet-stream");
        exchange.getResponseSender().send(ByteBuffer.wrap(bytes));
    }

    public static  void sendFile(HttpServerExchange exchange, String fileName, String content) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/octet-stream");
        exchange.getResponseHeaders().put(Headers.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"");
        exchange.getResponseSender().send(content);
    }

    public static  void sendFile(HttpServerExchange exchange, String fileName, byte[] bytes) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/octet-stream");
        exchange.getResponseHeaders().put(Headers.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"");
        exchange.getResponseSender().send(ByteBuffer.wrap(bytes));
    }
    
    public static void setTextResponseHeader(HttpServerExchange exchange) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
    }
    
    public static void setBinaryResponseHeader(HttpServerExchange exchange) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/octet-stream");
    }
}