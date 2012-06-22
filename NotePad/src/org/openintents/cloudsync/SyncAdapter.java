package org.openintents.cloudsync;

import java.util.List;

import org.openintents.notepad.NotePad;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class SyncAdapter {
	private static final boolean debug = true;
	private static final String TAG = "SyncAdapter";
	public static void startSync(Activity fromAcitivity) {
		//Experimentation
		if (debug) Log.d(TAG, "inside the startSync of SyncAdapter");
		String[] PROJECTIONALL = new String[] {
	            NotePad.Notes._ID, // 0
	            NotePad.Notes.TITLE, // 1
	            NotePad.Notes.NOTE,//2
	            NotePad.Notes.CREATED_DATE,
	            NotePad.Notes.MODIFIED_DATE,//4
	            NotePad.Notes.TAGS,
	            NotePad.Notes.ENCRYPTED,//6
	            NotePad.Notes.THEME,
	            NotePad.Notes.SELECTION_START,//8
	            NotePad.Notes.SELECTION_END,
	            NotePad.Notes.SCROLL_POSITION//10
	        };
		
		Uri noteUri = fromAcitivity.getIntent().getData();
		
		Cursor cursor = fromAcitivity.getContentResolver().query(noteUri,
				PROJECTIONALL,
				null,
				null,
				null);
		
		
		int totRows = cursor.getCount();
		if (debug) Log.d(TAG, "the total number of rows"+totRows);
		if (cursor.moveToFirst()) { 
			for(int i=0;i<totRows;i++) {
				String tuple="";
				for(int j=0;j<11;j++) {
					tuple=tuple+cursor.getString(j)+":";
					
				}
				if (debug) Log.d(TAG, tuple);
				cursor.moveToNext();
			}
			cursor.deactivate();
			cursor.close();
		}
		
		if(isIntentAvailable(fromAcitivity.getApplicationContext(), "vincent.start")) {
			if (debug) Log.d(TAG, "vincent.start is available");
		}
		
		Intent syncIntent = new Intent(fromAcitivity,SyncActivity.class);
		fromAcitivity.startActivity(syncIntent);
	}
	
	 public static boolean isIntentAvailable(Context context, String action) {
         final PackageManager packageManager = context.getPackageManager();
         final Intent intent = new Intent(action);
         List<ResolveInfo> resolveInfo =
                 packageManager.queryIntentActivities(intent,
                         PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfo.size() > 0) {
                     return true;
             }
        return false;
     }

}
