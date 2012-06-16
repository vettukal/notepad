package org.openintents.cloudsync;

import org.openintents.notepad.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class SyncActivity extends Activity{
	public final static int SYNC_REQUEST_CODE = 12035; 
	private static final String TAGv = "debugv";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sync_layout);
		
		// it would be better if this activity is called by the NotesList for result.
		// right now there is no prob.
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
		
		Log.d(TAGv, "inside syncing back the result");
		TextView syncText = (TextView) findViewById(R.id.sync_textview);
		syncText.setText("Adding the notes fetched from server");
		AsyncApplyResult aar = new AsyncApplyResult(this);
		Bundle b = intent.getExtras();
		String jsonData = "";
		if(b.containsKey("jsonData")) {
			jsonData = b.getString("jsonData");
		}
		aar.execute(jsonData);
	}
	
	void syncComplete() {
		TextView syncText = (TextView) findViewById(R.id.sync_textview);
		syncText.setText("Synchroniztion complete");
		finish();
	}

}
