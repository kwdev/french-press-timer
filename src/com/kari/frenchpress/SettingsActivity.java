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

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * 
 * Allow for preferences to be saved in the SharedPreferences
 * for this app. PreferenceActivity takes care of the saving.
 * 
 * @author kari
 *
 */
public class SettingsActivity extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preferences);
	}

}
