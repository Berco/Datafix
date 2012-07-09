package by.zatta.datafix.dialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import by.zatta.datafix.R;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutDialog extends DialogFragment {
	
	public static AboutDialog newInstance() {
        AboutDialog f = new AboutDialog();
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
    	getDialog().setTitle("About");
        View v = inflater.inflate(R.layout.aboutdialog_layout, container, false);
        
        TextView tv = (TextView) v.findViewById(R.id.tvAbout);
               
        Spanned inHtmlCC = Html.fromHtml(getAboutText());
                
        tv.setText(inHtmlCC);
                
		return v;
    }
	public String getAboutText(){
		InputStream is= null;
		String about="";
		
		try {
			is = getResources().getAssets().open("texts/background.html");
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

}
