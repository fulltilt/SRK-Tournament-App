package tournament.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.util.Log;

import com.viewpagerindicator.TitleProvider;
 
public class ViewPagerAdapter2 extends PagerAdapter implements TitleProvider {
	private static final String TAG = "ViewPagerAdapter";
	private DBAdapter dbAdapter;
	
    private static String[] titles = new String[]
    {
        "Round 1",
        "Quarterfinals",
        "Semifinals",
        "Finals"
    };
    private final Context context;
 
    public ViewPagerAdapter2( Context context )
    {
        this.context = context;
    }
 
    // This is the only methods from TitleProvider and should return the title of page at the specified position. We return the relevant title from the titles array.
    public String getTitle(int position) { return titles[position]; }
 
    // This returns the number of pages. In this case the size of our titles array.
    public int getCount()
    {
        return titles.length;
    }
 
    // This creates the view for a given position.
    public Object instantiateItem(ViewGroup pager, int position)
    {    	
    	ListView v = new ListView(context);
    	
    	Toast.makeText(context, "Position " + position, Toast.LENGTH_SHORT).show();
/*        String[] from = new String[] { "str" };
        int[] to = new int[] { android.R.id.text1 };
        List<Map<String, String>> items = new ArrayList<Map<String, String>>();
        
        for (int i = 0; i < 20; i++) {
            Map<String, String> map = new HashMap<String, String>();
            map.put( "str", String.format("Item %d", i + 1));
            items.add(map);
        }
        
        SimpleAdapter adapter = new SimpleAdapter(context, items, android.R.layout.simple_list_item_1, from, to);
        v.setAdapter(adapter); */
        ((ViewPager)pager).addView( v, 0 ); 
        return v;
    }
 
    // This destroys a previously instantiated view. We simply remove it from the parent, and it will be garbage collected.
    public void destroyItem( ViewGroup pager, int position, Object view )
    {
        ((ViewPager)pager).removeView((ListView)view);
    }
 
    @Override
    public boolean isViewFromObject( View view, Object object )
    {
        return view.equals( object );
    }
 
    @Override
    public void finishUpdate( View view ) {}
 
    @Override
    public void restoreState( Parcelable p, ClassLoader c ) {}
 
    @Override
    public Parcelable saveState() {
        return null;
    }
 
    @Override
    public void startUpdate( View view ) {}
}