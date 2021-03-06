package by.zatta.datafix;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.Locale;

import org.apache.commons.io.IOUtils;

import by.zatta.datafix.assist.ShellProvider;
import by.zatta.datafix.billing.BillingActivity;
import by.zatta.datafix.dialog.ShowInfoDialog;
import by.zatta.datafix.dialog.SortDialog.OnSortListener;
import by.zatta.datafix.dialog.WipeDialog.OnWipedListener;
import by.zatta.datafix.fragment.AppListFragment;
import by.zatta.datafix.fragment.AppListFragment.OnAppSelectedListener;
import by.zatta.datafix.fragment.PrefFragment;
import by.zatta.datafix.fragment.AppDetailFragment;
import by.zatta.datafix.fragment.PrefFragment.OnLanguageListener;
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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuInflater;
import android.view.MenuItem;

public class BaseActivity extends Activity implements OnAppSelectedListener, OnWipedListener, OnLanguageListener, OnSortListener{
	public static boolean DEBUG = false;
	private int mStars;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String version = myAppVersion();
        this.setTitle(getString(R.string.app_name) + " " + version);
        
        ShellProvider.INSTANCE.isSuAvailable();
        new PlantFiles().execute();
        
        SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        mStars = getPrefs.getInt("valueBugTrack", 1);
        String language = getPrefs.getString("languagePref", "unknown");
        if (!language.equals("unknown")) makeLocale(language);
        
        DEBUG = getPrefs.getBoolean("enableDebugging", false);
        
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
		MenuItem item = menu.add("Star");
		item.setTitle("star");
		item.setIcon(mStars > 0 ? R.drawable.star : R.drawable.star_empty);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		blowUp.inflate(R.menu.leftclick_optionchooser, menu);
		return true;
	}
    
    public void makeLocale(String language){
        Locale locale = new Locale(language); 
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, 
        getBaseContext().getResources().getDisplayMetrics());
    }
    
    public String myAppVersion(){
		PackageInfo pinfo;
		try {
			pinfo = this.getPackageManager().getPackageInfo((this.getPackageName()), 0);
			return pinfo.versionName;
		} catch (NameNotFoundException e) {
			return " ";
		}
		
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
	public void onLanguageListener(String language) {
    	makeLocale(language);
    	FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		fm.popBackStack();		
		ft.replace(android.R.id.content, new PrefFragment(), "prefs");
		ft.addToBackStack(null);
		ft.commit();
	}
    
    @Override
	public void onSortListener(Comparator<AppEntry> sort) {
		Fragment list = getFragmentManager().findFragmentByTag("list");
    	((AppListFragment) list).resortList(sort);
	}

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	if (item.getTitle().equals("star")){
    		Intent i = new Intent(BaseActivity.this, BillingActivity.class);
			startActivityForResult(i,14);	
    	}else{
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
    	}
		return false;
	}
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK){
			Bundle basket = data.getExtras();
			int s = basket.getInt("answer");
			mStars = s;
			invalidateOptionsMenu();
		}
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
					
			String data_storage_root = getBaseContext().getFilesDir().toString();
			
			InputStream is= null;
			OutputStream os = null;
			
			File f = new File(data_storage_root+"/totalscript.sh");
			if (!f.exists() || f.exists()){
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
			
			File g = new File(data_storage_root+"/60-datafix.sh");
			if (!g.exists() || g.exists()){
				try {
					is = getResources().getAssets().open("scripts/60-datafix.sh");
					os = new FileOutputStream(data_storage_root+"/60-datafix.sh");
					IOUtils.copy(is, os);
					is.close();
					os.flush();
					os.close();
					os = null;
				} catch (IOException e) {}
			}
			
			File d = new File(data_storage_root+"/datafix_ng_busybox");
			if (!d.exists() || f.exists() ){
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
			setPreferences();
			return null;
		}
		
		private void setPreferences(){
	    	SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
	    	Editor editor = getPrefs.edit();
	    	editor.putString("kernel", ShowInfoDialog.getKernelVersion());
	        editor.putString("tibuState", ShowInfoDialog.getTitaniumState());
	        editor.putString("initDContent", ShowInfoDialog.getInitDContent());
	        editor.putString("showSizes", ShowInfoDialog.showSizes());
	        editor.putString("version", ShowInfoDialog.ourVersion());
	        editor.putBoolean("fixed", ShowInfoDialog.isFixed());
	        editor.commit();
	    }
	}

}
