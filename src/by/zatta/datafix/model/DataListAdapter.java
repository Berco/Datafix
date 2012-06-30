package by.zatta.datafix.model;

import java.util.List;

import by.zatta.datafix.R;
import by.zatta.datafix.dialog.ShowInfoDialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DataListAdapter extends ArrayAdapter<DataEntry> {
    private final LayoutInflater mInflater;
    

    public DataListAdapter(Context context) {
    	
        super(context, android.R.layout.simple_list_item_2);
    	
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setData(List<DataEntry> data) {
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
            view = mInflater.inflate(R.layout.list_item_icon_icon, parent, false);
        } else {
            view = convertView;
        }
        
       

        DataEntry item = getItem(position);
        
        int percent_yaffs = (int)(((double)item.getYaffsSize()/(1803550720/172))*98);
        int percent_data = (int)(((double)item.getDataSize()/(1803550720/172))*98);
        
        if (item.getType().contentEquals("dir")){
        	((ImageView)view.findViewById(R.id.iconLeft)).setImageResource(R.drawable.folder);
        	((ImageView)view.findViewById(R.id.iconRight)).setImageResource(R.drawable.folder);
        }else{
        	((ImageView)view.findViewById(R.id.iconLeft)).setImageResource(R.drawable.notes);
            ((ImageView)view.findViewById(R.id.iconRight)).setImageResource(R.drawable.notes);
        }
        ((TextView)view.findViewById(R.id.tvDatasFolderName)).setText(item.getDataName());
        ((TextView)view.findViewById(R.id.tvFolderSizeYaff)).setText(ShowInfoDialog.readable(item.getYaffsSize(), false));
        ((TextView)view.findViewById(R.id.tvFolderSizeData)).setText(ShowInfoDialog.readable(item.getDataSize(), false));
        ((ProgressBar)view.findViewById(R.id.pbYaffsProgressBar)).setProgress(percent_yaffs);
        ((ProgressBar)view.findViewById(R.id.pbDataProgressBar)).setProgress(percent_data);
       
        
        return view;
    }    
}