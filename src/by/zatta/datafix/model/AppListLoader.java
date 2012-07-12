package by.zatta.datafix.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import by.zatta.datafix.BaseActivity.InterestingConfigChanges;
import by.zatta.datafix.BaseActivity.PackageIntentReceiver;
import by.zatta.datafix.assist.ShellProvider;


	/**
     * A custom Loader that loads all of the installed applications.
     */
    public class AppListLoader extends AsyncTaskLoader<List<AppEntry>> {
        final InterestingConfigChanges mLastConfig = new InterestingConfigChanges();
        final PackageManager mPm;
        public static long biggestSize;

        List<AppEntry> mApps;
        PackageIntentReceiver mPackageObserver;

        public AppListLoader(Context context) {
            super(context);

            // Retrieve the package manager for later use; note we don't
            // use 'context' directly but instead the save global application
            // context returned by getContext().
            mPm = getContext().getPackageManager();
        }

        /**
         * This is where the bulk of our work is done.  This function is
         * called in a background thread and should generate a new set of
         * data to be published by the loader.
         */
        @Override public List<AppEntry> loadInBackground() {
            // Retrieve all known applications.
        	String[] total=null;
        	String[] caches=null;
        	String[] ctxt=null;
        	String[] dtxt=null;
        		total= ShellProvider.INSTANCE.getCommandOutput("du -sL /datadata/*|sort -n | awk -F \"/\" '{ print $3, $1 }'").split(" ");
        		caches = ShellProvider.INSTANCE.getCommandOutput("du -sL /datadata/*/cache|sort -n | awk -F \"/\" '{ print $3, $1 }'").split(" ");
        		ctxt = ShellProvider.INSTANCE.getCommandOutput("cat /data/local/datafix/move_cache.txt").split(" ");
        		dtxt = ShellProvider.INSTANCE.getCommandOutput("cat /data/local/datafix/skip_apps.txt").split(" ");
        	        	
        	File cacheFile = new File("/data/local/datafix/move_cache.txt");
        	if (!cacheFile.exists() || ctxt.length == 1){
        		ctxt = new String[2];
        		ctxt[0] = "com.android.providers.downloads"; ctxt[1]= "com.google.android.gm";
        	}
        	
        	List<ApplicationInfo> apps = mPm.getInstalledApplications(
                    PackageManager.GET_UNINSTALLED_PACKAGES |
                    PackageManager.GET_DISABLED_COMPONENTS);
            if (apps == null) {
                apps = new ArrayList<ApplicationInfo>();
                
            }

            final Context context = getContext();

            // Create corresponding array of entries and load their labels.
            List<AppEntry> entries = new ArrayList<AppEntry>(apps.size());
            biggestSize=0;
            for (int i=0; i<apps.size(); i++) {
                AppEntry entry = new AppEntry(this, apps.get(i), "false", "false");
                entry.loadLabel(context);
                for(int a = 0; a < total.length ; a=a+2){
    	    	    if (entry.getPackName().equals(total[a])){
                	//System.out.println("total: "+total[a] + " " + total[a+1]);
                	entry.setTotalSize((total[a+1]).trim());
                	if (Long.valueOf(total[a+1].trim())*1024 > biggestSize)
                		biggestSize=Long.valueOf(total[a+1].trim())*1024;
    	    	    }
                }
    	    	for(int a = 0; a < caches.length ; a=a+2){
    	    		if (entry.getPackName().equals(caches[a])){
    	    	    //System.out.println("caches: "+ caches[a] + " " + caches[a+1]);
    	    	    entry.setCacheSize((caches[a+1]).trim());
    	    		}
            	}
    	    	for(int a = 0; a < ctxt.length ; a++){
    	    		if (entry.getPackName().equals(ctxt[a])){
    	    	    //System.out.println("cache txt: "+ ctxt[a]);
    	    	    entry.setCacheBool("true");
    	    		}
            	}
    	    	for(int a = 0; a < dtxt.length ; a++){
    	    		if (entry.getPackName().equals(dtxt[a])){
    	    	    //System.out.println("data txt: "+ dtxt[a]);
    	    	    entry.setDataBool("true");
    	    		}
            	}
    	    	
    	    	entries.add(entry);
            }
            
            
            // Sort the list.
            Collections.sort(entries);

            // Done!
            return entries;
        }
      	    

        /**
         * Called when there is new data to deliver to the client.  The
         * super class will take care of delivering it; the implementation
         * here just adds a little more logic.
         */
        @Override public void deliverResult(List<AppEntry> apps) {
            if (isReset()) {
                // An async query came in while the loader is stopped.  We
                // don't need the result.
                if (apps != null) {
                    onReleaseResources(apps);
                }
            }
            List<AppEntry> oldApps = apps;
            mApps = apps;

            if (isStarted()) {
                // If the Loader is currently started, we can immediately
                // deliver its results.
                super.deliverResult(apps);
            }

            // At this point we can release the resources associated with
            // 'oldApps' if needed; now that the new result is delivered we
            // know that it is no longer in use.
            if (oldApps != null) {
                onReleaseResources(oldApps);
            }
        }

        /**
         * Handles a request to start the Loader.
         */
        @Override protected void onStartLoading() {
            if (mApps != null) {
                // If we currently have a result available, deliver it
                // immediately.
                deliverResult(mApps);
            }

            // Start watching for changes in the app data.
            if (mPackageObserver == null) {
                mPackageObserver = new PackageIntentReceiver(this);
            }

            // Has something interesting in the configuration changed since we
            // last built the app list?
            boolean configChange = mLastConfig.applyNewConfig(getContext().getResources());

            if (takeContentChanged() || mApps == null || configChange) {
                // If the data has changed since the last time it was loaded
                // or is not currently available, start a load.
                forceLoad();
            }
        }

        /**
         * Handles a request to stop the Loader.
         */
        @Override protected void onStopLoading() {
            // Attempt to cancel the current load task if possible.
            cancelLoad();
        }

        /**
         * Handles a request to cancel a load.
         */
        @Override public void onCanceled(List<AppEntry> apps) {
            super.onCanceled(apps);

            // At this point we can release the resources associated with 'apps'
            // if needed.
            onReleaseResources(apps);
        }

        /**
         * Handles a request to completely reset the Loader.
         */
        @Override protected void onReset() {
            super.onReset();

            // Ensure the loader is stopped
            onStopLoading();

            // At this point we can release the resources associated with 'apps'
            // if needed.
            if (mApps != null) {
                onReleaseResources(mApps);
                mApps = null;
            }

            // Stop monitoring for changes.
            if (mPackageObserver != null) {
                getContext().unregisterReceiver(mPackageObserver);
                mPackageObserver = null;
            }
        }

        /**
         * Helper function to take care of releasing resources associated
         * with an actively loaded data set.
         */
        protected void onReleaseResources(List<AppEntry> apps) {
            // For a simple List<> there is nothing to do.  For something
            // like a Cursor, we would close it here.
        }
    }
