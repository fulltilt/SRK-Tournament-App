package tournament.app;

import tournament.app.DBAdapter;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.*;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;

public class PlayerEntry extends FragmentActivity {
	private static final String TAG = "PlayerEntryAppActivity";
	private DBAdapter dbAdapter;
	private ArrayList<String> entrants;
	private ArrayAdapter<String> adapter;
	//private AutoCompleteTextView textView;
	//private EditText playerName;
	private AutoCompleteTextView playerName;
	private TextView tournamentLabel;
	private String tournamentName;
	private String tournamentID;	
	private int index;
	
    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_entry);
        
        dbAdapter = new DBAdapter(this);
        dbAdapter.open();	

        tournamentLabel = (TextView) findViewById(R.id.textView1);

        String[] players;
        Cursor cursor = dbAdapter.getAllPlayers();
        startManagingCursor(cursor);
/*       
        if(cursor.getCount() > 0)
        {
            players = new String[cursor.getCount()];
            int i = 0;
 
            while (cursor.moveToNext())
            {
                 players[i] = cursor.getString(cursor.getColumnIndex(DBAdapter.KEY_PLAYER_NAME));
                 i++;
             }
        }
        else
        {
            players = new String[] {};
        }
*/
players = new String[] {};	// temporarily commented above out due to errors

        // have the player name entry field autocomplete options come from database
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, players);
        playerName = (AutoCompleteTextView) findViewById(R.id.editText2);
        playerName.setThreshold(3);
        playerName.setAdapter(adapter);        
        Log.d(TAG, "onCreated");
    }

	protected void onResume() {
        super.onResume();
        
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	if (extras.getString("name") != null)
        		tournamentName = extras.getString("name");

        	if (extras.getStringArrayList("entrants") != null)
        		entrants = extras.getStringArrayList("entrants");

        	if (extras.getString("tournamentID") != null)
        		tournamentID = extras.getString("tournamentID"); 
   
        	index = extras.getInt("index"); 
        }        
        
        tournamentLabel.setText(tournamentName);
    
        // this section is when user clicks on an item in the Entrant List which allows them to edit the entrant name
        if (index != -1) {
        	playerName.setText(entrants.get(index));
        	entrants.remove(index);
        }
	}
	
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.entry_actionbar, menu);

        return true;
    } 
    
    public boolean onOptionsItemSelected(MenuItem item) { 
    	Intent intent = null;
    	
        switch (item.getItemId()) {
	        case android.R.id.home:
	            // app icon in action bar clicked; go home
	            intent = new Intent(this, HomeActivity.class);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
	            return true;
	        case R.id.menu_save:
	            // plus icon in action bar clicked; go home
	        	String name = playerName.getText().toString();
	        	if (name.length() == 0) {
	        		Toast.makeText(getBaseContext(), "Invalid Player Name!!", Toast.LENGTH_LONG).show();
	        		break;
	        	} 
	        	else if (entrants.contains(name)) {
	        		Toast.makeText(getBaseContext(), "Player name already on the list!!", Toast.LENGTH_LONG).show();
	        		break;
	        	}
	        		
	        	else {
	        		if (index != -1)	// maintain the order if we're editing a ListView item
	        			entrants.add(index, name);
	        		else
	        			entrants.add(name);	
	        	}
	        		
	            
	        	// if the row is added, insert returns a 'long' id of the row. Once this is done, go back to home
long rowID = dbAdapter.insertPlayer(name, "");  /**** phone blank for now ****/
//long rowID = 1;
	            if (rowID != -1) {
	            	Toast.makeText(getBaseContext(), "Player entered successfully!", Toast.LENGTH_LONG).show();
		            intent = new Intent(this, EntrantList.class);
		            
                	// send the ArrayList of entrants to the next Activity
                	Bundle extras = new Bundle();
                	extras.putString("tournamentID", tournamentID);
                	extras.putString("name", tournamentName);
                	extras.putStringArrayList("entrants", entrants);
                	intent.putExtras(extras);
                	
		            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		            startActivity(intent);
	            }
	            
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
        }
        
        return false;
    }    
    
    public void displayPlayer(Cursor c)
    {
        Toast.makeText(this, 
                "id: " + c.getString(0) + "\n" +
                "Name: " + c.getString(1) + "\n" +
                "Phone:  " + c.getString(2),
                Toast.LENGTH_LONG).show();        
    } 

    public void onDestroy() {
        super.onDestroy();
     
        // close the database
        dbAdapter.close();
    }    
}