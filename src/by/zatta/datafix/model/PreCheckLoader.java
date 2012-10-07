package by.zatta.datafix.model;

import by.zatta.datafix.assist.ShellProvider;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

    public class PreCheckLoader extends AsyncTaskLoader<String> {
    	Context mContext;

        public PreCheckLoader(Context context) {
        	super(context);
        	mContext = context;
        }

        @Override public String loadInBackground() {
        	SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        	String checkmode = getPrefs.getString("scriptcheck", "simple_check");
        	ShellProvider.INSTANCE.getCommandOutput("chmod 740 /data/data/by.zatta.datafix/files/totalscript.sh");
            String testString = ShellProvider.INSTANCE.getCommandOutput("/data/data/by.zatta.datafix/files/totalscript.sh check_sizes "+checkmode);
            return testString;
        }
      	    
        @Override public void deliverResult(String testString) {
            super.deliverResult(testString);
        }

        @Override protected void onStartLoading() {
            forceLoad();
        }

        @Override protected void onStopLoading() {
            cancelLoad();
        }

        @Override public void onCanceled(String testString) {
            super.onCanceled(testString);
        }

        @Override protected void onReset() {
            super.onReset();
            onStopLoading();
        }
    }
