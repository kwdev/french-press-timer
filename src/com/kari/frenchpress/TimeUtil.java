/**
 *  Copyright 2012 Kari Wilhelm
 * 
 *  This file is part of French Press Timer.
 *  
 *  French Press Timer is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  French Press Timer is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with French Press Timer.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */ 

package com.kari.frenchpress;

public class TimeUtil {

	public static String format(long seconds) {
		
		
		long minutes = seconds / 60;
		
		long secs = seconds - (minutes * 60);
		
		String time = ""+minutes+":";
		
		if (secs < 10) {
			time += "0";
		}

		time += secs;
		
		return time;
	}
	
}
