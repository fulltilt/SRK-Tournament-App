package tournament.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

public class TournamentEntry extends FragmentActivity {
	private DBAdapter dbAdapter;
	private EditText tournamentName;
	private DatePicker date; 
	private RadioButton singleFormat, doubleFormat;
	private int format = 2;		// default to '2' which refers to double-elimination format

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tournament_entry);        
        dbAdapter = new DBAdapter(this);
        dbAdapter.open();
        
        tournamentName = (EditText) findViewById(R.id.editText1);
        date = (DatePicker) findViewById(R.id.datePicker);
        singleFormat = (RadioButton) findViewById(R.id.radioSingle);
        doubleFormat = (RadioButton) findViewById(R.id.radioDouble);
        singleFormat.setOnClickListener(radio_listener);
        doubleFormat.setOnClickListener(radio_listener);
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
	        	String name = tournamentName.getText().toString();
	        	if (name.length() == 0) {
	        		Toast.makeText(getBaseContext(), "Invalid Tournament Name!!", Toast.LENGTH_LONG).show();
	        		break;
	        	}
	        	
	        	String tournamentDate = (date.getMonth() + 1) + "/" + date.getDayOfMonth() + "/" + date.getYear(); 
	        	
	        	// if the row is added, insert returns a 'long' id of the row. Once this is done, go back to home
	        	long rowID = dbAdapter.insertTournament(name, tournamentDate, format);
	            if (rowID != -1) {
	            	Toast.makeText(getBaseContext(), "Tournament added successfully!", Toast.LENGTH_LONG).show();
		            intent = new Intent(this, HomeActivity.class);
		            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		            startActivity(intent);
	            }
	            
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
        }
        
        return false;
    }    

    private OnClickListener radio_listener = new OnClickListener() {
        public void onClick(View v) {
            // Perform action on clicks
            RadioButton rb = (RadioButton) v;
            if (rb.getText().equals("Single")) 
            	format = 1;
            else
            	format = 2;
        }
    };
}
