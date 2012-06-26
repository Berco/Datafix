package by.zatta.datafix.dialog;

import by.zatta.datafix.R;
import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class FirstUseDialog extends DialogFragment implements View.OnClickListener{
	
	public static FirstUseDialog newInstance() {
        FirstUseDialog f = new FirstUseDialog();
        return f;
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
    	getDialog().setTitle("Welcome!");
        View v = inflater.inflate(R.layout.firstusedialog_layout, container, false);
        
        TextView tv = (TextView) v.findViewById(R.id.tvAbout);
        Button DISMISS = (Button)v.findViewById(R.id.btnDismiss);
        Button DONTSHOW = (Button) v.findViewById(R.id.btnDontShowAgain);
        DISMISS.setOnClickListener(this);
        DONTSHOW.setOnClickListener(this);
        
        String about = new String(
				"  This app does nothing by itself" + '\n' +
				"  it installs a script in init.d" + '\n' +
				"  which does the actual work." + '\n' + '\n' +
				"  This means you have to reboot to get" + '\n' +
				"  the script to run and the datafix active." + '\n' + '\n' +
				"  By default all libraries will stay on /data" + '\n' +
				"  and the rest is moved back to /datadata" + '\n' +
				"  By checking the checkboxes you can choose" + '\n' +
				"  what else will stay in /data" + '\n'
				);
        tv.setText(about);
                
		return v;
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case R.id.btnDismiss:
			break;
		case R.id.btnDontShowAgain:
			SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
	    	Editor editor = getPrefs.edit();
	    	editor.putBoolean("showFirstUse", false);
	    	editor.commit();
			break;
		}
		dismiss();
		
	}

}
