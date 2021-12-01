package com.zarroboogsfound.ws4pi.data;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import io.undertow.server.HttpServerExchange;

/**
 * @author rbrodt
 *
 * Wrapper class for Agent and Appliance HTTP Server reply Objects.
 * The HTTP Server constructs a DataReply with the Object that represents
 * the data the client has requested, and then calls the base class send()
 * method to return the data as serialized JSON to the HTTP client.
 * 
 * See <code>com.datarunner.common.data.Reply</code> for more information.
 */
public class ReplyWrapper extends Reply {

	public String type;
	public Object object;
	
	public ReplyWrapper(Object data) {
		this.type = data.getClass().getName();
		this.object = data;
	}

	public void send(HttpServerExchange exchange) {
		String json = toJson(this);
		System.out.println("HTTP Object Reply:\n"+json);
		ContentTypeSenders.sendJson(exchange, json);
	}

	@Override
	public void send(Socket socket) {
		try {
			OutputStream output = socket.getOutputStream();
			String json = toJson(this);
			System.out.println("TCP Object Reply:\n"+json);
			output.write(json.getBytes("UTF-16"));
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
