package by.zatta.datafix.dialog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
        setRetainInstance(false);
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
				
				getDateAndTime()
				
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
		ShellProvider.INSTANCE.getCommandOutput("ls -l /data/data/com.keramidas.TitaniumBackup/shared_prefs/com.keramidas.TitaniumBackup_preferences.xml | awk '{print $1}'");
		
		File f = new File(TITANIUM_PATH);
		if (f.exists()) System.out.println("TB file exists");
		else System.out.println("TB file does not");
		if (f.canRead()) System.out.println("TB file can be read");
		else System.out.println("TB file can not be read");
		if (f.isFile()) System.out.println("TB file is a file");
		else System.out.println("TB file is not a file");
		if (f.isHidden()) System.out.println("TB file is hidden");
		else System.out.println("TB file is not hidden");
		
		
		
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
		ShellProvider.INSTANCE.getCommandOutput("ls -l /data/data/com.keramidas.TitaniumBackup/shared_prefs/com.keramidas.TitaniumBackup_preferences.xml | awk '{print $1}'");
		if (f.exists()) System.out.println("TB file exists");
		else System.out.println("TB file does not");
		if (f.canRead()) System.out.println("TB file can be read");
		else System.out.println("TB file can not be read");
		if (f.isFile()) System.out.println("TB file is a file");
		else System.out.println("TB file is not a file");
		if (f.isHidden()) System.out.println("TB file is hidden");
		else System.out.println("TB file is not hidden");
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
		return showSizes;
	}
	
	public static String readable(Long bytes, boolean si) {
    	int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
	
	public static boolean isFixed(){
		String fixed = ShellProvider.INSTANCE.getCommandOutput("[ -L \"/data/data\" ] && echo nofix");
		if (fixed.contains("nofix")){
			return false;
		}else return true;
	}
	
	public static String getDateAndTime(){
		Calendar c = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd.HH.mm.ss");
		String dateAndTime = sdf.format(c.getTime())+"-datafix";
		return dateAndTime;
	}
}
