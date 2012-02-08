package tournament.app;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
//import android.support.v4.app.ActionBar;
import android.support.v4.view.Menu;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.view.MenuItem;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;

public class Bracket extends FragmentActivity {
	private static final String TAG = "Bracket";
	private DBAdapter dbAdapter;
	private Cursor cursor;
	private SimpleCursorAdapter adapter;
	private ListView listWinnersBracket;
	static final String[] FROM = { DBAdapter.KEY_PLAYER1, DBAdapter.KEY_PLAYER2 };  
	static final int[] TO = { R.id.player1, R.id.player2 };
	private String tournamentID;
	private ArrayList<String> entrantsList = new ArrayList<String>(16);		// tournament starts with 16 players
	private ArrayList<String> losersList = new ArrayList<String>();
	private ArrayList<String> winnersListR2 = new ArrayList<String>(8);		// after 1st round, there are 8 players left in the winners bracket
	private ArrayList<String> winnersListR3 = new ArrayList<String>(4);		// after 2nd round, there are 4 players left in the winners bracket
	private ArrayList<String> winnersListR4 = new ArrayList<String>(2);		// after 3rd round, there are 2 players left in the winners bracket
	private String tempP1, tempP2;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.d(TAG, "In onCreate()");
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bracket);

        dbAdapter = new DBAdapter(this);
        dbAdapter.open();	// DB doesn't get created until I call 'open()' which in turn calls 'getWriteableDatabase()'

        listWinnersBracket = (ListView) findViewById(R.id.winnersBracket);

        Bundle extras = getIntent().getExtras();
        if (extras != null) 
        	if (extras.getString("tournamentID") != null)
        		tournamentID = extras.getString("tournamentID");  
        
        // create the initial list of entrants who will all initially be in winners bracket
        cursor = dbAdapter.getCurrentTournamentEntrants(tournamentID);
        startManagingCursor(cursor);
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false)
        {
        	entrantsList.add(cursor.getString(0));
        	entrantsList.add(cursor.getString(1));
        	cursor.moveToNext();
        }
/*    	
        // construct the list according to the proper seeding order (update: EntrantList correctly seeds the entrants so this block is not needed
        if (winnersList == null) {
        	winnersList = new ArrayList<String>();
	        winnersList.add(tempList.get(0)); winnersList.add(tempList.get(14)); winnersList.add(tempList.get(10)); winnersList.add(tempList.get(6));
	        winnersList.add(tempList.get(4)); winnersList.add(tempList.get(8)); winnersList.add(tempList.get(12)); winnersList.add(tempList.get(2));
	        winnersList.add(tempList.get(3)); winnersList.add(tempList.get(13)); winnersList.add(tempList.get(9)); winnersList.add(tempList.get(5));
	        winnersList.add(tempList.get(7)); winnersList.add(tempList.get(11)); winnersList.add(tempList.get(15)); winnersList.add(tempList.get(1));
	        tempList = null;
        }
*/
//for (String s : winnersList)
//	Log.d(TAG, s);

    }
    
	protected void onResume() {
		Log.d(TAG, "in onResume()");
        super.onResume();
 
        Bundle extras = getIntent().getExtras();
        if (extras != null) 
        	if (extras.getString("tournamentID") != null)
        		tournamentID = extras.getString("tournamentID");
        
        // display brackets
        cursor = dbAdapter.getBrackets(tournamentID);
        startManagingCursor(cursor);
        
        // Set up the adapter
	    adapter = new SimpleCursorAdapter(this, R.layout.bracket_row, cursor, FROM, TO, 0);   
	    adapter.setViewBinder(VIEW_BINDER);	// attach custom ViewBinder instance to vanilla adapter 
	    listWinnersBracket.setAdapter(adapter); // 
	    
	    // handler when user clicks on a ListView item (in this case, have Context menu pop up)
	    listWinnersBracket.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {    	
            	registerForContextMenu(listWinnersBracket); 
            	listWinnersBracket.setLongClickable(false);  // undo setting of this flag in registerForContextMenu
                openContextMenu(view);
            }
        });
    }
 
	// context menu that pops up when user clicks a item in the ListView
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, v, menuInfo);
    	
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
    	int bracketNum = info.position * 2;		// position refers to the position on the ListView. Multiplying by 2 since there are 2 entrants per position

    	tempP1 = entrantsList.get(bracketNum);
    	tempP2 = entrantsList.get(bracketNum + 1);
    	
    	menu.setHeaderTitle("Pick the winner of the match");
    	menu.add(1, 0, 0, tempP1);
    	menu.add(1, 1, 0, tempP2);
    }
    
    // handle when user clicks on a bracket context menu pops up that allows user to select a winner of a match
    public boolean onContextItemSelected(android.view.MenuItem item) {
    	if (item.getItemId() == 0) {	// '0' refers to the 1st player in the Context menu
    		dbAdapter.updateWinner(tournamentID, tempP1, tempP2, tempP1);
    		adapter.notifyDataSetChanged();	// notify the adapter that the ListView should be refreshed
    	}
    	else if (item.getItemId() == 1) {	// '1' refers to the 2nd player in the Context menu
    		dbAdapter.updateWinner(tournamentID, tempP1, tempP2, tempP2);
    		adapter.notifyDataSetChanged();	// notify the adapter that the ListView should be refreshed
    	}
    	
        return true;
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

    // View binder constant to inject business logic for brackets
    final ViewBinder VIEW_BINDER = new ViewBinder() { 

  	// called for each data element that needs to be bound to a particular view
      public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
    	  Log.d(TAG, "in ViewBinder");

    	  if (view.getId() == R.id.player1) {
    		  ((TextView)view).setText(cursor.getString(columnIndex));

    		  Cursor c = dbAdapter.getWinner(tournamentID, cursor.getString(columnIndex));
    		  startManagingCursor(c);
    		  if (c.getCount() == 1)
    			  ((TextView)view).setBackgroundResource(R.drawable.top_shape_winner);
    		  else
    			  ((TextView)view).setBackgroundResource(R.drawable.top_shape);
    		  
    		 
    	  }
    		  
    	  if (view.getId() == R.id.player2) {
    		  ((TextView)view).setText(cursor.getString(columnIndex));
    		  
    		  Cursor c = dbAdapter.getWinner(tournamentID, cursor.getString(columnIndex));
    		  startManagingCursor(c);
    		  if (c.getCount() == 1)
    			  ((TextView)view).setBackgroundResource(R.drawable.bottom_shape_winner);
    		  else
    			  ((TextView)view).setBackgroundResource(R.drawable.bottom_shape);
    		  
    		 
    	  }
/*
    	  Log.d(TAG, "View ID: " + Integer.valueOf(view.getId()).toString());
    	  Log.d(TAG, "Winners Bracket ID: " + Integer.valueOf(R.id.winnersBracket).toString());
    	  Log.d(TAG, "Losers Bracket ID: " + Integer.valueOf(R.id.losersBracket).toString());
    	  Log.d(TAG, "Player1 ID: " + Integer.valueOf(R.id.player1).toString());
    	  Log.d(TAG, "Player2 ID: " + Integer.valueOf(R.id.player2).toString());
*/
          return true;	// return true so that SimpleCursorAdapter doesn't process bindView() on this element in its standard way
      }
    };
    
}
