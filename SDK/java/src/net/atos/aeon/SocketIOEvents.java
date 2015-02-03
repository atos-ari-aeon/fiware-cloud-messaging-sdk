/**
  Copyright (C) 2014 ATOS
 
    This file is part of AEON.

    AEON is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    AEON is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with AEON.  If not, see <http://www.gnu.org/licenses/>.
 
  Authors:
  Jose Gato Luis <jose.gato@atos.net>
  
  */
 
package net.atos.aeon;


import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIOException;

import org.json.JSONException;
import org.json.JSONObject;



public class SocketIOEvents implements IOCallback, IOAcknowledge {

	private String queue = null;

	private AEONInterface eventCallback = null;
	private AEONSDKMessages aeonMessages = null;

	public SocketIOEvents(AEONInterface eventCallback) {
		this.eventCallback = eventCallback;
		this.aeonMessages = new AEONSDKMessages();

	}


	
	public String getQueue() {
		return queue;
	}

	public void setQueue(String queue) {
		this.queue = queue;
	}

	
	@Override
	public void ack(Object... data) {
		System.out.println("Socket ACK nos supported");
	}

	@Override
	public void on(String event, IOAcknowledge ack, Object... data) {
		if (event.equals("message-" + this.queue)){
			eventCallback.deliveredMessage((JSONObject) data[0]);
		}else if (event.equals("control")){
			eventCallback.control( aeonMessages.controlTranslator( (JSONObject) data[0]));
		}
		else {
			System.out.println("Event ignored " + event);
		}
	}



	@Override
	public void onMessage(String message, IOAcknowledge ack) {
		System.out.println("Server said: " + message + " but we dont care");
	}

	@Override
	public void onMessage(JSONObject json, IOAcknowledge ack) {
		try {
			System.out.println("Server said:" + json.toString(2) + " but we dont care");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onConnect() {

	}

	@Override
	public void onDisconnect() {
		System.out.println("Connection terminated.");
		this.eventCallback.control(aeonMessages.INFRASTRUCTURE_DOWN);
	}

	@Override
	public void onError(SocketIOException socketIOException) {
		System.out.println("an Error occured");
		socketIOException.printStackTrace();
	}



}