package org.openintents.cloudsync;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openintents.notepad.NotePad;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

public class AsyncChangedNotes extends AsyncTask<Void, Void, String[][]>{
	private static StringBuilder jsonBuilder = new StringBuilder();
	SyncActivity activity;
	static String tag = "vincent";
	static String TAG = "AsyncChangedNotes";
	static String[] PROJECTIONALL = new String[] {
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
	public AsyncChangedNotes(SyncActivity activity) {
		this.activity = activity;
	}

	@Override
	protected String[][] doInBackground(Void... params) {
		
		Log.d(tag, "inside the async Change notes");
		jsonBuilder = new StringBuilder(); // needed so that previous results are not saved in builder.
		/**
		 *Note: No need for this section of code.  
		 
		Uri notesUri = Uri.parse(NotePad.Notes.CONTENT_URI.toString());
		Cursor cursor = activity.getContentResolver().query(notesUri, PROJECTIONALL, null, null, null);
		
		if (cursor.moveToFirst()) { }

        int totRows = cursor.getCount();
        Log.d(tag, "total row in notes table:-> "+totRows );
        for(int i=0;i<totRows;i++) {
        	Log.d(tag, cursor.getString(0));
        	Log.d(tag, cursor.getString(1));
        	Log.d(tag, cursor.getString(2));
            cursor.moveToNext();
        }
        */
        
        String MOD_AUTHORITY = "org.openintents.mods.contentprovider";
		String MOD_BASE_PATH = "modifys";
		
        Uri modUri = Uri.parse("content://" + MOD_AUTHORITY + "/" + MOD_BASE_PATH);
        Log.d(TAG, "uri is:-> "+modUri.toString());//del this
		Cursor modCursor = activity.getContentResolver().query(modUri, null, null, null, null);
		
		long[][] modMatrix = getmodMatrix(modCursor);
		
		/**
		if(modMatrix.length==0) {
			Log.d(TAG, "modMatrix lenght returned is 0");
			modCursor.close();
			addAllNotesToModTable();
		} else {
		*/
			Log.d(TAG, "lenght of the modMatrix is:-> "+modMatrix.length);
			
			Uri notesUri = Uri.parse(NotePad.Notes.CONTENT_URI.toString());
			Cursor cursor = activity.getContentResolver().query(notesUri, PROJECTIONALL, null, null, null);
			cursor.moveToFirst();
			int totRowsn = cursor.getCount();
			for(int i=0;i<totRowsn;i++) {
				Log.d(TAG, "going into checkAndAdd with note:-> "+cursor.getString(1));
				checkAndAdd(cursor,modMatrix);
				cursor.moveToNext();
			}
			// maybe cursor can be close
		//} else
		// testing the results..
		modCursor.close();
		modCursor = activity.getContentResolver().query(modUri, null, null, null, null);
		int totRows = modCursor.getCount();
		modCursor.moveToFirst();
		for(int i=0;i<totRows;i++) {
			Log.d(TAG, "TEsting the last: "+modCursor.getString(0)+" "+modCursor.getString(1));
			modCursor.moveToNext();
		}
		//jsonbuilder.length() - 1 is needed to eliminate the last comma in the building process
		String jsonBuilderString = "";
		if(jsonBuilder.length()>1) {
			jsonBuilderString = jsonBuilder.substring(0, jsonBuilder.length()-1);
		}
		String jsonData = "{ \"data\" : [" + jsonBuilderString + "] }";
		Log.d(TAG, jsonData);
		try {
		    JSONObject mainJobj = new JSONObject(jsonData);
			JSONArray jarray = mainJobj.getJSONArray("data");
		} catch (JSONException e) {
			Log.d(TAG, "exception in main json arra",e);
		}
		String[][] retJson = new String[1][1];
		retJson[0][0] = jsonData;

		return retJson;
	}

	private void checkAndAdd(Cursor cursor, long[][] modMatrix) {
		long localModDate = Long.parseLong(cursor.getString(4));
		long localId = Long.parseLong(cursor.getString(0));
		String MOD_AUTHORITY = "org.openintents.mods.contentprovider";
		String MOD_BASE_PATH = "modifys";
        Uri modUri = Uri.parse("content://" + MOD_AUTHORITY + "/" + MOD_BASE_PATH);
        boolean flag=true;
		for(int i=0;i<modMatrix.length;i++) {
			flag=true;
			if(localId==modMatrix[i][1]) {
				flag=false;
				if(localModDate==modMatrix[i][2]) {
					//do nothing this means the modification has not been done to this note
					// coz id and modDate are same compared to the previous sync data.
					Log.d(TAG, "do nothing for:-> "+cursor.getString(1));
					break;
				}
				else {
					ContentValues values = new ContentValues();
					values.put("localid", localId);
					values.put("moddate", localModDate);
					values.put("pckname", activity.getPackageName());
					modUri = Uri.withAppendedPath(modUri, Long.toString(modMatrix[i][0])); // this is going to update _id[0] with values of the present sync
					int returnVal = activity.getContentResolver().update(modUri, values, null, null);// return val is 1 if success.
					Log.d(TAG, "modifying the value with _id:-> "+modMatrix[i][0]+" localId:-> "+localId+" result:-> "+returnVal);
					addToJson(cursor);
					break;
				}
			}
		}
		if(flag) { // this means the record with this id was not found in previous sync db. i.e this is new note
			ContentValues values = new ContentValues();
			values.put("localid", localId);
			values.put("moddate", localModDate);
			values.put("pckname", activity.getPackageName());
			Uri insertUri = activity.getContentResolver().insert(modUri, values);
			Log.d(TAG, "inserting the new value localId:-> "+localId+" retUri is:-> "+insertUri.toString());
			addToJson(cursor);
		}
		// Do NOT close cursor
		
	}

	private void addToJson(Cursor cursor) {
		
		// this methods makes the json string by adding the notes to one json string
		
        String id=cursor.getString(0);
		String title=cursor.getString(1);
		String note=cursor.getString(2);
		String created_date=cursor.getString(3);
		String modified_date=cursor.getString(4);
		String tags=cursor.getString(5);
		String encrypted=cursor.getString(6);
		String theme=cursor.getString(7);
		String selection_start=cursor.getString(8);
		String selection_end=cursor.getString(9);
		String scroll_position=cursor.getString(10);
		
		String jsonNoteString= " { \"id\": \" "+id+" \" , \"jsonString\": { \"title\": \" "+title+" \", \"note\": \" "+note+" \", \"created_date\": \" "+created_date+" \", \"modified_date\": \" "+modified_date+" \", \"tags\": \" "+tags+" \", \"encrypted\": \" "+encrypted+" \", \"theme\": \" "+theme+" \", \"selection_start\": \" "+selection_start+" \", \"selection_end\": \" "+selection_end+" \",\"scroll_position\": \" "+scroll_position+" \" } }  ";
		Log.d(TAG, jsonNoteString);
		jsonBuilder.append(jsonNoteString);
		jsonBuilder.append(",");
		try {
			JSONObject jNoteObject = new JSONObject(jsonNoteString);
		       
		       Log.d(TAG,"id is:-> "+jNoteObject.getString("id"));
		       JSONObject jsonStringObj = jNoteObject.getJSONObject("jsonString");
		       Log.d(TAG,"note head is:-> "+jsonStringObj.getString("title"));
		       Log.d(TAG,"note main body:-> "+jsonStringObj.getString("note"));
		       Log.d(TAG,"note select start:-> "+jsonStringObj.getString("selection_start"));
		} 
		catch (JSONException e) {
			Log.d(TAG, "json exception occured",e);
		}
		
	}

	private void addAllNotesToModTable() {
		Uri notesUri = Uri.parse(NotePad.Notes.CONTENT_URI.toString());
		Cursor cursor = activity.getContentResolver().query(notesUri, PROJECTIONALL, null, null, null);
		cursor.moveToFirst();
		String MOD_AUTHORITY = "org.openintents.mods.contentprovider";
		String MOD_BASE_PATH = "modifys";
        Uri modUri = Uri.parse("content://" + MOD_AUTHORITY + "/" + MOD_BASE_PATH);
        ContentValues values=new ContentValues();
		int totRows = cursor.getCount();
		for(int i=0;i<totRows;i++) {
			values.put("localid", cursor.getString(0));
			values.put("moddate", cursor.getString(4));
			values.put("pckname", activity.getPackageName());
			Uri insertUri = activity.getContentResolver().insert(modUri, values);
			Log.d(TAG, "the inserted Uri is:-> "+insertUri.toString());
			Log.d(TAG,"the inserted value is:-> "+cursor.getString(0)+" "+cursor.getString(4));
			cursor.moveToNext();
		}
		cursor.close();
	}

	private long[][] getmodMatrix(Cursor modCursor) {
		
		
		//TODO only the elements with same package must be in the long
		if(modCursor==null | modCursor.getCount()==0) {
			Log.d(TAG, "it is null");
			return new long[0][3];
		}
		modCursor.moveToFirst();
		Log.d(TAG, "lenght returned is:-> "+modCursor.getCount());// test all the elemets..
		//Log.d(TAG, "some elements of first element:-> "+modCursor.getString(0)+" "+modCursor.getString(1)+" "+modCursor.getString(2)+" ");
		long[][] modMatrix = new long[modCursor.getCount()][3];
		int totRows = modCursor.getCount();
		
		for(int i=0;i<totRows;i++){
			modMatrix[i][0] = Long.parseLong(modCursor.getString(0));
			modMatrix[i][1] = Long.parseLong(modCursor.getString(1));
			modMatrix[i][2] = Long.parseLong(modCursor.getString(2));
			modCursor.moveToNext();
		}
		
		for(int i=0;i<totRows;i++){
			Log.d(TAG, "the modMatrix:-> "+modMatrix[i][0]+" "+modMatrix[i][1]+" "+modMatrix[i][2]);
			
		}
		return modMatrix;
	}

	@Override
	protected void onPostExecute(String[][] result) {
		Log.d(TAG, "inside post execute");
		// checked for the case when there is no modification....
		String jsonString = result[0][0];
		if(SyncAdapter.isIntentAvailable(activity, "vincent.start")) {
			Intent syncIntent = new Intent("vincent.start");
			syncIntent.putExtra("data", jsonString);
			syncIntent.putExtra("package", activity.getPackageName());
			activity.startActivityForResult(syncIntent, SyncActivity.SYNC_REQUEST_CODE);
		}
		super.onPostExecute(result);
	}
	

}
