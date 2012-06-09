package org.openintents.cloudsync;

import org.openintents.notepad.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class SyncActivity extends Activity{
	public final static int SYNC_REQUEST_CODE = 12035;  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sync_layout);
		
		TextView syncText = (TextView) findViewById(R.id.sync_textview);
		
		AsyncChangedNotes asn = new AsyncChangedNotes(this);
		asn.execute();
		
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		
		switch(requestCode) {
		case SYNC_REQUEST_CODE:
            syncResult(resultCode, intent);
            
            
            break;
		}
		super.onActivityResult(requestCode, resultCode, intent);
	}
	private void syncResult(int resultCode, Intent intent) {
		// TODO Auto-generated method stub
		AsyncApplyResult aar = new AsyncApplyResult(this);
		Bundle b = intent.getExtras();
		String jsonData = "";
		if(b.containsKey("jsonData")) {
			jsonData = b.getString("jsonData");
		}
		aar.execute(jsonData);
	}
	
	

}
