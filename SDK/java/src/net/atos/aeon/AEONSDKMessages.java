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

import org.json.JSONException;
import org.json.JSONObject;

public class AEONSDKMessages {

	// messages only during publishing mode

	JSONObject UNKNWON_ERROR = null;
	JSONObject INFRASTRUCTURE_DOWN = null;
	JSONObject URL_ERROR = null;
	JSONObject SDK_SUB_MODE = null;

	// during subscription mode you have the
	// the previous one (except SDK_SUB_MODE), plus the following

	JSONObject SDK_PUB_MODE = null;
	JSONObject SUBSCRIPTION_CORRECT = null;
	JSONObject SUBSCRIPTION_INCORRECT = null;
	JSONObject SUBSCRIPTION_DELETED = null;
	JSONObject SUBSCRIPTION_LOCKED = null;
	JSONObject NOT_SUBSCRIBED = null;
	JSONObject UNSUBSCRIBED = null;

	JSONObject INFRASTRUCTURE_UP = null;

	public AEONSDKMessages() {
		try {

			UNKNWON_ERROR = new JSONObject("{ " + "code:0," + "error:true,"
					+ "msg:'Unknwon error'}");

			URL_ERROR = new JSONObject("{" + "code:1," + "error:true,"
					+ "msg:'Bad URL'}");

			INFRASTRUCTURE_DOWN = new JSONObject("{ " + "code:3,"
					+ "error:true,"
					+ "msg:'Communication infrastructure down'}");

			INFRASTRUCTURE_UP = new JSONObject("{ " + "code:50,"
					+ "error:false," + "msg:'Communication infrastructure up'}");

			SDK_PUB_MODE = new JSONObject(
					"{"
							+ "code:100,"
							+ "error:true,"
							+ "msg:'Operation Denied. SDK operating in Publication Mode'}");

			SDK_SUB_MODE = new JSONObject(
					"{"
							+ "code:101,"
							+ "error:true,"
							+ "msg:'Operation Denied. SDK operating in Subscription Mode'}");

			SUBSCRIPTION_CORRECT = new JSONObject("{" + "code:250,"
					+ "error:false," + "msg:'You have been subscribed'}");

			SUBSCRIPTION_DELETED = new JSONObject("{" + "code:251,"
					+ "error:false,"
					+ "msg:'Your subscription has been deleted'}");

			UNSUBSCRIBED = new JSONObject("{" + "code:252," + "error:false,"
					+ "msg:'You have been unsubscribed'}");

			SUBSCRIPTION_LOCKED = new JSONObject(
					"{"
							+ "code:201,"
							+ "error:true,"
							+ "msg:'This subscription is been used by other process (locked)'}");

			NOT_SUBSCRIBED = new JSONObject("{" + "code:202," + "error:true,"
					+ "msg:'You are not subscribed'}");


			SUBSCRIPTION_INCORRECT = new JSONObject("{" + "code:203,"
					+ "error:false," + "msg:'Subscription incorrect, bad request'}");

		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	public JSONObject controlTranslator(JSONObject message) {

		Integer code;
		try {
			code = message.getInt("code");
			switch (code) {
			case 102:
				return INFRASTRUCTURE_UP;
			case 103:
				return INFRASTRUCTURE_DOWN;
			case 201:
				return SUBSCRIPTION_INCORRECT;
			case 204:
				return SUBSCRIPTION_LOCKED;
			case 250:
				return SUBSCRIPTION_CORRECT;
			case 251:
				return SUBSCRIPTION_DELETED;
			case 401:
				return NOT_SUBSCRIBED;
			case 450:
				return UNSUBSCRIBED;

			}

			return UNKNWON_ERROR;
		} catch (JSONException e) {
			e.printStackTrace();
			return UNKNWON_ERROR;
		}

	}

}
