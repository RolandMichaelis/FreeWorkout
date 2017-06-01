package tools;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by uwe on 19.09.13.
 */
public class BaseGameActivity extends Activity {

    private Map<String,Typeface> typefaces = new HashMap<String,Typeface>();
    private float density;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        density = getResources().getDisplayMetrics().density;
    }

    protected float scale(float v) {
        return  density*v;
    }

    protected void addTypeface(String name) {
        Typeface typeface = Typeface.createFromAsset(getAssets(),name+".ttf");
        typefaces.put(name, typeface);
    }

    protected void setTypeface(TextView v, String typefaceName) {
        Typeface t = getTypeface(typefaceName);
        if(t!=null) {
            v.setTypeface(t);
        }
    }

    protected Typeface getTypeface(String typefaceName) {
        return typefaces.get(typefaceName);
    }

    protected void hideView(int id) {
        findViewById(id).setVisibility(View.GONE);
    }
    protected void showView(int id) {
        findViewById(id).setVisibility(View.VISIBLE);
    }

    protected  void setText(int id, String text) {
        ((TextView)findViewById(id)).setText(text);
    }

    public String timeFormat(int sec) {
        // Gibt Sekunden als hh:mm:ss aus
        String s;
        int timeSeconds=sec;
        int timeMinutes=timeSeconds/60;
        timeSeconds=timeSeconds-(timeMinutes*60);
        int timeHours=timeMinutes/60;
        timeMinutes=timeMinutes-(timeHours*60);
        if(timeHours>0) s=String.valueOf(timeHours)+":"+String.format("%02d",timeMinutes)+":"+String.format("%02d",timeSeconds);
        else s=String.format("%02d",timeMinutes)+":"+String.format("%02d",timeSeconds);
        return s;
    }

    public static class ListUtils {
        public static void setDynamicHeight(ListView mListView) {
            ListAdapter mListAdapter = mListView.getAdapter();
            if (mListAdapter == null) {
                // when adapter is null
                return;
            }
            int height = 0;
            int desiredWidth = View.MeasureSpec.makeMeasureSpec(mListView.getWidth(), View.MeasureSpec.UNSPECIFIED);
            for (int i = 0; i < mListAdapter.getCount(); i++) {
                View listItem = mListAdapter.getView(i, null, mListView);
                listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
                height += listItem.getMeasuredHeight();
            }
            ViewGroup.LayoutParams params = mListView.getLayoutParams();
            params.height = height + (mListView.getDividerHeight() * (mListAdapter.getCount() - 1));
            mListView.setLayoutParams(params);
            mListView.requestLayout();
        }
    }

}
