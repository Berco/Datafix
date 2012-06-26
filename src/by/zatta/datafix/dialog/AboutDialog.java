package by.zatta.datafix.dialog;

import by.zatta.datafix.R;
import android.app.DialogFragment;
import android.os.Bundle;
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
        String background = " Background"+'\n'+'\n'+
         "On Samsung Galaxy S, cyanogenmod 7 creates a ~175MB yaffs partition which "+
         "holds all app data.  This partition is mounted at /datadata and the usual "+
         "location of this directory at /data/data is a symbolic link that points "+
         "to /datadata."+
         
         '\n'+'\n'+
         "     $ ls -l /data/data"+'\n'+
         "     lrwxrwxrwx      1 system        system  9 Oct  8 19:34 /data/data -> /datadata"+
         
         '\n'+'\n'+
         "The app apk itself does not reside here, but data created by the app resides "+
         "here, especially, the apps sqlite databases and user preference xml files. "+
         "The cyanogenmod developers did it this way to address the lag experienced on "+
         "the stock Galaxy S firmware.  But, this produces a problem. "+
         '\n'+'\n'+
         "The /datadata partition is quite small and may be filled up quickly by an "+
         "app that caches a lot of data (g+ for example).  Once /datadata fills up, "+
         "apps will begin force closing since they will not be able to create any new"+
         "data on /datadata.  The ext4 /data partition, which is about 2GB, has plenty "+
         "of space free.  So, it is attractive to move /datadata onto this large "+
         "partition.  But then we have the lag problem again.  That is where this script "+
         "comes in.  It moves certain subdirectories from each app on the ext4 partition. "+
         "Default select the \"lib\" & \"libs\" subdirectories as here are static files : with "+ 
         "only read access, ext4 performances are not so bad compared to yaffs ones.";
        
        String about = new String(
				"  This app is a cooperation between" + '\n' +
				"  Wendigogo and Zatta, both members of xda" + '\n' + '\n' +
				background
				);
        tv.setText(about);
                
		return v;
    }

}
