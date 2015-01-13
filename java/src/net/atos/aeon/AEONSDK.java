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

import io.socket.SocketIO;

import java.net.MalformedURLException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import net.atos.aeon.ClientHelper.RelaxedHostNameVerifier;

import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

//import test.Main2.EventsCallbackImp;

public class AEONSDK {

	/*
	 * This client uses the next Socket.io-client implementation:
	 * 
	 * https://github.com/Gottox/socket.io-java-client
	 * 
	 * There are several Java implementations. But they seems no very robust. So
	 * we have to be aware to the possibility of using others.
	 */

	String socketServer = null;
	SocketIO socket = null;
	String subscribeUrl = "";
	String publishUrl = "";
	String id = "";
	String desc = "";
	JSONObject subscription = null;

	AEONInterface eventCallback = null;
	SocketIOEvents aeonCallback = null;

	Logger sioLogger = java.util.logging.Logger.getLogger("io.socket");

	String mode = "";

	AEONSDKMessages messages = null;

	Client webClient = null;

	public static class DummyCallbacks implements AEONInterface {

		@Override
		public void deliveredMessage(JSONObject data) {

		}

		@Override
		public void control(JSONObject data) {

		}

	}

	private String getServerEndpoint(String url) {
		String[] parts = url.split("/");
		return parts[0] + "//" + parts[2];
	}

	private String getSocketServerEndpoint(String url) {

		WebResource webResource = this.webClient.resource(url
				+ "/subscribe/config");

		ClientResponse response = webResource.accept("application/json").get(
				ClientResponse.class);
		String socketServer = "";

		try {

			if (response.getStatus() != 200) {
				eventCallback.control(messages.URL_ERROR);
				subscription = null;
			} else {
				socketServer = new JSONObject(response.getEntity(String.class))
						.getJSONArray("result").getJSONObject(0)
						.getString("socket_server");
			}

		} catch (ClientHandlerException e1) {
			e1.printStackTrace();
			eventCallback.control(messages.UNKNWON_ERROR);
		} catch (UniformInterfaceException e1) {
			eventCallback.control(messages.URL_ERROR);
		} catch (JSONException e1) {
			e1.printStackTrace();
			eventCallback.control(messages.UNKNWON_ERROR);
		} catch (Exception e) {
			eventCallback.control(messages.UNKNWON_ERROR);
		}

		return socketServer;
	}
	
	private void Init(String subscribeUrl, String id, String desc) {
	
		this.messages = new AEONSDKMessages();

		try {
			if (subscribeUrl.indexOf("/subscribe") != -1) {
				if (subscribeUrl.startsWith("https")) {

					this.webClient = ClientHelper.createClient();

					SSLContext sc = SSLContext.getInstance("TLS");
					sc.init(null, ClientHelper.trustAllCerts,
							new SecureRandom());
					SocketIO.setDefaultSSLSocketFactory(sc);
					HttpsURLConnection
							.setDefaultHostnameVerifier(new RelaxedHostNameVerifier());

				} else
					this.webClient = Client.create();

				this.subscribeUrl = subscribeUrl;
				this.socketServer = getSocketServerEndpoint(getServerEndpoint(subscribeUrl));

				this.socket = new SocketIO(socketServer);
				this.socket.addHeader("force_new_connection", "true");
				// this.socket.addHeader("transports", "xhr-polling");
				// this.socket.addHeader("polling duration", "20");

				this.id = id;
				this.desc = desc;
				this.mode = "subscribe";
				sioLogger.setLevel(Level.OFF);
			} else
				this.mode = "error";
		} catch (MalformedURLException e) {
			e.printStackTrace();
			this.mode = "error";
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.mode = "error";
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.mode = "error";
		}
	
	}

	/*
	 * Constructor to configure subscribe mode
	 */

	public AEONSDK(String subscribeUrl, String id, String desc) {
		this.Init(subscribeUrl, id, desc);
	}

	public AEONSDK(String subscribeUrl, JSONObject subscriptionData) {
		
		try {
			this.Init(subscribeUrl, subscriptionData.get("id").toString(), subscriptionData.get("desc").toString());
			this.subscription = subscriptionData;
		} catch (JSONException e) {
			this.mode = "error";
			e.printStackTrace();
		}
		
		
	}

	/*
	 * Constructor to configure publishing mode
	 */
	public AEONSDK(String publishUrl) {
		this.messages = new AEONSDKMessages();

		if (publishUrl.indexOf("/publish") != -1) {

			if (publishUrl.startsWith("https"))

				this.webClient = ClientHelper.createClient();

			else
				this.webClient = Client.create();

			this.publishUrl = publishUrl;

			this.mode = "publish";
			sioLogger.setLevel(Level.OFF);

		} else
			this.mode = "error";
	}

	private void emitSubscriptionRequest() {
		socket.emit("subscribeQueue", this.subscription);
		try {
			aeonCallback.setQueue(this.subscription.getString("subkey"));
		} catch (JSONException e) {

			e.printStackTrace();
		}
	}

	public void pauseSusbscription() {

		if (this.mode == "error") {
			eventCallback.control(messages.URL_ERROR);
			return;
		}

		if (this.mode == "publish") {
			eventCallback.control(messages.SDK_SUB_MODE);
			return;
		}

		socket.emit("unSubscribeQueue", this.subscription);
	}

	public void continueSubscription() {

		if (this.mode == "error") {
			eventCallback.control(messages.URL_ERROR);
			return;
		}

		if (this.mode == "publish") {
			eventCallback.control(messages.SDK_SUB_MODE);
			return;
		}

		emitSubscriptionRequest();

	}

	public void deleteSubscription() {

		if (this.mode == "error") {
			eventCallback.control(messages.URL_ERROR);
			return;
		}

		if (this.mode == "publish") {
			eventCallback.control(messages.SDK_SUB_MODE);
			return;
		}

		socket.emit("unSubscribeQueue", this.subscription);

		Client client = this.webClient;

		WebResource webResource;
		try {
			webResource = client.resource(this.subscribeUrl + "/delete?id="
					+ this.subscription.get("id") + "&desc="
					+ this.subscription.get("desc"));
			ClientResponse response = webResource.accept("application/json")
					.entity(this.subscription.toString())
					.post(ClientResponse.class);

			if (response.getStatus() == 200) {
				eventCallback.control(messages.SUBSCRIPTION_DELETED);
				this.subscription = null;
			} else
				eventCallback.control(messages.UNKNWON_ERROR);
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	private JSONObject getRemoteSubscription() {

		System.out.println(this.subscribeUrl + "?id=" + this.id + "&desc="
				+ this.desc);
		WebResource webResource = this.webClient.resource(this.subscribeUrl
				+ "?id=" + this.id + "&desc=" + this.desc);

		ClientResponse response = webResource.accept("application/json").get(
				ClientResponse.class);

		JSONObject subscription = null;
		try {

			if (response.getStatus() != 200) {
				eventCallback.control(messages.URL_ERROR);
				subscription = null;
			} else {
				subscription = new JSONObject(response.getEntity(String.class))
						.getJSONArray("result").getJSONObject(0);
			}

		} catch (ClientHandlerException e1) {
			e1.printStackTrace();
			eventCallback.control(messages.UNKNWON_ERROR);
		} catch (UniformInterfaceException e1) {
			eventCallback.control(messages.URL_ERROR);
		} catch (JSONException e1) {
			e1.printStackTrace();
			eventCallback.control(messages.UNKNWON_ERROR);
		} catch (Exception e) {
			eventCallback.control(messages.UNKNWON_ERROR);
		}

		return subscription;
	}

	public JSONObject subscribe(AEONInterface eventCallback) {

		this.eventCallback = eventCallback;

		if (this.mode == "error") {
			eventCallback.control(messages.URL_ERROR);
			return null;
		}

		if (this.mode == "publish") {
			eventCallback.control(messages.SDK_SUB_MODE);
			return null;
		}

		if (!socket.isConnected()) {
			aeonCallback = new SocketIOEvents(eventCallback);
			socket.connect(aeonCallback);
		}

		if (this.subscription == null) {
			JSONObject requestedSubscription = getRemoteSubscription();
			if (requestedSubscription != null) {
				this.subscription = requestedSubscription;
				emitSubscriptionRequest();
			}

		} else
			emitSubscriptionRequest();

		return this.subscription;

	}

	public Integer publish(JSONObject data) {
		return publish(data, new DummyCallbacks());
	}

	public Integer publish(JSONObject data, AEONInterface eventCallback) {

		this.eventCallback = eventCallback;

		if (this.mode == "error") {
			eventCallback.control(messages.URL_ERROR);
			return null;
		}

		if (this.mode == "subscribe") {
			eventCallback.control(messages.SDK_PUB_MODE);
			return null;
		}

		try {
			Client client = this.webClient;

			WebResource webResource = client.resource(this.publishUrl);

			client.setConnectTimeout(4000);
			client.setReadTimeout(4000);

			ClientResponse response = webResource.accept("application/json")
					.type("application/json").entity(data.toString())
					.post(ClientResponse.class);

			JSONObject responseData;

			Integer code = new JSONObject(response.getEntity(String.class))
					.getInt("code");

			switch (code) {
			case 406:
				eventCallback.control(messages.URL_ERROR);
				break;
			}

			/*
			 * TODO: I am not able to catch ConnectionExceptions!!! In that case
			 * I would like to send a control message INFRASTRUCTURE_DOWN
			 */

			return code;

		} catch (ClientHandlerException e1) {
			e1.printStackTrace();
			eventCallback.control(messages.UNKNWON_ERROR);
		} catch (UniformInterfaceException e2) {
			eventCallback.control(messages.URL_ERROR);
		} catch (JSONException e3) {
			e3.printStackTrace();
			eventCallback.control(messages.UNKNWON_ERROR);
		} catch (Exception e) {
			eventCallback.control(messages.UNKNWON_ERROR);
		}

		return null;
	}
}
