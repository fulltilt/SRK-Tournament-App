package tournament.app;

import java.util.ArrayList;
import java.util.Arrays;

import com.viewpagerindicator.TitlePageIndicator;
import com.viewpagerindicator.TitleProvider;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
//import android.support.v4.app.ActionBar;
import android.support.v4.view.Menu;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewGroup;
import android.util.Log;
import android.widget.AdapterView;

import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.view.MenuItem;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;

public class Bracket extends FragmentActivity  {
	private static final String TAG = "Bracket";
	private DBAdapter dbAdapter;
	private Cursor cursor;
	private SimpleCursorAdapter adapter;
	private ListView listWinnersBracket;
	static final String[] FROM = { DBAdapter.KEY_PLAYER1, DBAdapter.KEY_PLAYER2 };  
	static final int[] TO = { R.id.player1, R.id.player2 };
	private String tournamentID;
	private ArrayList<String> entrantsList = new ArrayList<String>(16);				// tournament starts with 16 players
	private ArrayList<String> winnersQuarterFinals = new ArrayList<String>(Arrays.asList("TBD", "TBD", "TBD", "TBD", "TBD", "TBD", "TBD", "TBD"));		// the quarterfinals have 8 players in Winners bracket
	private ArrayList<String> winnersSemiFinals = new ArrayList<String>(Arrays.asList("TBD", "TBD", "TBD", "TBD"));		// the semifinals have 4 players in Winners bracket
	private ArrayList<String> winnersFinals = new ArrayList<String>(Arrays.asList("TBD", "TBD"));		// the finals have 2 players in Winners bracket
	private ArrayList<String> losersList = new ArrayList<String>();
	private String tempP1, tempP2, currentBracketLetter;
	private ViewPagerAdapter viewPagerAdapter = null;
	private ViewPager viewPager = null;
    
	private static String[] tabTitles = new String[] { "Round 1", "Quarterfinals", "Semifinals", "Finals"};
	private String[] round1BracketLetters = new String[] { "WA", "WB", "WC", "WD", "WE", "WF", "WG", "WH" }; 
	private String[] quarterFinalBracketLetters = new String[] { "WI", "WJ", "WK", "WL" };
	private String[] semiFinalBracketLetters = new String[] { "WM", "WN" };  
    
    /** Called when the activity is first created. */
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
        int row = 0;
        while (cursor.isAfterLast() == false)
        {
        	if (row++ < 8) {
        		entrantsList.add(cursor.getString(1));
        		entrantsList.add(cursor.getString(2));
        	}
        	else if (row++ < 12) {
        		winnersQuarterFinals.add(cursor.getString(1));
        		winnersQuarterFinals.add(cursor.getString(2));
        	}
        	else if (row++ < 14) {
        		winnersSemiFinals.add(cursor.getString(1));
        		winnersSemiFinals.add(cursor.getString(2));
        	}
        	else {
        		winnersFinals.add(cursor.getString(1));
        		winnersFinals.add(cursor.getString(2));
        	}
        	cursor.moveToNext();
        }
        Log.d(TAG, entrantsList.size() + "");
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

        // handle the tabs and the horizontal scrolling
		viewPagerAdapter = new ViewPagerAdapter(getBaseContext());
		viewPager = (ViewPager)findViewById(R.id.viewPager);
		TitlePageIndicator indicator = (TitlePageIndicator)findViewById(R.id.indicator);
		viewPager.setAdapter(viewPagerAdapter);
		viewPager.setCurrentItem(0);
		indicator.setViewPager(viewPager);
    }
    
	protected void onResume() {
		Log.d(TAG, "in onResume()");
        super.onResume();
    }
 
	// context menu that pops up when user clicks a item in the ListView
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	Log.d(TAG, "onCreateContextMenu()");
    	super.onCreateContextMenu(menu, v, menuInfo);
    	
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
    	int bracketNum = info.position * 2;		// position refers to the position on the ListView. Multiplying by 2 since there are 2 entrants per position

    	if (viewPager.getCurrentItem() == 0) {
    		tempP1 = entrantsList.get(bracketNum);
        	tempP2 = entrantsList.get(bracketNum + 1);
        	currentBracketLetter = round1BracketLetters[info.position];
    	} else if (viewPager.getCurrentItem() == 1) { 
    		tempP1 = winnersQuarterFinals.get(bracketNum);
        	tempP2 = winnersQuarterFinals.get(bracketNum + 1);
        	currentBracketLetter = quarterFinalBracketLetters[info.position];
    	} else if (viewPager.getCurrentItem() == 2) { 
    		tempP1 = winnersSemiFinals.get(bracketNum);
        	tempP2 = winnersSemiFinals.get(bracketNum + 1);
        	currentBracketLetter = semiFinalBracketLetters[info.position];
    	} else if (viewPager.getCurrentItem() == 3) { 
    		tempP1 = winnersFinals.get(bracketNum);
        	tempP2 = winnersFinals.get(bracketNum + 1);
        	currentBracketLetter = "WO";
    	}
    	
// snippet that makes sure that if the bracket has an unfilled entry to not have a popup box. Eventually, we'll take out the Toast when everything is working
    	if (tempP1.equals("TBD") || tempP2.equals("TBD")) {
    		Toast.makeText(getBaseContext(), "No display since the bracket isn't finalized", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	
    	menu.setHeaderTitle("Pick the winner of the match");
    	menu.add(1, 0, 0, tempP1);
    	menu.add(1, 1, 0, tempP2);
    }
    
    // handle when user clicks on a bracket context menu pops up that allows user to select a winner of a match
    public boolean onContextItemSelected(android.view.MenuItem item) {
    	String currentWinner = null;
    	if (item.getItemId() == 0) {	// '0' refers to the 1st player in the Context menu
    		currentWinner = tempP1;
    		dbAdapter.updateWinner(tournamentID, tempP1, tempP2, currentWinner);
    	}
    	else if (item.getItemId() == 1) {	// '1' refers to the 2nd player in the Context menu
    		currentWinner = tempP2;
    		dbAdapter.updateWinner(tournamentID, tempP1, tempP2, currentWinner);
    	}
    	
    	// determine which bracket the winner goes to
    	if (currentBracketLetter == "WA") {
    		dbAdapter.updateMatch(tournamentID, "WI", currentWinner, "player1"); //viewPagerAdapter.instantiateItem(pager, position);
    		winnersQuarterFinals.set(0, currentWinner);
    		//viewPagerAdapter.instantiateItem(viewPager, 1);
    	} else if (currentBracketLetter == "WB") { 
    		dbAdapter.updateMatch(tournamentID, "WI", currentWinner, "player2");
    		winnersQuarterFinals.set(1, currentWinner);
    	} else if (currentBracketLetter == "WC") {
    		dbAdapter.updateMatch(tournamentID, "WJ", currentWinner, "player1");
    		winnersQuarterFinals.set(2, currentWinner);
    	} else if (currentBracketLetter == "WD") {
    		dbAdapter.updateMatch(tournamentID, "WJ", currentWinner, "player2");
    		winnersQuarterFinals.set(3, currentWinner);
    	} else if (currentBracketLetter == "WE") {
    		dbAdapter.updateMatch(tournamentID, "WK", currentWinner, "player1");
    		winnersQuarterFinals.set(4, currentWinner);
    	} else if (currentBracketLetter == "WF") {
    		dbAdapter.updateMatch(tournamentID, "WK", currentWinner, "player2");
    		winnersQuarterFinals.set(5, currentWinner);
    	} else if (currentBracketLetter == "WG") {
    		dbAdapter.updateMatch(tournamentID, "WL", currentWinner, "player1");
    		winnersQuarterFinals.set(6, currentWinner);
    	} else if (currentBracketLetter == "WH") {
    		dbAdapter.updateMatch(tournamentID, "WL", currentWinner, "player2");
    		winnersQuarterFinals.set(7, currentWinner);
    	} else if (currentBracketLetter == "WI") {
    		dbAdapter.updateMatch(tournamentID, "WM", currentWinner, "player1");
    		winnersSemiFinals.set(0, currentWinner);
    	} else if (currentBracketLetter == "WJ") {
    		dbAdapter.updateMatch(tournamentID, "WM", currentWinner, "player2");
    		winnersSemiFinals.set(1, currentWinner);
    	} else if (currentBracketLetter == "WK") {
    		dbAdapter.updateMatch(tournamentID, "WN", currentWinner, "player1");
    		winnersSemiFinals.set(2, currentWinner);
    	} else if (currentBracketLetter == "WL") {
    		dbAdapter.updateMatch(tournamentID, "WN", currentWinner, "player2");
    		winnersSemiFinals.set(3, currentWinner);
    	} else if (currentBracketLetter == "WM") {
    		dbAdapter.updateMatch(tournamentID, "WO", currentWinner, "player1");
    		winnersFinals.set(0, currentWinner);
    	} else if (currentBracketLetter == "WN") {
    		dbAdapter.updateMatch(tournamentID, "WO", currentWinner, "player2");
    		winnersFinals.set(1, currentWinner);
    	} 
    	
    	adapter.notifyDataSetChanged();	// notify the adapter that the ListView should be refreshed
    	viewPagerAdapter.notifyDataSetChanged();
    	
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
	        default:
	            return super.onOptionsItemSelected(item);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        dbAdapter.close();	// close the database
    }    

    // View binder constant to inject business logic for brackets
    final ViewBinder VIEW_BINDER = new ViewBinder() { 

  	  // called for each data element that needs to be bound to a particular view
      public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
    	  //Log.d(TAG, "in ViewBinder");
    		  
    	  // handle the top half of a bracket that deals with Player 1
    	  if (view.getId() == R.id.player1) {
    		  String player1 = cursor.getString(columnIndex);
    		  if (player1 == null) {
    			  ((TextView)view).setText("-TBD-");
    			  ((TextView)view).setBackgroundResource(R.drawable.top_shape);
    		  } else {
	    		  ((TextView)view).setText(player1);
	    		  
	    		  String bracketLetter = cursor.getString(1);	// column '1' of cursor represents the bracket letter
	    		  Cursor c = dbAdapter.getWinner(tournamentID, cursor.getString(columnIndex), bracketLetter);
	    		  startManagingCursor(c);
	    		  if (c.getCount() == 1)
	    			  ((TextView)view).setBackgroundResource(R.drawable.top_shape_winner);
	    		  else
	    			  ((TextView)view).setBackgroundResource(R.drawable.top_shape);
    		  }
    	  }
    		  
    	  // handle the bottom half of a bracket that deals with Player 2
    	  if (view.getId() == R.id.player2) {
    		  String player2 = cursor.getString(columnIndex);
    		  if (player2 == null) {
    			  ((TextView)view).setText("-TBD-");
    			  ((TextView)view).setBackgroundResource(R.drawable.bottom_shape);
    		  } else {
    			  ((TextView)view).setText(player2);
    			  
    			  String bracketLetter = cursor.getString(1);
    			  Cursor c = dbAdapter.getWinner(tournamentID, cursor.getString(columnIndex), bracketLetter);
    			  startManagingCursor(c);
	    		  if (c.getCount() == 1)
	    			  ((TextView)view).setBackgroundResource(R.drawable.bottom_shape_winner);
	    		  else
	    			  ((TextView)view).setBackgroundResource(R.drawable.bottom_shape);
    		  }
    	  }
    	  
          return true;	// return true so that SimpleCursorAdapter doesn't process bindView() on this element in its standard way
      }
    };

    private class ViewPagerAdapter extends PagerAdapter implements TitleProvider {
    	private final Context context;
    	
        public ViewPagerAdapter(Context context) { this.context = context; }      
        public int getCount() { return tabTitles.length; }

        public Object instantiateItem(ViewGroup pager, int position) {
            LayoutInflater inflater = (LayoutInflater) pager.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = inflater.inflate(R.layout.bracket_rounds, null);
            switch (position) {
            case 0:
                    cursor = dbAdapter.getBrackets(tournamentID, "AND bracketLetter >= 'WA' AND bracketLetter <= 'WH'");
                    break;
            case 1:
            	    cursor = dbAdapter.getBrackets(tournamentID, "AND bracketLetter >= 'WI' AND bracketLetter <= 'WL'");
            	    break;
            case 2:
            		cursor = dbAdapter.getBrackets(tournamentID, "AND bracketLetter >= 'WM' AND bracketLetter <= 'WN'");
            		break;
            case 3:
            		cursor = dbAdapter.getBrackets(tournamentID, "AND bracketLetter = 'WO'");
                    break;
            }
            startManagingCursor(cursor);
            
            listWinnersBracket = (ListView) view.findViewById(R.id.winnersBracket);
            
            // Set up the adapter
    	    adapter = new SimpleCursorAdapter(context, R.layout.bracket_row, cursor, FROM, TO, 0);   
    	    adapter.setViewBinder(VIEW_BINDER);	// attach custom ViewBinder instance to vanilla adapter so brackets are displayed correctly
    	    listWinnersBracket.setAdapter(adapter); 
    	    
    	    // handler when user clicks on a ListView item (in this case, have Context menu pop up)
    	    listWinnersBracket.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
Log.d(TAG, "onItemClick()");
                	registerForContextMenu(listWinnersBracket); 
                	listWinnersBracket.setLongClickable(false);  // undo setting of this flag in registerForContextMenu
                    openContextMenu(view);
                }
            });
    	    
            ((ViewPager) pager).addView(view, 0);
            //viewPager.setAdapter(viewPagerAdapter);
            //finishUpdate(pager);
            
            return view;
        }

        public void destroyItem(View arg0, int arg1, Object arg2) { ((ViewPager) arg0).removeView((View) arg2); }
        public void finishUpdate(ViewGroup arg0) { }
        public boolean isViewFromObject(View arg0, Object arg1) { return arg0 == ((View) arg1); }
        public void restoreState(Parcelable arg0, ClassLoader arg1) { }
        public Parcelable saveState() { return null; }
        public void startUpdate(View arg0) { }

     // This is the only methods from TitleProvider and should return the title of page at the specified position. We return the relevant title from the titles array.
        public String getTitle(int position) { return tabTitles[position]; }
    }    
}
