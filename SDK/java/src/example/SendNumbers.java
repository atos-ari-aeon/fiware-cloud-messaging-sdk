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

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.util.concurrent.TimeUnit;

import net.atos.aeon.AEONInterface;
import net.atos.aeon.AEONSDK;

import org.json.JSONException;
import org.json.JSONObject;

import example.ReceiveNumbers.MyAEONCallbacks;

public class SendNumbers {
	
	
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
	 */
	public static void main(String[] args) throws MalformedURLException {

		
		
		AEONSDK sdk = new AEONSDK(Config.PUB_URL);

		
		for (int i = 0; i <= 500; i++) {
			JSONObject data = new JSONObject();
			try {
				data.put("number", i);
				System.out.println(data.toString());
				sdk.publish(data, new MyAEONCallbacks());
				TimeUnit.MICROSECONDS.sleep(200);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}

	}

}
