package by.zatta.datafix.dialog;

import by.zatta.datafix.R;
import by.zatta.datafix.assist.ShellProvider;
import by.zatta.datafix.model.AppEntry;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class WipeDialog extends DialogFragment implements View.OnClickListener{
	private ActivityManager manager;
	static AppEntry mHandledApp;
	static String mMode;
	OnWipedListener wipeListener;
	
    public static WipeDialog newInstance(AppEntry app, String wipe) {
        WipeDialog f = new WipeDialog();
        mHandledApp = app;
        mMode = wipe;
        return f;
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            wipeListener = (OnWipedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, 0);
        setRetainInstance(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	getDialog().setTitle("WIPE " + mMode.toUpperCase());
        View v = inflater.inflate(R.layout.wipe_dialog, container, false);
        
        TextView tv = (TextView) v.findViewById(R.id.text);
       
        tv.setText("Do wipe for"+ '\n' + mHandledApp.getLabel());
               
        Button NO = (Button)v.findViewById(R.id.btnNoWipe);
        Button WIPE = (Button) v.findViewById(R.id.btnYesWipe);
              
        NO.setOnClickListener(this); 
        WIPE.setOnClickListener(this); 
       
		return v;
    }
    
    public interface OnWipedListener{
		public void onWipedListener();
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case R.id.btnNoWipe:
			break;
		case R.id.btnYesWipe:
			wipeIt();
			break;
		}
		dismiss();
	}

	private void wipeIt() {
		String scriptLine = getActivity().getBaseContext().getFilesDir().toString()+"/totalscript.sh";
		ShellProvider.INSTANCE.getCommandOutput("chmod 777 "+ scriptLine);	
		manager = (ActivityManager) getActivity().getSystemService("activity");
		manager.killBackgroundProcesses(mHandledApp.getPackName());
		
		if (mMode.contentEquals("cache")){
			
		
			//Remove the content of the cache. Folder stays in place.
			manager = (ActivityManager) getActivity().getSystemService("activity");
			manager.killBackgroundProcesses(mHandledApp.getPackName());
			
			ShellProvider.INSTANCE.getCommandOutput(scriptLine + " wipe_cache " + mHandledApp.getPackName());
						
			System.out.println("Dialog wiping cache");
		}else
		if (mMode.contentEquals("data")){
			
		
			//Remove all folders with their content except */lib. It's contents should also stay in place
			manager = (ActivityManager) getActivity().getSystemService("activity");
			manager.killBackgroundProcesses(mHandledApp.getPackName());
			
			ShellProvider.INSTANCE.getCommandOutput(scriptLine + " wipe_data " + mHandledApp.getPackName());
						
			System.out.println("Dialog wiping data");
			
		}
		wipeListener.onWipedListener();
	}
	
}

