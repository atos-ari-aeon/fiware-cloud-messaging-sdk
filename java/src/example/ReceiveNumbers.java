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
 

package example;

import java.net.MalformedURLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.atos.aeon.AEONInterface;
import net.atos.aeon.AEONSDK;

import org.json.JSONException;
import org.json.JSONObject;

public class ReceiveNumbers {
	
	
	public static class MyAEONCallbacks implements AEONInterface {
		@Override
		public void deliveredMessage(JSONObject data) {
			System.out.println("Event received" + data.toString());
		}

		@Override
		public void control(JSONObject data) {
			System.out.println("Control Message" + data.toString());
			
		}

	}

	/**
	 * @param args
	 * @throws MalformedURLException
	 * @throws JSONException 
	 */
	/**
	 * @param args
	 * @throws MalformedURLException
	 * @throws JSONException
	 */
	public static void main(String[] args) throws MalformedURLException, JSONException {
		// TODO Auto-generated method stub

		JSONObject previousSub = new JSONObject("{'id':'test-java-666','desc':'test-java-666', 'subkey':'GPS-71454020-queu'}");
		
		MyAEONCallbacks myCallBack = new MyAEONCallbacks();
		final AEONSDK sdk = new AEONSDK(Config.SUB_URL, Config.YOUR_ID, Config.YOUR_DESC);
		//final AEONSDK sdk = new AEONSDK(Config.SUB_URL, previousSub);
		JSONObject subscription = sdk.subscribe(myCallBack);		
		
		if (subscription == null){
			System.out.println("Something went wrong I dont have a subscription");
			return;
		}
		System.out.println("subscription received " + subscription.toString());
		

		try {
			TimeUnit.SECONDS.sleep(3);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
/*	
		sdk.deleteSubscription();
		
		System.out.println("Deleted subscription: if some one is publish in the channel you will lost it for ever.");
		
		try {
			TimeUnit.SECONDS.sleep(3);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Ok, lets have another new subscription.");
		
		subscription = sdk.subscribe(myCallBack);		*/
		
		ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
		exec.scheduleAtFixedRate(new Runnable() {
			boolean paused = false;
		  @Override
		  public void run() {

		    System.out.println("Ok lets make a little break of 5 seconds. After that the subscription" +
					" will continue. And messages published while our break will be delivered inmediatly");
		    if (!paused){
		    	sdk.pauseSusbscription();
		    	paused = true;
		    }else{
		    	sdk.continueSubscription();
		    	paused = false;
		    }

		  }
		}, 1, 5, TimeUnit.SECONDS);
		
	}

}
