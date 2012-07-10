package by.zatta.datafix.dialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import by.zatta.datafix.R;
import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
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
        
        Spanned inHtmlCC = Html.fromHtml(getAboutText());
        
        tv.setText(inHtmlCC);                
		
        return v;
    }
	
	public String getAboutText(){
		InputStream is= null;
		String about="";
		
		try {
			is = getResources().getAssets().open("texts/first_start.html");
			InputStreamReader ir = new InputStreamReader(is);
	        BufferedReader br = new BufferedReader(ir);
            String line;
            while ((line = br.readLine())!= null ) {
                about = about + line;
            }
			is.close();
		} catch (IOException e) {}
				
		return about;
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
