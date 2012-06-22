package org.openintents.cloudsync;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openintents.notepad.NotePad;
import org.openintents.notepad.NotePadProvider;
import org.openintents.notepad.NotePad.Notes;

import android.content.ContentValues;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.util.Log;

public class AsyncApplyResult extends AsyncTask<String, Void, String>{
	static String tag = "vincent";
	static String TAG = "AsyncApplyResult";
	private static final boolean debug = true;
	SyncActivity activity;
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
	
	public AsyncApplyResult(SyncActivity activity) {
		this.activity = activity;
	}
	
	@Override
	protected String doInBackground(String... params) {
		
		if (debug) Log.v(TAG, "do in back of apply result");
		String jsonData = params[0];
		try {
			JSONObject jsonMainObj = new JSONObject(jsonData);
			JSONArray jsonArray = jsonMainObj.getJSONArray("data");
			if(jsonArray.length() == 0){
				// this means nothing is to be updated back in OI Notes
				return null;
			}
			
			for(int i=0;i<jsonArray.length();i++){
				JSONObject jobj = jsonArray.getJSONObject(i);
				
				int localId = jobj.getInt("id");
				if(localId==-1) {
					// No id is there which means it needs to be inserted and value updated to IdMapTable
					insertNote(jobj);
				}
				else {
					// Note is already present and we also have the Id. Simple updation needs to be done.
					JSONObject noteobj = jobj.getJSONObject("jsonString");
					
					Uri notes = Uri.parse(NotePad.Notes.CONTENT_URI.toString());
					notes = Uri.withAppendedPath(notes, String.valueOf(localId));
					
					ContentValues values = new ContentValues();
					values.put(Notes.TITLE, noteobj.getString("title").trim());
					values.put(Notes.NOTE, noteobj.getString("note").trim());
					values.put(Notes.CREATED_DATE, noteobj.getLong("created_date") );
					values.put(Notes.MODIFIED_DATE, noteobj.getLong("modified_date"));
					
					if(!(noteobj.getString("tags").trim().equals("null"))) {
						values.put(Notes.TAGS, noteobj.getString("tags"));
					}
					
					if(!(noteobj.getString("encrypted").trim().equals("null"))) {
						values.put(Notes.ENCRYPTED, noteobj.getLong("encrypted"));
					}
					
					if(!(noteobj.getString("theme").trim().equals("null"))) {
						values.put(Notes.THEME, noteobj.getString("theme"));
					}
					
					
					values.put(Notes.SELECTION_START, noteobj.getLong("selection_start"));
					values.put(Notes.SELECTION_END, noteobj.getLong("selection_end"));
					values.put(Notes.SCROLL_POSITION, noteobj.getDouble("scroll_position"));
					
					int returnInt = activity.getContentResolver().update(notes, values, null, null);
					if (debug) Log.d(TAG, "after updating "+noteobj.getString("title")+" return values is: "+returnInt);
					
				}
			}
		} catch (JSONException e) {
			
			e.printStackTrace();
		}
		
		return null;
	}

	private void insertNote(JSONObject jobj) {
		
		
		try {
			//long localId = jobj.getLong("id"); // just for checking del this
			long gId = jobj.getLong("googleId");
			JSONObject noteobj = jobj.getJSONObject("jsonString");
			
			ContentValues values = new ContentValues();
			values.put(Notes.TITLE, noteobj.getString("title").trim());
			values.put(Notes.NOTE, noteobj.getString("note").trim());
			values.put(Notes.CREATED_DATE, noteobj.getLong("created_date") );
			values.put(Notes.MODIFIED_DATE, noteobj.getLong("modified_date"));
			
			if(!(noteobj.getString("tags").trim().equals("null"))) {
				values.put(Notes.TAGS, noteobj.getString("tags"));
			}
			
			if(!(noteobj.getString("encrypted").trim().equals("null"))) {
				values.put(Notes.ENCRYPTED, noteobj.getLong("encrypted"));
			}
			
			if(!(noteobj.getString("theme").trim().equals("null"))) {
				values.put(Notes.THEME, noteobj.getString("theme"));
			}
			
			values.put(Notes.SELECTION_START, noteobj.getLong("selection_start"));
			values.put(Notes.SELECTION_END, noteobj.getLong("selection_end"));
			values.put(Notes.SCROLL_POSITION, noteobj.getDouble("scroll_position"));
			
			Uri notesUri = Uri.parse(NotePad.Notes.CONTENT_URI.toString());
			Uri insertUri = activity.getContentResolver().insert(notesUri, values);
			if (debug) Log.v(TAG, "inserted into the notepad: "+insertUri);
			// Now insert the new got Id form insertUri into idMapTable
			String IDMAP_AUTHORITY = "org.openintents.idmap.contentprovider";
			
			String IDMAP_BASE_PATH = "idmaps";
			Uri IDMAP_CONTENT_URI = Uri.parse("content://" + IDMAP_AUTHORITY
					+ "/" + IDMAP_BASE_PATH);
			
			String insertRetId = insertUri.getLastPathSegment();
			ContentValues idmapValues = new ContentValues();
			idmapValues.put("localid", Long.parseLong(insertRetId));
			idmapValues.put("appid", gId);
			idmapValues.put("pckname", activity.getPackageName());
			
			activity.getContentResolver().insert(IDMAP_CONTENT_URI, idmapValues);
			
		} catch (JSONException e) {
			
			e.printStackTrace();
		}
		
	}

	@Override
	protected void onPostExecute(String result) {
		
		activity.syncComplete();
		super.onPostExecute(result);
	}
	
	

}
