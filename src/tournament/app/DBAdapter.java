package tournament.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {
	private static final String TAG = "DBAdapter";
    public static final String KEY_ROWID = "_id";
    public static final String KEY_PLAYER_NAME = "name";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_TOURNAMENT_ID = "tournamentID";
    public static final String KEY_TOURNAMENT_NAME = "name";
    public static final String KEY_DATE = "date";
    public static final String KEY_FORMAT = "format";
    public static final String KEY_PLAYER1 = "player1";
    public static final String KEY_PLAYER2 = "player2";
    public static final String KEY_WINNER = "winner";
    public static final String KEY_BRACKET_LETTER = "bracketLetter";
    
    static final String DATABASE_NAME = "TournamentDB";
    public static final String PLAYER_TABLE = "player";
    public static final String TOURNAMENT_TABLE = "tournament";
    public static final String MATCH_TABLE = "match";
    static final int DATABASE_VERSION = 2;

    private static final String PLAYER_TABLE_CREATE =
        "create table player (_id integer primary key autoincrement, "
        + "name unique text not null, phone text)";
    
    private static final String TOURNAMENT_TABLE_CREATE = 
        "create table tournament (_id integer primary key autoincrement, "
    	+ "name unique text not null, date text not null, format integer)";

    private static final String MATCH_TABLE_CREATE = 
            "create table match (_id integer primary key autoincrement, "
        	+ "tournamentID integer, bracketLetter text, subBracketID integer, player1 text, player2 text, winner text)";
    
    private final Context context;    
    DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public DBAdapter(Context ctx) 
    {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }
        
    static class DatabaseHelper extends SQLiteOpenHelper 
    {	
        DatabaseHelper(Context context) 
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) 
        {
        	Log.d(TAG, "in onCreate()");
        	try {
        		db.execSQL(PLAYER_TABLE_CREATE);
        		db.execSQL(TOURNAMENT_TABLE_CREATE);
        		db.execSQL(MATCH_TABLE_CREATE);
        	} catch (SQLException e) {
        		e.printStackTrace();
        	}
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
        {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS tournament");
            db.execSQL("DROP TABLE IF EXISTS player");
            onCreate(db);
        }
    }    

    //---opens the database---
    public DBAdapter open() throws SQLException 
    {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    //---closes the database---    
    public void close() 
    {
        DBHelper.close();
    }
    
    //---insert a contact into the database---
    public long insertPlayer(String name, String phone) 
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_PLAYER_NAME, name);
        initialValues.put(KEY_PHONE, phone);
        return db.insert(PLAYER_TABLE, null, initialValues);
    }
    
    //---deletes a particular contact---
    public boolean deletePlayer(long rowId) 
    {
        return db.delete(PLAYER_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    //---retrieves all the players---
    public Cursor getAllPlayers() 
    {
        return db.query(PLAYER_TABLE, new String[] {KEY_ROWID, KEY_PLAYER_NAME,
                KEY_PHONE}, null, null, null, null, null);
    }

    //---retrieves a particular player---
    public Cursor getPlayer(long rowId) throws SQLException 
    {
        Cursor mCursor =
                db.query(true, PLAYER_TABLE, new String[] {KEY_ROWID,
                KEY_PLAYER_NAME, KEY_PHONE}, KEY_ROWID + "=" + rowId, null,
                null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    //---updates a contact---
    public boolean updatePlayer(long rowId, String name, String phone) 
    {
        ContentValues args = new ContentValues();
        args.put(KEY_PLAYER_NAME, name);
        args.put(KEY_PHONE, phone);
        return db.update(PLAYER_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }

    // retrieves all the tournaments. Used to display all tournaments on Home screen
    public Cursor getAllTournaments() 
    {
        return db.query(TOURNAMENT_TABLE, null, null, null, null, null, KEY_DATE + " DESC");
    }

    // returns player1 and player2 of a given tournament bracket
    public Cursor getCurrentTournamentEntrants(String tourneyID) 
    {    	
        return db.query(MATCH_TABLE, new String[] {KEY_BRACKET_LETTER, KEY_PLAYER1,
        		KEY_PLAYER2}, "tournamentID = " + tourneyID, null, null, null, null, null);	
    }
    
    //---insert a contact into the database---
    public long insertTournament(String name, String date, int format) 
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TOURNAMENT_NAME, name);
        initialValues.put(KEY_DATE, date);
        initialValues.put(KEY_FORMAT, format);
        return db.insert(TOURNAMENT_TABLE, null, initialValues);
    }  
    
    // get all matchups for a given tournament
    public Cursor getBrackets(String tourneyID, String bracketWHERE) 
    {    	
        return db.query(MATCH_TABLE, new String[] {KEY_ROWID, KEY_BRACKET_LETTER, KEY_PLAYER1,
        		KEY_PLAYER2}, "tournamentID = " + tourneyID + " " + bracketWHERE, null, null, null, null, null);	
    }    

    // insert a match into Match table
    public long insertMatch(String tournamentID, String bracketLetter, String player1, String player2) 
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TOURNAMENT_ID, Integer.parseInt(tournamentID));
        initialValues.put(KEY_BRACKET_LETTER, bracketLetter);
        initialValues.put(KEY_PLAYER1, player1);
        initialValues.put(KEY_PLAYER2, player2);
        return db.insert(MATCH_TABLE, null, initialValues);
    }  

    // update a match in Match table
    public long updateMatch(String tournamentID, String bracketLetter, String winner, String player) 
    {
        ContentValues value = new ContentValues();

        if (player.equals("player1"))
        	value.put(KEY_PLAYER1, winner);
        else
        	value.put(KEY_PLAYER2, winner);
        return db.update(MATCH_TABLE, value, "tournamentID = " + tournamentID + " AND bracketLetter = '"  + bracketLetter + "'", null);
    }  
    
    // get the rows from the Match table where the 'winner' column is null
    public Cursor getUnfinishedMatches(String tournamentID) { 
    	return db.query(MATCH_TABLE, new String[] {KEY_ROWID}, "tournamentID = " + tournamentID + " AND winner IS NULL",
        		null, null, null, null, null);	
    }
    
    // update the winner of a match
    public long updateWinner(String tournamentID, String player1, String player2, String winner)
    {
        ContentValues value = new ContentValues();
        value.put(KEY_WINNER, winner);
        return db.update(MATCH_TABLE, value, "tournamentID = " + tournamentID + " AND player1 = '" + player1 + "' AND player2 = '" + player2 + "'", null);
    }  
    
    // get the winner of a match
    public Cursor getWinner(String tournamentID, String player, String bracket) {
    	return db.query(MATCH_TABLE, new String[] {KEY_WINNER}, "tournamentID = " + tournamentID + " AND winner = '" + player + "' AND bracketLetter = '" + bracket + "'", 
        		null, null, null, null, null);
    }
}
