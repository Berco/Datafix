package by.zatta.datafix.dialog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.io.FileUtils;

import by.zatta.datafix.R;
import by.zatta.datafix.assist.ShellProvider;
import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ShowInfoDialog extends DialogFragment {
	
	private static final String KERNEL_VERSION_PATH = "/proc/version";
	private static final String TITANIUM_PATH = "/data/data/com.keramidas.TitaniumBackup/shared_prefs/com.keramidas.TitaniumBackup_preferences.xml";
	private static final String INITD_PATH = "/system/etc/init.d/";
			
	public static ShowInfoDialog newInstance() {
        ShowInfoDialog f = new ShowInfoDialog();
        return f;
    }
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, 0);
        setRetainInstance(true);
    }
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	getDialog().setTitle("Some info:");
        View v = inflater.inflate(R.layout.aboutdialog_layout, container, false);
        
        TextView tv = (TextView) v.findViewById(R.id.tvAbout);
        SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
		String kernel = getPrefs.getString("kernel", "undefined");
		String initDContent = getPrefs.getString("initDContent", "undefined");
		String tibuState = getPrefs.getString("tibuState", "undefined");
		String [] sizes = getPrefs.getString("showSizes", "1 2 3 4 5").split(" ");
		String version = getPrefs.getString("version", "not defined");
		String total = sizes[0] + " " + sizes[1];
		String free = sizes[2] + " " + sizes[3];
		String percent = sizes[4];
		
		Boolean fixed = getPrefs.getBoolean("fixed", false);
		String alreadyFixed="";
		if (fixed) alreadyFixed = "true, datafixed system"; else alreadyFixed = "false, no datafix installed";
		
		if (tibuState.contains("true"))
			tibuState = "You have Titanium installed and you have backupFollowSymlinks checked.";
		if (tibuState.contains("false"))
			tibuState = "You have Titanium installed and need to check backupFollowSymLinks in it's settings.";
		if (tibuState.contains("null"))
			tibuState = "You don't have Titanium Backup installed. Once you do, check backupFollowSymLinks in it's settings.";
        String about = new String(
				"Just to show some info:" + '\n' + '\n'+
				"Your Kernel:"+ '\n' +
				kernel+ '\n'+'\n'+
				
				"This needs a script starting with " + initDContent + '\n' + '\n' +
				
				tibuState + '\n' + '\n'+
				
				"/datadata has:" + '\n' +
				"  Total Space: " + total + '\n' +
				"  Free Space:  " + free + " (" + percent + ")"  + '\n' + '\n' +
				
				"/data/data is a symlink: " + alreadyFixed + '\n' + '\n' +
				
				getDateAndTime() + '\n' + '\n' +
				
				version
				
				);
        tv.setText(about);
                
		return v;
    }
	
	public static String getKernelVersion() {
		String kernelVersion = "";
        try {
            InputStream is = new FileInputStream(KERNEL_VERSION_PATH);
            InputStreamReader ir = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(ir);

            String line;
            while ((line = br.readLine())!= null ) {
                kernelVersion = line;
            }

            is.close();
        } catch (IOException e) {
            Log.e("datafix", "Problem reading kernel version file");
            kernelVersion = "";
            ;
        }
        return kernelVersion;
    }
	
	public static String getTitaniumState(){
		String titaniumState = "";
		ShellProvider.INSTANCE.getCommandOutput("chmod 777 " + TITANIUM_PATH);
		
		ShellProvider.INSTANCE.getCommandOutput("chmod 777 /data/data/com.keramidas.TitaniumBackup/shared_prefs");
		ShellProvider.INSTANCE.getCommandOutput("chmod 755 /datadata/com.keramidas.TitaniumBackup/shared_prefs");
		
		try {
            InputStream is = new FileInputStream(TITANIUM_PATH);
            InputStreamReader ir = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(ir);

            String line = null;
            while ((line = br.readLine())!= null ) {
            	if (line.contains("name=\"backupFollowSymlinks\"") && line.contains("true"))
            	    titaniumState = "true";
            	if (line.contains("name=\"backupFollowSymlinks\"") && line.contains("false"))
            	    titaniumState = "false";
            	if (line.length()==0){
            		titaniumState ="null";
            	}
            }
            is.close();
        } catch (IOException e) {
            Log.e("datafix", "Problem reading titanium prefs file");
            titaniumState = "null";
            ;
        }
		
		ShellProvider.INSTANCE.getCommandOutput("chmod 660 " + TITANIUM_PATH);
		
		return titaniumState;
	}
	
	public static String getInitDContent(){
		File f = new File(INITD_PATH);
		if (f.exists()&& f.isDirectory()){
			String[] list = f.list();
			String initDContent = "";
			for (String ss : list){
				initDContent = initDContent + ss.charAt(0);
			}
			if (!initDContent.contains("S"))
				initDContent = "30";
			else initDContent = "S30";
			return initDContent;	
		}else
			return "null";
	}
	
	public static String showSizes(){
		String showSizes = " ";
		File datadata = new File ("/datadata/");
		if (datadata.isDirectory()){
			Long total = datadata.getTotalSpace();
			Long free = datadata.getFreeSpace();
			int percent = (int)(((double)free/total)*100);
			
			showSizes = readable(total, false);
			showSizes = showSizes + " " + readable(free, false);
			showSizes = showSizes + " " + Integer.toString(percent) + "%";			
		}
		else {
			long size = FileUtils.sizeOfDirectory(new File("/data"));
			showSizes = readable(size, false) + " 0 MB 3%";	
		}
		return showSizes;
	}
	
	public static String readable(Long bytes, boolean si) {
    	int unit = si ? 1000 : 1024;
    	boolean negative = false;
    	if (bytes < 0){
    		negative = true;
    		bytes = Math.abs(bytes);
    	}
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        String output = String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
        if (negative) output = "-"+output;
        return output;
    }
	
	public static boolean isFixed(){
		String fixed = ShellProvider.INSTANCE.getCommandOutput("[ -L \"/data/data\" ] && echo nofix");
		if (fixed.contains("nofix")){
			return false;
		}else return true;
	}
	
	public static String ourVersion(){
		String version = "";
		String initdVersion = ShellProvider.INSTANCE.getCommandOutput("grep \"version\" /system/etc/init.d/*30datafix_ng_busybox | tr -d '[:alpha:] [:punct:]'").trim();
		if (initdVersion.length() < 8) initdVersion = "0";  //exists but no version
		if (initdVersion.length() > 8) initdVersion = "-1"; // doesn't exist
		int initInt = Integer.valueOf(initdVersion);
		
		String appVersion = ShellProvider.INSTANCE.getCommandOutput("grep \"version\" /data/data/by.zatta.datafix/files/datafix_ng_busybox | tr -d '[:alpha:] [:punct:]'").trim();
		if (appVersion.length() != 8) appVersion = "0"; // just a precaution..
		int appInt = Integer.valueOf(appVersion);
		
		String fileVersion = ShellProvider.INSTANCE.getCommandOutput("cat /data/data/.datafix_ng").trim();
		if (fileVersion.length() < 8) fileVersion = "0";   // exists but no version
		if (fileVersion.length() > 8) fileVersion = "-1";  // doesn't exist
		int fileInt = Integer.valueOf(fileVersion);
		
		String process = "full_update";
		if (initInt >= appInt) process = "only_files";
		if (initInt >= fileInt) process = "only_files";
		if (initInt < appInt ) process = "files_and_script";
		if (initInt < 0 || fileInt < 0) process = "full_update";
		
		initdVersion = "initdVersion: " + Integer.toString(initInt) + " ";
		appVersion = "appVersion: " + Integer.toString(appInt) + " ";
		fileVersion = "fileVersion: " + Integer.toString(fileInt) + " ";
		process = "installation: " + process;
		
		version = initdVersion + '\n' + appVersion + '\n' + fileVersion + '\n' + process;
		return version;
	}
	
	public static String getDateAndTime(){
		Calendar c = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd.HH.mm.ss");
		String dateAndTime = sdf.format(c.getTime())+"-datafix";
		
		return dateAndTime;
	}
	
}
