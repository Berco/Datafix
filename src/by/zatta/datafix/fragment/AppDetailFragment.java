package by.zatta.datafix.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import by.zatta.datafix.BaseActivity;
import by.zatta.datafix.R;
import by.zatta.datafix.assist.ShellProvider;
import by.zatta.datafix.assist.TouchInterceptor;
import by.zatta.datafix.dialog.WipeDialog;
import by.zatta.datafix.model.AppEntry;
import by.zatta.datafix.model.DataEntry;
import by.zatta.datafix.model.DataListAdapter;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class AppDetailFragment extends ListFragment implements OnClickListener{
	private ProgressBar mProgressView;
	private LinearLayout mBarHolder;
	private Button mBtnClearCache;
	private Button mBtnClearData;
	private TouchInterceptor mList;
	public 	List<DataEntry> dataList;
	private AppEntry mHandledApp;
	private ImageView mImage;
	private TextView mTvAppName;
	private TextView mTvPackName;
	private DataListAdapter mAdapter;		
	private TouchInterceptor.TickListener mTickListener =
			    new TouchInterceptor.TickListener() {
			        @Override
					public void ticked(int item, int tick) {
			        	
			        	DataEntry dataItem = dataList.get(item);
			        	if (BaseActivity.DEBUG)
			        	System.out.println("Ticklisten item: "+dataItem.getDataName() + " box: " + tick);
			            
			            switch (tick){
			            case 2:
			            	//if (app.getCacheBool().equals("true")) app.setCacheBool("false");
			            	//else app.setCacheBool("true");
			               	break;
			            case 1:
			            	//if (app.getDataBool().equals("true")) app.setDataBool("false");
			            	//else app.setDataBool("true");
			            	break;
			            }
			            mAdapter.clear();
			            mAdapter.setData(dataList);
		            }
			    };
	
	
	public AppDetailFragment(AppEntry app) {
		mHandledApp = app;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) { 
		View view = inflater.inflate(R.layout.appdetail_layout, null);
		mProgressView = (ProgressBar) view.findViewById(R.id.pbAppDetails);
		mBarHolder = (LinearLayout) view.findViewById(R.id.llAppDetails);
		mBtnClearCache = (Button) view.findViewById(R.id.btnClearCaches);
		mBtnClearData = (Button) view.findViewById(R.id.btnClearAllData);
		mBtnClearCache.setOnClickListener(this);
		mBtnClearData.setOnClickListener(this);
		
		mImage = (ImageView) view.findViewById(R.id.ivAppDetailIcon);
		mTvAppName = (TextView) view.findViewById(R.id.tvAppName);
		mTvPackName = (TextView) view.findViewById(R.id.tvPackageName);
			
		return view;
     }

	@Override public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			toggleLoading(true);
				mList = (TouchInterceptor) getListView();
				mList.setTickListener(mTickListener);
				mList.setDivider(null);
				registerForContextMenu(mList);
				mImage.setImageDrawable(mHandledApp.getIcon());
				mTvAppName.setText(mHandledApp.getLabel());
				mTvPackName.setText(mHandledApp.getPackName());
												
				dataList= loadData();
				
				setHasOptionsMenu(true);
				if ( mAdapter == null){
					mAdapter = new DataListAdapter(getActivity());
					setListAdapter(mAdapter);
									
					
				}
				toggleLoading(false);
				mAdapter.setData(dataList);
	}
	
	private void toggleLoading(boolean show) {
    	mBarHolder.setVisibility(show ? View.VISIBLE : View.GONE);
    	mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
    	mBarHolder.setGravity(show ? Gravity.CENTER : Gravity.TOP);    	
    }
	
	public void upDateDetails(){
		dataList = loadData();
		mAdapter.setData(dataList);
	}
	
	private List<DataEntry> loadData(){
		try {
			SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
			Boolean fixed = getPrefs.getBoolean("fixed", false);
			
			String[] total_yaffs = ShellProvider.INSTANCE.getCommandOutput("du -s /datadata/"+mHandledApp.getPackName()+"/*|sort | awk -F \"/\" '{ print $4, $1 }'").split(" ");
			String[] total_data = {"0", "no datafix yet" } ;
			if (fixed)
				total_data = ShellProvider.INSTANCE.getCommandOutput("du -s /data/data/"+mHandledApp.getPackName()+"/*|sort | awk -F \"/\" '{ print $5, $1 }'").split(" ");
			
			List<DataEntry> entries;
			
			//Little buggy. Might need to rethink this one
			
			if ((total_yaffs[0].contains("du:") && !total_data[0].contains("du:")) || total_data.length >= total_yaffs.length){
				entries = new ArrayList<DataEntry>((total_data.length)/2);
			}else{
				entries = new ArrayList<DataEntry>((total_yaffs.length)/2);
			}
			
			if (BaseActivity.DEBUG)
				System.out.println("lengte yaffs: " + total_yaffs.length + "en data: " + total_data.length);
			
			
			//if the app fully resides in /data/data. Prefents nullpointers
			if (total_yaffs[0].contains("du:") && !total_data[0].contains("du:")){
				if (BaseActivity.DEBUG)
				System.out.println("Yaffs no such file");
	        	for (int i=0; i<total_data.length; i=i+2) {
	                String name = total_data[i];
	                String dataSize = total_data[i+1];
	                String yaffsSize = "0";
	                String type = "file";
	                	                
	                File data = new File("/data/data/"+mHandledApp.getPackName()+"/"+name);
	                File yaffs = new File("/datadata/"+mHandledApp.getPackName()+"/"+name);
	                if ((data.exists() && data.isDirectory()) || (yaffs.exists() && yaffs.isDirectory()))
	                	type = "dir";
	                
	                DataEntry entry = new DataEntry(name, dataSize.trim(), yaffsSize.trim(), type);
	                entries.add(entry);
	            }
	        // if the app fully resides in /datadata. Prevents nullpointers
			}else if ((total_data[0].contains("du:") || total_data[1].contains("no datafix yet")) && !total_yaffs[0].contains("du:")){
				if (BaseActivity.DEBUG)
				System.out.println("Data no such file");
	        	for (int i=0; i<total_yaffs.length; i=i+2) {
	        		String name = total_yaffs[i];
	        		String yaffsSize = total_yaffs[i+1];
	        		String dataSize = "0";
	        		String type = "file";
	        			                
	                File data = new File("/data/data/"+mHandledApp.getPackName()+"/"+name);
	                File yaffs = new File("/datadata/"+mHandledApp.getPackName()+"/"+name);
	                if ((data.exists() && data.isDirectory()) || (yaffs.exists() && yaffs.isDirectory()))
	                	type = "dir";
	                
	                DataEntry entry = new DataEntry(name, dataSize.trim(), yaffsSize.trim(), type);
	                entries.add(entry);
	            }
	        //if there are more items in /data/data than on /datadata.
			}else if (total_data.length >= total_yaffs.length){
				for (int i=0; i<total_data.length; i=i+2) {
					String name = total_data[i];
					String dataSize = total_data[i+1];
					String yaffsSize = "0";
					String type = "file";
					
	                for(int b = 0; b < total_yaffs.length ; b=b+2){
	    	    	    if (total_yaffs[b].equals(name)){
	                	yaffsSize = total_yaffs[b+1];
	    	    	    }
	                }
	                
	                File data = new File("/data/data/"+mHandledApp.getPackName()+"/"+name);
	                File yaffs = new File("/datadata/"+mHandledApp.getPackName()+"/"+name);
	                if ((data.exists() && data.isDirectory()) || (yaffs.exists() && yaffs.isDirectory()))
	                	type = "dir";
	                
	                DataEntry entry = new DataEntry(name, dataSize.trim(), yaffsSize.trim(), type);
	                entries.add(entry);
	            }
			//if there are more items in /datadata than on /data/data.
			}else if (total_data.length < total_yaffs.length){
				for (int i=0; i<total_yaffs.length; i=i+2) {
					String name = total_yaffs[i];
					String yaffsSize = total_yaffs[i+1];
					String dataSize = "0";
					String type = "file";
					
					for(int b = 0; b < total_data.length ; b=b+2){
	    	    	    if (total_data[b].equals(name)){
	                	dataSize = total_data[b+1];
	    	    	    }
	                }
					
					File data = new File("/data/data/"+mHandledApp.getPackName()+"/"+name);
	                File yaffs = new File("/datadata/"+mHandledApp.getPackName()+"/"+name);
	                if ((data.exists() && data.isDirectory()) || (yaffs.exists() && yaffs.isDirectory()))
	                	type = "dir";
	                
	                DataEntry entry = new DataEntry(name, dataSize.trim(), yaffsSize.trim(), type);
	                entries.add(entry);
	            }
			
			}else{
				DataEntry entry = new DataEntry("Can't determine contents", "0", "0", "file");
				entries.add(entry);
			
			}
			Collections.sort(entries);

	            return entries;
			
			
		} catch (Exception e) {
			System.out.println("can't determine sizes for this app");
		}
		return null;
		
	}

	@Override
	public void onClick(View v) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		
		switch(v.getId()){
		case R.id.btnClearCaches:			
			Fragment prev = getFragmentManager().findFragmentByTag("dialog");
			if (prev != null) ft.remove(prev);
			ft.addToBackStack(null);
			DialogFragment newFragment = WipeDialog.newInstance(mHandledApp, "cache");
			newFragment.show(ft, "dialog");
			break;
		case R.id.btnClearAllData:
			Fragment old = getFragmentManager().findFragmentByTag("dialog");
			if (old != null) ft.remove(old);
			ft.addToBackStack(null);
			DialogFragment newestFragment = WipeDialog.newInstance(mHandledApp, "data");
			newestFragment.show(ft, "dialog");
			break;
		}
		
		
		
	}

}
