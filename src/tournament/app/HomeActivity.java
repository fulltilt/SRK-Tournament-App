  package tournament.app;

import java.util.ArrayList;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.Menu;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.support.v4.view.MenuItem;
import android.support.v4.widget.SimpleCursorAdapter;

public class HomeActivity extends FragmentActivity {
	private static final String TAG = "HomeActivity";
	private DBAdapter dbAdapter;
	private ListView listTournament;
	private ArrayList<String> tournaments = new ArrayList<String>();
	private Cursor cursor;
	SimpleCursorAdapter adapter;
	static final String[] FROM = { DBAdapter.KEY_TOURNAMENT_NAME, DBAdapter.KEY_DATE };  
	static final int[] TO = { R.id.item1, R.id.item2 };
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.d(TAG, "in onCreate()");
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        
        dbAdapter = new DBAdapter(this);
        dbAdapter.open();	// DB doesn't get created until I call 'open()' which in turn calls 'getWriteableDatabase()'

        listTournament = (ListView) findViewById(R.id.listTournament);
    }
    
	protected void onResume() {
		Log.d(TAG, "in onResume()");
        super.onResume();
        
        // display tournaments
        cursor = dbAdapter.getAllTournaments();
        startManagingCursor(cursor);
       
        // Set up the adapter
        if (cursor.getCount() == 0)		// for the initial situation when there's no tournaments in the database
        {
        	tournaments.add("Tap to add a tournament");
        	listTournament.setAdapter(new ArrayAdapter<String>(this, R.layout.one_item_row, R.id.name, tournaments));
        	listTournament.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                	Intent newActivity = new Intent(view.getContext(), TournamentEntry.class);  
                	newActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(newActivity);
                }
            });
        }
        else
        {
	        adapter = new SimpleCursorAdapter(this, R.layout.two_item_row, cursor, FROM, TO, 0);  // 
	        listTournament.setAdapter(adapter); // 
        	listTournament.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                	
                	// send the name of the tournament to the next Activity
                	Bundle extras = new Bundle();
                	cursor.moveToPosition(position);

					String tournamentID = cursor.getString(cursor.getColumnIndex(DBAdapter.KEY_ROWID));       
					String name = cursor.getString(cursor.getColumnIndex(DBAdapter.KEY_TOURNAMENT_NAME));

					cursor = dbAdapter.getBrackets(tournamentID, "");		
					startManagingCursor(cursor);       

					Intent newActivity = null;
                	if (cursor.getCount() == 0) {
	                	newActivity = new Intent(view.getContext(), EntrantList.class);  
	                	
	                	extras.putString("name", name);
	                	extras.putString("tournamentID", tournamentID);
	                	newActivity.putExtras(extras);
	                	newActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	                    startActivity(newActivity);
                	}
                	else {
                		cursor = dbAdapter.getTournamentFormat(tournamentID); // returns tournament format
                		startManagingCursor(cursor);
                		cursor.moveToFirst();

                		// if tournament format is '1', go to SingleEliminationBracket else go to DoubleEliminationBracket
                		if (cursor.getInt(cursor.getColumnIndex(DBAdapter.KEY_FORMAT)) == 1)
                			newActivity = new Intent(view.getContext(), SingleEliminationBracket.class);
                		else
                			newActivity = new Intent(view.getContext(), DoubleEliminationBracket.class);
	                	
	                	extras.putString("name", name);
	                	extras.putString("tournamentID", tournamentID);
	                	newActivity.putExtras(extras);
	                	newActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	                    startActivity(newActivity); 
                	}
                }
            });
        }
        
        //cursor.close();
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_actionbar, menu);

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
	        case R.id.menu_add:
	            // plus icon in action bar clicked; go home
	            intent = new Intent(this, TournamentEntry.class);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
        }
    }

    public void onDestroy() {
        super.onDestroy();
     
        // close the database
        dbAdapter.close();
    }    
}
