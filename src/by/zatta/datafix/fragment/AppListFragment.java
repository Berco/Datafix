package by.zatta.datafix.fragment;

import java.io.File;
import java.util.List;

import by.zatta.datafix.BaseActivity;
import by.zatta.datafix.R;
import by.zatta.datafix.assist.TouchInterceptor;
import by.zatta.datafix.dialog.ConfirmDialog;
import by.zatta.datafix.dialog.ExitDialog;
import by.zatta.datafix.dialog.FirstUseDialog;
import by.zatta.datafix.dialog.ShowInfoDialog;
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
	private TouchInterceptor mList;
	public List<AppEntry> appList;
	private TouchInterceptor.TickListener mTickListener =
			    new TouchInterceptor.TickListener() {
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
		mBtnExit.setOnClickListener(this);
		mBtnFlash.setOnClickListener(this);
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
				}
				freeSpaceAvailable();
	}
	
	public void upDateList(){
		toggleLoading(true);
		getLoaderManager().restartLoader(0, null, this);
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
		mAdapter.setData(appList);
	}
	
	public void showFirstUse(){	
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag("dialog");
		if (prev != null) ft.remove(prev);
		ft.addToBackStack(null);
		DialogFragment newFragment = FirstUseDialog.newInstance();
		newFragment.show(ft, "dialog");
	}

	@Override public void onLoaderReset(Loader<List<AppEntry>> loader) {
		mAdapter.setData(null);
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
		}
		
	}

}
