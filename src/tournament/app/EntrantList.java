package tournament.app;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
//import android.support.v4.app.ActionBar;
//import android.support.v4.view.ActionMode;
import android.support.v4.view.Menu;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.View;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.view.MenuItem;

import com.ericharlow.DragNDrop.DragListener;
import com.ericharlow.DragNDrop.DragNDropAdapter;
import com.ericharlow.DragNDrop.DragNDropListView;
import com.ericharlow.DragNDrop.DropListener;
import com.ericharlow.DragNDrop.RemoveListener;

public class EntrantList extends FragmentActivity {
	private static final String TAG = "EntrantList";
	private static final int TOURNEY_SIZE = 16;
	private DBAdapter dbAdapter;
	private ArrayList<String> entrants;
	private String tournamentName;		// need this for the label of the tournament name
	private String tournamentID;
	private TextView tournamentLabel;
	private DragNDropListView listEntrants;
	private DragNDropAdapter DnDAdapter;
	//private static ActionMode mActionMode = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.d(TAG, "In onCreate()");
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_list);

        dbAdapter = new DBAdapter(this);
        dbAdapter.open();	// DB doesn't get created until I call 'open()' which in turn calls 'getWriteableDatabase()'

        entrants = new ArrayList<String>();
        
        tournamentLabel = (TextView) findViewById(R.id.textView1);
		listEntrants = (DragNDropListView) findViewById(R.id.android_listEntrants); 
		if (listEntrants instanceof DragNDropListView) {
			listEntrants.setDropListener(mDropListener);
			listEntrants.setRemoveListener(mRemoveListener);
			listEntrants.setDragListener(mDragListener);
		}
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
        }
        
        tournamentLabel.setText(tournamentName);
        
        // Set up the adapter
		DnDAdapter = new DragNDropAdapter(this, new int[]{R.layout.dragitem}, new int[]{R.id.TextView01}, entrants);
		        
        if (entrants.size() == 0)		// for the initial situation when there's no tournaments in the database
        {
        	entrants.add("Tap to add an entrant...");       	
        	listEntrants.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                	Intent newActivity = new Intent(view.getContext(), PlayerEntry.class);  
                	entrants.remove(0);
                	
                	// send the ArrayList of entrants to the next Activity
                	Bundle extras = new Bundle();
                	extras.putInt("index", -1);
                	extras.putString("tournamentID", tournamentID);
                	extras.putString("name", tournamentName);
                	extras.putStringArrayList("entrants", entrants);
                	newActivity.putExtras(extras);
                	
                	newActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(newActivity);
                }
            });
        } else {
	    	listEntrants.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	            public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
	            	//Cursor c = DnDAdapter.retrieveRow(rowId);
	            	Intent newActivity = new Intent(view.getContext(), PlayerEntry.class);  
	            	
	            	// send the ArrayList of entrants to the next Activity
	            	Bundle extras = new Bundle();
	            	extras.putInt("index", position);
	            	extras.putString("tournamentID", tournamentID);
	            	extras.putString("name", tournamentName);
	            	extras.putStringArrayList("entrants", entrants);
	            	newActivity.putExtras(extras);
	            	
	            	newActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	                startActivity(newActivity);
	            }
	        });
        }
           
        
/* not yet implemented in ActionBarSherlock         
        listEntrants.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            // Called when the user long-clicks on someView
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long rowId) {
                if (mActionMode != null) {
                    return false;
                }
Log.d(TAG, "past mActionMode != null in setOnLongClickListener()");
                // Start the CAB using the ActionMode.Callback defined above
                mActionMode = startActionMode(mActionModeCallback);
                parent.setSelected(true);
                return true;
			}
        });
*/
            
        listEntrants.setAdapter(DnDAdapter);
        registerForContextMenu(listEntrants); 		// handle when user long clicks an item
    }

	// context menu that pops up when user long clicks a item in the ListView
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, v, menuInfo);  
    	menu.add(0, 1, 0, "Delete Entrant");		// manually set the Item ID to '1' for delete's
        
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        int itemID = info.position;
        menu.setHeaderTitle("Do you want to delete " + entrants.get(itemID));
    }
    
    // handle when user clicks on a selection in pop-up context menu (only option in this case is to delete
    public boolean onContextItemSelected(android.view.MenuItem item) {
    	AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
    	int itemID = info.position;
    	
    	if (item.getItemId() == 1) {	// '1' refers to the delete seletion in the Context Menu
    		Toast.makeText(getBaseContext(), "Removed Player " + entrants.get(itemID), Toast.LENGTH_LONG).show();
    		entrants.remove(itemID); 
    		DnDAdapter.notifyDataSetChanged();
    	}
    	
        return true;
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.entrant_list_actionbar, menu);

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
	            intent = new Intent(this, PlayerEntry.class);
	            if (entrants.get(0).contains("Tap to add an entrant..."))
	            	entrants.remove(0);
	            
	            // send the ArrayList of entrants to the next Activity
            	Bundle extras = new Bundle();
            	extras.putInt("index", -1);
            	extras.putString("tournamentID", tournamentID);
            	extras.putString("name", tournamentName);
            	extras.putStringArrayList("entrants", entrants);
            	intent.putExtras(extras);
            	
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
	            return true;
	        case R.id.menu_shuffle:
	        	Collections.shuffle(entrants);
	            DnDAdapter.notifyDataSetChanged();
	        	return true;
	        case R.id.menu_save:
	        	/* finalize the entrants and enter players into database */
	        	//int tourneySize = (int)Math.pow(2, (int)Math.ceil(Math.log(entrants.size()) / Math.log(2)));	// get the minimum tourney size by taking the log base 2 of the total # of entrants and using that exponent on 2
	
	        	// tournament must have at least 4 entrants
	        	if (entrants.size() < 4) {
	        		Toast.makeText(getBaseContext(), "Tournament must have at least 4 entrants!!", Toast.LENGTH_LONG).show();
	        		break;
	        	}
	        		
	        	// calculate the # of byes to add to entrant list to fill the tournament roster to 16 if needed
	        	int byes = TOURNEY_SIZE - entrants.size(); 	        
	        	while (byes != 0) {
	        		entrants.add(null);
	        		byes--;
	        	}
	        	
	        	// attempt to add the entrants into the Player table
	        	for (String entrant : entrants)
	        	{		
	        		try {
	        			dbAdapter.insertPlayer(entrant, "");
	        		}
	        		catch (Exception e) { Log.d(TAG, entrant + " already in database!"); }
	        	}
	        	
	        	// using the list order as the order of seeds, setup the matches accordingly
	        	try {
	        		dbAdapter.insertMatch("WA", tournamentID, entrants.get(0), entrants.get(15));
	        		dbAdapter.insertMatch("WB", tournamentID, entrants.get(7), entrants.get(8));
	        		dbAdapter.insertMatch("WC", tournamentID, entrants.get(4), entrants.get(11));
	        		dbAdapter.insertMatch("WD", tournamentID, entrants.get(3), entrants.get(12));
	        		dbAdapter.insertMatch("WE", tournamentID, entrants.get(5), entrants.get(10));
	        		dbAdapter.insertMatch("WF", tournamentID, entrants.get(2), entrants.get(13));
	        		dbAdapter.insertMatch("WG", tournamentID, entrants.get(6), entrants.get(9));
	        		dbAdapter.insertMatch("WH", tournamentID, entrants.get(1), entrants.get(14));
	        		dbAdapter.insertMatch("WI", tournamentID, null, null);
	        		dbAdapter.insertMatch("WJ", tournamentID, null, null);
	        		dbAdapter.insertMatch("WK", tournamentID, null, null);
	        		dbAdapter.insertMatch("WL", tournamentID, null, null);
	        		dbAdapter.insertMatch("WM", tournamentID, null, null);
	        		dbAdapter.insertMatch("WN", tournamentID, null, null);
	        		dbAdapter.insertMatch("WO", tournamentID, null, null);
	        		
        		}
        		catch (Exception e) { Log.d(TAG, "Error while attempting to insert a match!"); e.printStackTrace(); }
	        	
	        	return true;
	        default:
	        	return false;	// this is here in case we use the pop-up menu 
	        	//    return super.onOptionsItemSelected(item);
        }
        
        return false;
    }
    
    public void onDestroy() {
        super.onDestroy();
     
        // close the database
        dbAdapter.close();
    } 
    
	private DropListener mDropListener = new DropListener() {
	    public void onDrop(int from, int to) {
	    	DnDAdapter.onDrop(from, to);
	    	listEntrants.invalidateViews();
	    }
	};
	    
	private RemoveListener mRemoveListener = new RemoveListener() {
	    public void onRemove(int which) {
	    	DnDAdapter.onRemove(which);
	    	listEntrants.invalidateViews();	 
	    }
	};
	    
	private DragListener mDragListener = new DragListener() {
	  	int backgroundColor = 0xe0103010;
	   	int defaultBackgroundColor;
	    	
		public void onDrag(int x, int y, ListView listView) {  }

		public void onStartDrag(View itemView) {
			itemView.setVisibility(View.INVISIBLE);
			defaultBackgroundColor = itemView.getDrawingCacheBackgroundColor();
			itemView.setBackgroundColor(backgroundColor);
			ImageView iv = (ImageView)itemView.findViewById(R.id.ImageView01);
			if (iv != null) iv.setVisibility(View.INVISIBLE);
		}

		public void onStopDrag(View itemView) {
			if (itemView != null) {
				itemView.setVisibility(View.VISIBLE);
				itemView.setBackgroundColor(defaultBackgroundColor);
				ImageView iv = (ImageView)itemView.findViewById(R.id.ImageView01);
				if (iv != null) 
					iv.setVisibility(View.VISIBLE);
			}
		}
	};    

/* not yet implemented in ActionBarSherlock
	// the following anonymous inner class is for a contextual action bar
	static final private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
	    // Called when the action mode is created; startActionMode() was called
	    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
Log.d(TAG, "in onCreateActionMode()");
	        // Inflate a menu resource providing context menu items
	        MenuInflater inflater = mode.getMenuInflater();
	        inflater.inflate(R.menu.entrant_contextual_actionbar, menu);
	        return true;
	    }

	    // Called each time the action mode is shown. Always called after onCreateActionMode, but
	    // may be called multiple times if the mode is invalidated.
	    @Override
	    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
	        return false; // Return false if nothing is done
	    }

	    // Called when the user selects a contextual menu item
	    @Override
	    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
	        switch (item.getItemId()) {
	            case R.id.ic_menu_edit:
	                //shareCurrentItem();
	                mode.finish(); // Action picked, so close the CAB
	                return true;
	            case R.id.ic_menu_delete:
	            	return true;
	            default:
	                return false;
	        }
	    }

	    // Called when the user exits the action mode
	    @Override
	    public void onDestroyActionMode(ActionMode mode) {
	        mActionMode = null;
	    }
	};
*/	
}
