package by.zatta.datafix.fragment;

import by.zatta.datafix.R;
import by.zatta.datafix.dialog.AboutDialog;
import by.zatta.datafix.dialog.ShowInfoDialog;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.widget.Toast;

public class PrefFragment extends PreferenceFragment {
	
	OnLanguageListener languageListener;
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            languageListener = (OnLanguageListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnLanguageListener");
        }
    }
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
		if (pref.getKey().contentEquals("enableDebugging")){
			Toast.makeText(getActivity().getBaseContext(), getString(R.string.DebuggingWarning), Toast.LENGTH_LONG).show();
		}
		
		if (pref.getKey().contentEquals("languagePref")){
			pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					languageListener.onLanguageListener(newValue.toString());
					return true;
				}	
			});			
		}
		return false;
	}
	
	public interface OnLanguageListener{
		public void onLanguageListener(String language);
	}
	
}
