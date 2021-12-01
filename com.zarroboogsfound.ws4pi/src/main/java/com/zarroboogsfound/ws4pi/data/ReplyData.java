package com.zarroboogsfound.ws4pi.data;

/**
 * @author rbrodt
 * 
 * Base class for UDP Broadcast Reply Data from Agents and Appliances.
 * 
 * The reply object is wrapped in a <code>com.datarunner.common.undertow.DataReply</code> object
 * and sent as serialized JSON to the client in response to the clients' UDP broadcast messages.
 * See <code>com.datarunner.common.discovery.server.DiscoveryServer.setReplyData(Object)</code>
 */
public class ReplyData {
	// The human-readable Agent or Appliance name
	public String name;
	// A GUID that uniquely identifies the Agent or Appliance.
	// The GUID can be used by clients to identify a specific server instance.
	// The implication is that we can host multiple Agent or
	// Appliance instances on the same server.
	public String guid;
}
