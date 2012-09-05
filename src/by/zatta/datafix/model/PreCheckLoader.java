package by.zatta.datafix.model;

import by.zatta.datafix.assist.ShellProvider;
import android.content.AsyncTaskLoader;
import android.content.Context;

    public class PreCheckLoader extends AsyncTaskLoader<String> {

        public PreCheckLoader(Context context) {
            super(context);
        }

        @Override public String loadInBackground() {
            ShellProvider.INSTANCE.getCommandOutput("chmod 740 /data/data/by.zatta.datafix/files/totalscript.sh");
            String testString = ShellProvider.INSTANCE.getCommandOutput("/data/data/by.zatta.datafix/files/totalscript.sh check_sizes ");
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
