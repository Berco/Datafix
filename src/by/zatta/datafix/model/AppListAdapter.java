package by.zatta.datafix.model;

import java.util.List;

import by.zatta.datafix.R;
import by.zatta.datafix.dialog.ShowInfoDialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class AppListAdapter extends ArrayAdapter<AppEntry> {
    private final LayoutInflater mInflater;

    public AppListAdapter(Context context) {
    	
        super(context, android.R.layout.simple_list_item_2);
    	
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setData(List<AppEntry> data) {
        clear();
        if (data != null) {
            addAll(data);
        }
    }

    /**
     * Populate new items in the list.
     */
    @Override public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView == null) {
            view = mInflater.inflate(R.layout.list_item_icon_text, parent, false);
        } else {
            view = convertView;
        }
        
        //Long biggest = getItem(0).getTotalSize();
        Long biggest = AppListLoader.biggestSize;

        AppEntry item = getItem(position);
        Boolean cache = false;
        Boolean data = false;
        if (item.getCacheBool().equals("true")) cache = true;
        if (item.getDataBool().equals("true")) data = true;
        
        String total_size = ShowInfoDialog.readable(item.getTotalSize(), false);
        String cache_size = ShowInfoDialog.readable(item.getCacheSize(), false);
        int percent_total = (int)(((double)item.getTotalSize()/biggest)*98);
        
        
        ((ImageView)view.findViewById(R.id.icon)).setImageDrawable(item.getIcon());
        ((TextView)view.findViewById(R.id.text)).setText(item.getLabel());
        ((TextView)view.findViewById(R.id.text2)).setText(item.getPackName());
        ((TextView)view.findViewById(R.id.text3)).setText("Total: "+total_size+", cache: " + cache_size);
        ((CheckBox)view.findViewById(R.id.cbCache)).setChecked(cache);
        ((CheckBox)view.findViewById(R.id.cbKeepData)).setChecked(data);
        ((ProgressBar)view.findViewById(R.id.pbSpaceByApp)).setProgress(percent_total);
       
        
        return view;
    }    
    
}