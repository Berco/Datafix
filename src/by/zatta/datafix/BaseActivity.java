package by.zatta.datafix;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

import by.zatta.datafix.dialog.ShowInfoDialog;
import by.zatta.datafix.dialog.WipeDialog.OnWipedListener;
import by.zatta.datafix.fragment.AppListFragment;
import by.zatta.datafix.fragment.AppListFragment.OnAppSelectedListener;
import by.zatta.datafix.fragment.PrefFragment;
import by.zatta.datafix.fragment.AppDetailFragment;
import by.zatta.datafix.model.AppEntry;
import by.zatta.datafix.model.AppListLoader;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuInflater;
import android.view.MenuItem;

public class BaseActivity extends Activity implements OnAppSelectedListener, OnWipedListener{
	public static final boolean DEBUG = true;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        new PlantFiles().execute();
        
        FragmentManager fm = getFragmentManager();

        if (fm.findFragmentById(android.R.id.content) == null) {
            AppListFragment list = new AppListFragment();
            fm.beginTransaction().add(android.R.id.content, list, "list").commit();
        }
    }
    
    @Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater blowUp = getMenuInflater();
		blowUp.inflate(R.menu.leftclick_optionchooser, menu);
		return true;
	}
    
    @Override
	public void onAppSelectedListener(AppEntry app) {
		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		Fragment pref = getFragmentManager().findFragmentByTag("details");
		if (pref == null){
			ft.replace(android.R.id.content, new AppDetailFragment(app), "details");
			ft.addToBackStack(null);
			ft.commit();		
		}else{
			ft.remove(getFragmentManager().findFragmentByTag("details"));
			ft.commit();
			fm.popBackStack();	
		}
	}
    
    @Override
	public void onWipedListener() {
    	Fragment pref = getFragmentManager().findFragmentByTag("details");
    	Fragment list = getFragmentManager().findFragmentByTag("list");
    	// TODO need to add a check for this one in onWipedListener
    	((AppDetailFragment) pref).upDateDetails();
    	((AppListFragment) list).upDateList();
	}

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		Fragment pref = getFragmentManager().findFragmentByTag("prefs");
		if (pref == null){
			ft.replace(android.R.id.content, new PrefFragment(), "prefs");
			ft.addToBackStack(null);
			ft.commit();		
		}else{
			ft.remove(getFragmentManager().findFragmentByTag("prefs"));
			ft.commit();
			fm.popBackStack();
		}
		return false;
	}
    
    
    /**
     * Helper for determining if the configuration has changed in an interesting
     * way so we need to rebuild the app list.
     */
    public static class InterestingConfigChanges {
        final Configuration mLastConfiguration = new Configuration();
        int mLastDensity;

        public boolean applyNewConfig(Resources res) {
            int configChanges = mLastConfiguration.updateFrom(res.getConfiguration());
            boolean densityChanged = mLastDensity != res.getDisplayMetrics().densityDpi;
            if (densityChanged || (configChanges&(ActivityInfo.CONFIG_LOCALE
                    |ActivityInfo.CONFIG_UI_MODE|ActivityInfo.CONFIG_SCREEN_LAYOUT)) != 0) {
                mLastDensity = res.getDisplayMetrics().densityDpi;
                return true;
            }
            return false;
        }
    }

    /**
     * Helper class to look for interesting changes to the installed apps
     * so that the loader can be updated.
     */
    public static class PackageIntentReceiver extends BroadcastReceiver {
        final AppListLoader mLoader;

        public PackageIntentReceiver(AppListLoader loader) {
            mLoader = loader;
            IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
            filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
            filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
            filter.addDataScheme("package");
            mLoader.getContext().registerReceiver(this, filter);
            // Register for events related to sdcard installation.
            IntentFilter sdFilter = new IntentFilter();
            sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
            sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
            mLoader.getContext().registerReceiver(this, sdFilter);
        }

        @Override public void onReceive(Context context, Intent intent) {
            // Tell the loader about the change.
            mLoader.onContentChanged();
        }
    }
    
    private class PlantFiles extends AsyncTask<Void, Void, Void> {
    	private String destFileName = "/datafix_ng_busybox";
    	
		@Override
		protected Void doInBackground(Void... arg0) {
			setPreferences();
			
			String data_storage_root = getBaseContext().getFilesDir().toString();
			
			InputStream is= null;
			OutputStream os = null;
			
			File f = new File(data_storage_root+"/totalscript.sh");
			if (!f.exists() || (f.exists())){
				try {
					is = getResources().getAssets().open("scripts/totalscript.sh");
					os = new FileOutputStream(data_storage_root+"/totalscript.sh");
					IOUtils.copy(is, os);
					is.close();
					os.flush();
					os.close();
					os = null;

				} catch (IOException e) {}
			}
			
			File d = new File(data_storage_root+"/datafix_ng_busybox");
			if (!d.exists() || (d.exists())){
				try {
					is = getResources().getAssets().open("scripts/30datafix_ng_busybox");
					os = new FileOutputStream(data_storage_root+destFileName);
					IOUtils.copy(is, os);
					is.close();
					os.flush();
					os.close();
					os = null;

				} catch (IOException e) {}
			}
			return null;
		}
		
		private void setPreferences(){
	    	SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
	    	Editor editor = getPrefs.edit();
	    	editor.putString("kernel", ShowInfoDialog.getKernelVersion());
	        editor.putString("tibuState", ShowInfoDialog.getTitaniumState());
	        editor.putString("initDContent", ShowInfoDialog.getInitDContent());
	        editor.putString("showSizes", ShowInfoDialog.showSizes());
	        editor.putBoolean("fixed", ShowInfoDialog.isFixed());
	        editor.commit();
	    }
	}
	
}
