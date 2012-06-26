package by.zatta.datafix.fragment;

import by.zatta.datafix.R;
import by.zatta.datafix.dialog.AboutDialog;
import by.zatta.datafix.dialog.ShowInfoDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

public class PrefFragment extends PreferenceFragment {
	
	

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs);
        
    }

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen screen,
			Preference pref) {
		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();	
		Fragment about = getFragmentManager().findFragmentByTag("dialog");
		
		if (about != null) ft.remove(about);
		ft.addToBackStack(null);
		
		if (pref.getKey().contentEquals("about_app_key")){
			DialogFragment aboutFragment = AboutDialog.newInstance();
			aboutFragment.show(ft, "dialog");
			return true;
		}
		if (pref.getKey().contentEquals("about_phone_key")){
			DialogFragment aboutFragment = ShowInfoDialog.newInstance();
			aboutFragment.show(ft, "dialog");
		}
		return false;
	}

}
