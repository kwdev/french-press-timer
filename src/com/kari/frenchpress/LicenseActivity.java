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
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;


/**
 * 
 * @author kari
 *
 */
public class LicenseActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.license);
		
		TextView textView = (TextView) findViewById(R.id.text_content);
		
		textView.setText(Html.fromHtml(LicenseNotice.getLicenseText(this).toString()));
		
	}
	

/*
	public void onSaveInstanceState(Bundle savedInstanceState) {
		
		super.onSaveInstanceState(savedInstanceState);
		
	}


	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

	}	*/
	
	@Override
	protected void onPause() {
		super.onPause();
		
		this.finish();
	}


	public void close(View button) {
		this.finish();
	}
	
	
}
