package org.openintents.notepad.cloudsync;

import org.openintents.notepad.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class SyncActivity extends Activity{
	public final static int SYNC_REQUEST_CODE = 12035; 
	private static final String TAG = "SyncActivity";
	private static final boolean debug = true;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sync_layout);
		
		TextView syncText = (TextView) findViewById(R.id.sync_textview);
		
		AsyncDetectChange adc = new AsyncDetectChange(this);
		adc.execute();
		//AsyncChangedNotes asn = new AsyncChangedNotes(this);
		//asn.execute();
		
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
		
		if (debug) Log.d(TAG, "inside syncing back the result");
		TextView syncText = (TextView) findViewById(R.id.sync_textview);
		syncText.setText("Adding the notes fetched from server");
		AsyncApplyResult aar = new AsyncApplyResult(this);
		Bundle b = intent.getExtras();
		String jsonData = "";
		String deleteData = "";
		if(b.containsKey("jsonData")) {
			jsonData = b.getString("jsonData");
		}
		if(b.containsKey("delete")) {
			deleteData = b.getString("delete");
		}
		
		aar.execute(new String[]{jsonData,deleteData});
	}
	
	void syncComplete() {
		TextView syncText = (TextView) findViewById(R.id.sync_textview);
		syncText.setText("Synchroniztion complete");
		finish();
	}

}
