package com.zarroboogsfound.ws4pi.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.zarroboogsfound.ws4pi.logging.Log;

import io.undertow.server.HttpServerExchange;

/**
 * @author rbrodt
 *
 * Wrapper class for Agent and Appliance HTTP Server Error Objects.
 * The HTTP Server constructs an ErrorReply with either an error message
 * string and error level, or an <code>Exception</code> object. If the error
 * is an Exception, a stack trace is also included in the JSON returned to
 * the HTTP client.
 * 
 * The base class send() method must be called to return the error information
 * as serialized JSON to the HTTP client.
 * 
 * See <code>com.datarunner.common.data.Reply</code> for more information.
 */
public class ErrorReply extends Reply {

	public int level = -1;
	public String message = null;
	public List<String> stacktrace = new ArrayList<String>();
	
	public ErrorReply() {
		this.status = "ERROR";
	}
	
	public ErrorReply(String message) {
		this();
		this.message = message;
	}

	public ErrorReply(int level, String message) {
		this(message);
		this.level = level;
	}
	
	public ErrorReply(Exception e) {
		this(-1, e.getMessage());
		if (e.getMessage()==null)
			this.message = e.getClass().getSimpleName();
		for (StackTraceElement st : e.getStackTrace()) {
			stacktrace.add(st.toString());
		}
	}
	
	public void send(HttpServerExchange exchange) {
		String json = toJson(this);
		Log.info(this.getClass().getSimpleName()+" HTTP Error Reply:\n"+json);
		ContentTypeSenders.sendJson(exchange, json);
	}

	@Override
	public void send(Socket socket) {
		try {
			OutputStream output = socket.getOutputStream();
			String json = toJson(this);
			Log.info(this.getClass().getSimpleName()+" TCP Error Reply:\n"+json);
			output.write(json.getBytes("UTF-16"));
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
