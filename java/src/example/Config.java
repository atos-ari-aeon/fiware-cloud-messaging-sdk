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

public final class Config  {
	
	/*
	 * Remember to get your PUB_URL and SUB_URL
	 * from one AEON Channel. You can do this easily
	 * through the AEON Dashboard
	 * 
	 */
	
	/*
	 * Config for subscriptions. If you get
	 * errors about subscription in use try
	 * with other id and desc values.
	 * 
	 * These values represents your process
	 * in the AEON network and needs to be
	 * unique.
	 * 
	 */
	
	//public static final String SUB_URL = "http://localhost:3000/subscribe/f057efeb-5c52-4e48-81fd-23851ab6ea01";
	public static final String SUB_URL =  "SUB_URL";
	public static final String YOUR_ID = "YOUR_ID";
	public static final String YOUR_DESC = "YOUR_DESC";
	
	/*
	 * Config for publishing
	 */
	
	//public static final String PUB_URL = "http://localhost:3000/publish/fbb53d7e-ab46-4a4d-bfb0-6415eb90d134";
	public static final String PUB_URL = "PUB_URL";
}