package com.zarroboogsfound.ws4pi;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.zarroboogsfound.ws4pi.data.ErrorReply;
import com.zarroboogsfound.ws4pi.data.QueryParams;
import com.zarroboogsfound.ws4pi.data.ReplyWrapper;
import com.zarroboogsfound.ws4pi.devices.DeviceController;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;

public class HttpRequestHandler {

	private WS4PiConfig config;

	public HttpRequestHandler(WS4PiConfig config) {
		this.config = config;
	}

	public void handleRequest(final HttpServerExchange exchange) {
		String qp = exchange.getQueryString();
		//if (exchange.getQueryString()!=null && !exchange.getQueryString().isEmpty())
		System.out.println("HTTP Request from :"+exchange.getConnection().getPeerAddress().toString()+
		"\n"+exchange.getRequestURL()+(qp==null||qp.isEmpty() ? "" : ("?"+qp)));

		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
		HttpString method = exchange.getRequestMethod();
		try {
			if (method == Methods.GET) {
				handleGetRequest(exchange);
			}
			else if (method == Methods.POST) {
				handlePostRequest(exchange);
			}
		}
		catch (Exception e) {
			new ErrorReply(e).send(exchange);
		}
	}

	// HTTP GET and POST requests contain the device name and operation parameters.
	// The device name is parsed from the request path - this is the first "word"
	// in the path delimited with "/" and either the end of the path string, or
	// the "?" query parameter delimiter character. For example the GET request:
	// 
	//     http://localhost:8081/servo?channel=0
	//
	// will return the position and other information for servo channel 0.
	// For POST methods, this sample request:
	//
	//     http://localhost:8081/servo?channel=0&position=50
	//
	// will move servo channel 0 to position 50.

	public void handleGetRequest(HttpServerExchange exchange) {
		DeviceController dc = getDeviceController(exchange);
		if (dc!=null) {
			try {
				new ReplyWrapper(dc.handleGetOperation(exchange)).send(exchange);
			}
			catch (Exception e) {
				new ErrorReply(e).send(exchange);
			}
		}
	}

	public void handlePostRequest(final HttpServerExchange exchange) {
		DeviceController dc = getDeviceController(exchange);
		if (dc!=null)
		{
			try {
				new ReplyWrapper(dc.handleSetOperation(exchange)).send(exchange);
			}
			catch (Exception e) {
				new ErrorReply(e).send(exchange);
			}
		}
	}
	
	private DeviceController getDeviceController(HttpServerExchange exchange) {
		String device = null;
		String req = exchange.getRequestPath();
		if (!req.startsWith("/")) {
			new ErrorReply("MISSING DEVICE NAME").send(exchange);
			return null;
		}
		
		// the request string contains the device type as a string;
		// convert this to a DeviceType enum
		if (req.contains("?"))
			device = req.substring(1,req.indexOf('?'));
		else
			device = req.substring(1);
		DeviceType type = DeviceType.valueOf(device.toUpperCase());

		DeviceController dc = config.getDeviceProvider().getDeviceController(type);
		if (dc==null) {
			new ErrorReply("UNKNOWN DEVICE NAME "+device).send(exchange);
			return null;
		}
		return dc;
	}
}
