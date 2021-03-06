package by.zatta.datafix.dialog;

import by.zatta.datafix.BaseActivity;
import by.zatta.datafix.R;
import by.zatta.datafix.assist.ShellProvider;
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
    	getDialog().setTitle(getString(R.string.ExitTitle));
        View v = inflater.inflate(R.layout.exit_dialog, container, false);
        
        View tv = v.findViewById(R.id.text);
        ((TextView)tv).setText(getString(R.string.RealyExit));

        
        Button NO = (Button)v.findViewById(R.id.btnNoExit);
        Button YESREBOOT = (Button) v.findViewById(R.id.btnYesExit);
        
        if (BaseActivity.DEBUG){	
			YESREBOOT.setText(getString(R.string.btnYesExit) + '\n' + "dump logcat");
		}
        
        
        NO.setOnClickListener(this); 
        YESREBOOT.setOnClickListener(this); 
        
		return v;
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case R.id.btnNoExit:
			break;
		
		case R.id.btnYesExit:
			if (BaseActivity.DEBUG){
				ShellProvider.INSTANCE.getCommandOutput("echo \" \" >> /sdcard/debugfileZatta.txt");
				ShellProvider.INSTANCE.getCommandOutput("echo \"***** LOGCAT ***\" >> /sdcard/debugfileZatta.txt");
				ShellProvider.INSTANCE.getCommandOutput("logcat -d >> /sdcard/debugfileZatta.txt");
			}
			getActivity().finish();
			break;
		}
		dismiss();
	}
}
