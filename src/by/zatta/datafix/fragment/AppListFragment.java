package by.zatta.datafix.fragment;

import java.io.File;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import by.zatta.datafix.BaseActivity;
import by.zatta.datafix.R;
import by.zatta.datafix.assist.TouchInterceptor;
import by.zatta.datafix.dialog.ChangelogDialog;
import by.zatta.datafix.dialog.ConfirmDialog;
import by.zatta.datafix.dialog.ExitDialog;
import by.zatta.datafix.dialog.FirstUseDialog;
import by.zatta.datafix.dialog.ShowInfoDialog;
import by.zatta.datafix.dialog.SortDialog;
import by.zatta.datafix.model.AppEntry;
import by.zatta.datafix.model.AppListAdapter;
import by.zatta.datafix.model.AppListLoader;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class AppListFragment extends ListFragment 
			implements LoaderManager.LoaderCallbacks<List<AppEntry>>, OnClickListener {
	private ProgressBar mProgressView;
	private ProgressBar mProgressFree;
	private LinearLayout mBarHolder;
	private TextView mTvFreeSpace;
	private Button mBtnExit;
	private Button mBtnFlash;
	private View mTopView;
	private TouchInterceptor mList;
	public List<AppEntry> appList;
	private Comparator<AppEntry> usedComparator = TYPE_COMPARATOR;
	private TouchInterceptor.TickListener mTickListener =
			    new TouchInterceptor.TickListener() {
			        @Override
					public void ticked(int item, int tick) {
			        	if (BaseActivity.DEBUG)
			        	System.out.println("Ticklisten item: "+item + " box: " + tick);
			            AppEntry app = appList.get(item);
			            switch (tick){
			            case 2:
			            	if (app.getCacheBool().equals("true")) app.setCacheBool("false");
			            	else app.setCacheBool("true");
			               	break;
			            case 1:
			            	if (app.getDataBool().equals("true")) app.setDataBool("false");
			            	else app.setDataBool("true");
			            	break;
			            }
			            mAdapter.clear();
			            mAdapter.setData(appList);
		            }
			    };
	
	private AppListAdapter mAdapter;
	OnAppSelectedListener mAppListener;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mAppListener = (OnAppSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }
    }
    
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) { 
		View view = inflater.inflate(R.layout.applist_layout, null);
		mProgressView = (ProgressBar) view.findViewById(R.id.loading_fff);
		mBarHolder = (LinearLayout) view.findViewById(R.id.llBarHolder);
		mProgressFree = (ProgressBar) view.findViewById(R.id.pbFreeSpaceTotal);
		mTvFreeSpace = (TextView) view.findViewById(R.id.tvSpaceOnDataData);
		mBtnExit = (Button) view.findViewById(R.id.btnExit);
		mBtnFlash = (Button) view.findViewById(R.id.btnFlash);
		mTopView = view.findViewById(R.id.vTopListDiscription);
		mBtnExit.setOnClickListener(this);
		mBtnFlash.setOnClickListener(this);
		mTopView.setOnClickListener(this);
		return view;
     }

	@Override public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

				mList = (TouchInterceptor) getListView();
				mList.setTickListener(mTickListener);
				mList.setDivider(null);
		        registerForContextMenu(mList);
		        			
		        setHasOptionsMenu(true);
				if ( mAdapter == null){
					mAdapter = new AppListAdapter(getActivity());
					setListAdapter(mAdapter);
					toggleLoading(true);			
					getLoaderManager().initLoader(0, null, this);
				
					SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
					Boolean firstUse = getPrefs.getBoolean("showFirstUse", true);
					if (firstUse) showFirstUse();
					
					String currentVersion = myAppVersion();
					String previousVersion = getPrefs.getString("oldVersion", "1.1.5");
					if (! currentVersion.equals(previousVersion)) showChangelog();
					//showChangelog();
				}
				freeSpaceAvailable();
	}
	
	public void upDateList(){
		toggleLoading(true);
		getLoaderManager().restartLoader(0, null, this);
	}
	
	public void resortList(Comparator<AppEntry> sort){
		usedComparator = sort;
		Collections.sort(appList, usedComparator);
		mAdapter.setData(appList);
	}
	
	public void freeSpaceAvailable(){
			File datadata = new File ("/datadata/");
			if (datadata.isDirectory()){
				Long total = datadata.getTotalSpace();
				Long free = datadata.getFreeSpace();
				int freePercent = (int)(((double)free/total)*100);
				String freeSpace = getString(R.string.FreeSpaceBar) + ShowInfoDialog.readable(free, false)+ '\n' + Integer.toString(freePercent) + "%";
				
				mProgressFree.setProgress(100 - freePercent);
				mTvFreeSpace.setText(freeSpace);
			}
	}
	
	private void toggleLoading(boolean show) {
    	mBarHolder.setVisibility(show ? View.VISIBLE : View.GONE);
    	mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
    	mBarHolder.setGravity(show ? Gravity.CENTER : Gravity.TOP);    	
    }
	
	public static final Comparator<AppEntry> ALPHA_COMPARATOR = new Comparator<AppEntry>() {
        private final Collator sCollator = Collator.getInstance();
        @Override
        public int compare(AppEntry p, AppEntry o) {
            return sCollator.compare(p.getLabel(), o.getLabel());
        }
    };
    
    public static final Comparator<AppEntry> SIZE_COMPARATOR = new Comparator<AppEntry>() {
    	private final Collator sCollator = Collator.getInstance();
    	@Override
        public int compare(AppEntry p, AppEntry o) {
				if ( o.getTotalSize() != p.getTotalSize() ){
					int num = 0;
					if (o.getTotalSize() > p.getTotalSize() ) num = 1; 
					if (o.getTotalSize() < p.getTotalSize() ) num =  -1;
					return num;	
				}else{
					return sCollator.compare(p.getLabel(), o.getLabel());
				}  
        }
    };
    
    public static final Comparator<AppEntry> TYPE_COMPARATOR = new Comparator<AppEntry>() {
    	private final Collator sCollator = Collator.getInstance();
    	@Override
        public int compare(AppEntry p, AppEntry o) {
				if ( sCollator.compare(p.getType(), o.getType()) == 0   ){
					int num = 0;
					if (o.getTotalSize() > p.getTotalSize() ) num = 1; 
					if (o.getTotalSize() == p.getTotalSize() ) num =  0;
					if (o.getTotalSize() < p.getTotalSize() ) num =  -1;
					return num;
				}else{	
					return sCollator.compare(o.getType(), p.getType());
				}
			}
    };
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (BaseActivity.DEBUG)
		Log.i("BaseActivity", "Item clicked: " + id);
		AppEntry item = (AppEntry) getListAdapter().getItem(position);
        mAppListener.onAppSelectedListener(item);
	}
	
	public interface OnAppSelectedListener{
		public void onAppSelectedListener(AppEntry app);
	}	

	@Override public Loader<List<AppEntry>> onCreateLoader(int id, Bundle args) {
		return new AppListLoader(getActivity());
	}

	@Override public void onLoadFinished(Loader<List<AppEntry>> loader, List<AppEntry> data) {
		appList = data;
		toggleLoading(false);
		freeSpaceAvailable();
		Collections.sort(appList, usedComparator);
		mAdapter.setData(appList);
	}

	@Override public void onLoaderReset(Loader<List<AppEntry>> loader) {
		mAdapter.setData(null);
	}

	public void showFirstUse(){	
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag("dialog");
		if (prev != null) ft.remove(prev);
		ft.addToBackStack(null);
		DialogFragment newFragment = FirstUseDialog.newInstance();
		newFragment.show(ft, "dialog");
	}
	
	public void showChangelog(){	
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag("dialog");
		if (prev != null) ft.remove(prev);
		ft.addToBackStack(null);
		DialogFragment newFragment = ChangelogDialog.newInstance();
		newFragment.show(ft, "dialog");
	}
	
	public String myAppVersion(){
		PackageInfo pinfo;
		try {
			pinfo = getActivity().getBaseContext().getPackageManager().getPackageInfo((getActivity().getBaseContext().getPackageName()), 0);
			return pinfo.versionName;
		} catch (NameNotFoundException e) {
			return " ";
		}
		
	}
	
	@Override
	public void onClick(View v) {
		
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		switch(v.getId()){
		case R.id.btnFlash:			
			Fragment prev = getFragmentManager().findFragmentByTag("dialog");
			if (prev != null) ft.remove(prev);
			ft.addToBackStack(null);
			DialogFragment newFragment = ConfirmDialog.newInstance(appList);
			newFragment.show(ft, "dialog");
			break;
		case R.id.btnExit:
			Fragment exit = getFragmentManager().findFragmentByTag("dialog");
			if (exit != null) ft.remove(exit);
			ft.addToBackStack(null);
			DialogFragment exitFragment = ExitDialog.newInstance();
			exitFragment.show(ft, "dialog");			
			break;	
		case R.id.vTopListDiscription:
			Fragment sort = getFragmentManager().findFragmentByTag("dialog");
			if (sort != null) ft.remove(sort);
			ft.addToBackStack(null);
			DialogFragment sortFragment = SortDialog.newInstance(usedComparator);
			sortFragment.show(ft, "dialog");			
			break;
		}
	}
}
