package by.zatta.datafix.dialog;

import java.util.Comparator;

import by.zatta.datafix.R;
import by.zatta.datafix.fragment.AppListFragment;
import by.zatta.datafix.model.AppEntry;
import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class SortDialog extends DialogFragment implements RadioGroup.OnCheckedChangeListener, View.OnClickListener{
	OnSortListener sortListener;
	static Comparator<AppEntry> usedComparator;
	
	public static SortDialog newInstance(Comparator<AppEntry> sentComparator) {
        SortDialog f = new SortDialog();
        usedComparator = sentComparator;
        return f;
    }
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            sortListener = (OnSortListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnSortListener");
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
    	getDialog().setTitle(getString(R.string.SortTitle));
        View v = inflater.inflate(R.layout.sortdialog_layout, container, false);
        RadioGroup RG = (RadioGroup) v.findViewById(R.id.rgSortList);
        RadioButton btnAlpha = (RadioButton) v.findViewById(R.id.rbAlpha);
        RadioButton btnSize = (RadioButton) v.findViewById(R.id.rbSize);
        RadioButton btnType = (RadioButton) v.findViewById(R.id.rbType);
                
        if (usedComparator == AppListFragment.SIZE_COMPARATOR)
        	btnSize.setChecked(true);
        if (usedComparator == AppListFragment.TYPE_COMPARATOR)
        	btnType.setChecked(true);
        if (usedComparator == AppListFragment.ALPHA_COMPARATOR)
        	btnAlpha.setChecked(true);
        
        RG.setOnCheckedChangeListener(this);
        
        Button NO = (Button)v.findViewById(R.id.btnCancelSort);
        NO.setOnClickListener(this); 
                
		return v;
    }
    
    public interface OnSortListener{
		public void onSortListener(Comparator<AppEntry> sort);
	}

	@Override
	public void onClick(View v) {
		dismiss();
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int id) {
		switch (id){
		case R.id.rbAlpha:
			sortListener.onSortListener(AppListFragment.ALPHA_COMPARATOR);
			break;
		case R.id.rbSize:
			sortListener.onSortListener(AppListFragment.SIZE_COMPARATOR);
			break;
		case R.id.rbType:
			sortListener.onSortListener(AppListFragment.TYPE_COMPARATOR);
			break;
		}
		dismiss();
	}
}
