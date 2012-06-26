package by.zatta.datafix.dialog;

import by.zatta.datafix.R;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ExitDialog extends DialogFragment implements View.OnClickListener{
	
	public static ExitDialog newInstance() {
        ExitDialog f = new ExitDialog();
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
    	getDialog().setTitle("EXIT");
        View v = inflater.inflate(R.layout.exit_dialog, container, false);
        
        View tv = v.findViewById(R.id.text);
        ((TextView)tv).setText("Are you sure you want to exit?");

        
        Button NO = (Button)v.findViewById(R.id.btnNoExit);
        Button YESREBOOT = (Button) v.findViewById(R.id.btnYesExit);
        
        
        NO.setOnClickListener(this); 
        YESREBOOT.setOnClickListener(this); 
        
		return v;
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case R.id.btnNoExit:
			//Toast.makeText(getActivity(), "Stay", Toast.LENGTH_LONG).show();
			break;
		
		case R.id.btnYesExit:
			//Toast.makeText(getActivity(), "Leave", Toast.LENGTH_LONG).show();
			getActivity().finish();
			break;
		}
		dismiss();
	}
}
