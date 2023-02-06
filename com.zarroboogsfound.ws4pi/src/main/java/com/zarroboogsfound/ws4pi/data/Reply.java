package com.zarroboogsfound.ws4pi.data;

import java.lang.reflect.Modifier;
import java.net.Socket;
import java.util.HashMap;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import io.undertow.server.HttpServerExchange;

/**
 * @author rbrodt
 *
 * Base class for Agent and Appliance HTTP Server reply Objects. All responses are
 * serialized into JSON and sent as plain text. This class has static <code>send()</code>
 * and <code>receive()</code> methods that assist in serializing/deserializing the
 * Reply object.
 */
public class Reply {
	public String status = "OK";

	// NOTE: this class and subclasses of it are serialized as JSON. This base class
	// should be abstract, but you can't serialize an abstract class, so these two
	// send() methods must be overridden by subclasses.
	public void send(HttpServerExchange exchange) {
		throw new UnsupportedOperationException(this.getClass().getSimpleName()+".send(HttpServerExchange) is not implemented");
	}
	
	public void send(Socket socket) {
		throw new UnsupportedOperationException(this.getClass().getSimpleName()+".send(Socket) is not implemented");
	}
	
	protected static String toJson(Reply reply) {
		Gson gson = new GsonBuilder()
				.excludeFieldsWithModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.ABSTRACT)
				.setPrettyPrinting()
				.create();
		return gson.toJson(reply);
	}

	public String toJson() {
		return toJson(this);
	}

	public static <T> T receive(String json, Class<T> classOfT) {
		Object o = receive(json);
		return (T) o;
	}
	
	public static Object receive(String json) {
		return fromJson(json);
	}
	
	protected static Object fromJson(String json) {
		Gson gson = new GsonBuilder()
				.create();
		Reply r = gson.fromJson(json, Reply.class);
		if (r.status.equals("OK")) {
			ReplyWrapper dr = gson.fromJson(json, ReplyWrapper.class);
			try {
				Class clazz = Class.forName(dr.type);
				JsonElement je = gson.toJsonTree(dr.object);
				return gson.fromJson(je, clazz);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			return dr;
		}
		if (r.status.equals("ERROR")) {
			ErrorReply er = gson.fromJson(json, ErrorReply.class);
			return er;
		}
		return r;
	}
}
