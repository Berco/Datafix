package by.zatta.datafix.dialog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import by.zatta.datafix.BaseActivity;
import by.zatta.datafix.R;
import by.zatta.datafix.assist.ShellProvider;
import by.zatta.datafix.model.AppEntry;
import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

public class ConfirmDialog extends DialogFragment implements View.OnClickListener{
	List<AppEntry> fls;
	CheckBox mCbNandroid;
	String update;
	
	private LinearLayout mLinLayFlashView;
	
    public static ConfirmDialog newInstance(List<AppEntry> apps) {
        ConfirmDialog f = new ConfirmDialog();
        
        Bundle args = new Bundle();
        args.putParcelableArrayList("lijst", (ArrayList<? extends Parcelable>) apps);
        f.setArguments(args);
        
        return f;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fls = getArguments().getParcelableArrayList("lijst");
        setStyle(DialogFragment.STYLE_NORMAL, 0);
        setRetainInstance(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	getDialog().setTitle(getString(R.string.ConfirmTitle));
        View v = inflater.inflate(R.layout.confirm_dialog, container, false);
        
        TextView tvTB = (TextView) v.findViewById(R.id.text);
        TextView tvUP = (TextView) v.findViewById(R.id.text2);
        mCbNandroid = (CheckBox) v.findViewById(R.id.cbMakeNandroid);
        
        SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
		String tibuState = getPrefs.getString("tibuState", "undefined");
		int color = 0;
		if (tibuState.contains("true")){
			tibuState = getString(R.string.TiBuChecked);
			color = getResources().getColor(R.color.green);
		}
		if (tibuState.contains("false")){
			tibuState = getString(R.string.TiBuNotChecked);
			color = getResources().getColor(R.color.red);
		}
		if (tibuState.contains("null")){
			tibuState = getString(R.string.TiBuNotPresent);
			color = getResources().getColor(R.color.white);
		}
		tvTB.setTextColor(color);
		tvTB.setText(tibuState);
		
		String version = getPrefs.getString("version", "0 1 2 3 4 5 6 7");
		String[] versionArray = version.split(" ");
		update = versionArray[7];
		String updateMessage="";
		if (update.equals("only_files"))
			updateMessage = getString(R.string.FilesOnly);	
		if (update.equals("full_update"))
			updateMessage = getString(R.string.ShowVersion) + versionArray[3] + '\n' + getString(R.string.FullUpdate);
		if (update.equals("files_and_script")){
			updateMessage = getString(R.string.ShowVersion) + versionArray[3] + '\n' + getString(R.string.FilesAndScript);
		}
		
		tvUP.setText(updateMessage);
		
        mLinLayFlashView = (LinearLayout) v.findViewById(R.id.llFilesForDialog);
        buildForm();
        Button NO = (Button)v.findViewById(R.id.btnNoInstall);
        Button YESANDREBOOT = (Button) v.findViewById(R.id.btnYesAndReboot);
        Button YESNOREBOOT = (Button) v.findViewById(R.id.btnYesNoReboot);
        
        NO.setOnClickListener(this); 
        YESANDREBOOT.setOnClickListener(this); 
        YESNOREBOOT.setOnClickListener(this);
		return v;
    }

    private void buildForm() {
		boolean cachefile = false;
    	boolean datafile = false;
    	for (AppEntry ff : fls){
    		if (ff.getCacheBool().equals("true")) cachefile = true;
    		if (ff.getDataBool().equals("true")) datafile = true;
    	}
    	
    	if (cachefile){
    	addFormField(getString(R.string.ToCache));
    		for (AppEntry ff: fls){
    			if (ff.getCacheBool().equals("true"))
    				addFormField("  "+ ff.getPackName());
    		}
    		addFormField("");
    	}
    	
    	if (datafile){	
    		addFormField(getString(R.string.ToData));
    		for (AppEntry ff: fls){
    			if (ff.getDataBool().equals("true"))
    				addFormField("  "+ ff.getPackName());
    		}	
    	}
	}

	private void addFormField(String label) {
		TextView tvLabel = new TextView(getActivity());
		tvLabel.setLayoutParams(getDefaultParams(false));
		tvLabel.setText(label);

		mLinLayFlashView.addView(tvLabel);
		
	}

	private LayoutParams getDefaultParams(boolean isLabel) {
		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);
		if (isLabel) {
			params.bottomMargin = 5;
			params.topMargin = 10;
		}
		return params;
	}

	
	@Override
	public void onClick(View v) {
		SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
		String scripttype = getPrefs.getString("initDContent", "undefined");
				
		switch (v.getId()){
		case R.id.btnNoInstall:
			break;
		case R.id.btnYesAndReboot:
			try {
				String rebootMode = "";
				if (mCbNandroid.isChecked()){
					writeExtendedCommand();
					rebootMode = " reboot_recovery";
				}else{
					rebootMode=" reboot";
				}
				writeFiles();
				ShellProvider.INSTANCE.getCommandOutput("chmod 777 /data/data/by.zatta.datafix/files/totalscript.sh");
				ShellProvider.INSTANCE.getCommandOutput("/data/data/by.zatta.datafix/files/totalscript.sh prepare_runtime " + scripttype + " " + update + rebootMode);
				} catch (Exception e) {	}
				
			break;
		case R.id.btnYesNoReboot:
			if (!mCbNandroid.isChecked()){
			try {
				writeFiles();
				ShellProvider.INSTANCE.getCommandOutput("chmod 777 /data/data/by.zatta.datafix/files/totalscript.sh");
				ShellProvider.INSTANCE.getCommandOutput("/data/data/by.zatta.datafix/files/totalscript.sh prepare_runtime " + scripttype + " " + update +" noreboot");
				Toast.makeText(getActivity().getBaseContext(), getString(R.string.WarningNandroidNotChecked), Toast.LENGTH_LONG).show();
				
		    	Editor editor = getPrefs.edit();
		    	editor.putString("version", ShowInfoDialog.ourVersion());
		        editor.commit();
			} catch (Exception e) {	}
			}else{
				Toast.makeText(getActivity().getBaseContext(), getString(R.string.WarningNandroidChecked), Toast.LENGTH_LONG).show();
			}
			break;
		}
		dismiss();
	}

	public void writeFiles(){
	    try
	    {
	    	File cache = new File(getActivity().getBaseContext().getFilesDir()+"/move_cache.txt");
	        cache.delete();
	        FileWriter c = new FileWriter(cache, true);
	        File data = new File(getActivity().getBaseContext().getFilesDir()+"/skip_apps.txt");
	        data.delete();
	        FileWriter d = new FileWriter(data, true);
	        File user = new File(getActivity().getBaseContext().getFilesDir()+"/user_apps.txt");
	        user.delete();
	        FileWriter u = new FileWriter(user, true);
	        File syst = new File(getActivity().getBaseContext().getFilesDir()+"/system_apps.txt");
	        syst.delete();
	        FileWriter s = new FileWriter(syst, true);
	         
	        for (AppEntry ff: fls){
				if (ff.getCacheBool().equals("true")){
					c.append(ff.getPackName()+ '\n');
				}
				if (ff.getDataBool().equals("true")){
					d.append(ff.getPackName()+ '\n');
				}
				if (ff.getType().equals("user")){
					u.append(ff.getPackName() + '\n');
				}
				if (ff.getType().equals("system")){
					s.append(ff.getPackName() + '\n');
				}
				
			}
	        c.flush();
	        c.close();
	        d.flush();
	        d.close();
	        u.flush();
	        u.close();
	        s.flush();
	        s.close();
	        
	        if (BaseActivity.DEBUG){
	        System.out.println("Wrote file:" + cache.getName() );
	        System.out.println("Wrote file:" + data.getName() );
	        }
	     }catch(IOException e){}
	   }
	
		
	public void writeExtendedCommand(){
	    try
	    {
	    	File exco = new File(getActivity().getBaseContext().getFilesDir()+"/extendedcommand");
	        exco.delete();
	        FileWriter w = new FileWriter(exco, true);
	    	
	    	w.append("ui_print(\" \");"+'\n');
	        w.append("ui_print(\"  ---      DATAFIX      --- \");"+'\n');
	        w.append("ui_print(\"  ---     HERE WE GO    --- \");"+'\n');
	        w.append("ui_print(\"  --- ZATTA / WENDIGOGO --- \");"+'\n');
	        w.append("ui_print(\" \");"+'\n');
	        
	        String backupName= ShowInfoDialog.getDateAndTime();
	        String backup = "assert(backup_rom(\"/sdcard/clockworkmod/backup/"+backupName+"\"));";
	        w.append(backup);
	        
	        w.append(" "+'\n');
	        	        
	        w.flush();
	        w.close();
	        if (BaseActivity.DEBUG)
	        	System.out.println("Wrote file:" + exco.getName() );
	    }catch(IOException e){}
	   }    
}

