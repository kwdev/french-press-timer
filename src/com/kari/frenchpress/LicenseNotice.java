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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.InputStreamReader;

/**
 * 
 * This class shows a dialog with license information if it has not already 
 * been acknowledged by the user.
 * 
 * 
 * @author kari
 *
 */
public class LicenseNotice {

	private static final String LICENSE_ACKNOWLEDGED = "gpl3_license_accepted";
	private static final String LICENSE_ASSET = "LICENSE";
	private static final String LICENSE_PREFERENCES = "license_preferences";

	
	static void check(final Activity activity) {


		final SharedPreferences preferences = activity.getSharedPreferences(LICENSE_PREFERENCES, Context.MODE_PRIVATE);

		if (!preferences.getBoolean(LICENSE_ACKNOWLEDGED, false)) {
			
			final AlertDialog.Builder builder = new AlertDialog.Builder(
					activity);
			builder.setTitle(R.string.license_title);
			builder.setCancelable(true);
			
			builder.setPositiveButton(R.string.license_ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					// The user said OK, save in the shared preferences
					
					preferences.edit().putBoolean(LICENSE_ACKNOWLEDGED, true).commit();
					
				}
			});


			// Construct dialog content
			
			StringBuilder content = new StringBuilder(activity.getString(R.string.license_copy)).append("\n\n");
			content.append(activity.getString(R.string.license_warranty)).append("\n\n");
			content.append(activity.getString(R.string.license_info));
			
			builder.setMessage(content);
			
			builder.create().show();
		}
	}


	/**
	 * Reads the license asset as lines of text
	 * 
	 * @param activity
	 * @return
	 */
	public static CharSequence getLicenseText(Activity activity) {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(activity.getAssets().open(LICENSE_ASSET)));
			String line;
			StringBuilder buffer = new StringBuilder();
			while ((line = in.readLine()) != null) {
				buffer.append(line).append('\n');
			}
			return buffer;
		} catch (IOException e) {
			return "";
		} finally {
			closeStream(in);
		}
	}

	/**
	 * Closes the given stream.
	 * 
	 * @param stream
	 */
	private static void closeStream(Closeable stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				// Ignore
			}
		}
	}
}
