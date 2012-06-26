package by.zatta.datafix.assist;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public class TouchInterceptor extends ListView {
    
    private TickListener mTickListener;
    private static int width = 48;
    boolean doit = false;
    
    public TouchInterceptor(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
    	if (mTickListener !=null){
            	switch (ev.getAction()) {
            	case MotionEvent.ACTION_DOWN:
                    int x = (int) ev.getX();
                    int y = (int) ev.getY();
                    int itemnum = pointToPosition(x, y);
                    if (itemnum == AdapterView.INVALID_POSITION) {
                        break;
                    }
                    ViewGroup item = (ViewGroup) getChildAt(itemnum - getFirstVisiblePosition());
                    
                    // The right side of the item is the grabber for touching the item
                    if (item.getRight()-x < width) {
                    	System.out.println("Tick inside intercept (right):"+ itemnum );
                    	mTickListener.ticked(itemnum, 1);
                    	doit = true;
                    	return false;
                    }
                    if ((item.getRight()-width)-x < width) {
                    	System.out.println("Tick inside intercept (left):"+ itemnum);
                    	mTickListener.ticked(itemnum, 2);
                    	doit = true;
                    	return false;
                    }
                }           	
            }        
        return super.onInterceptTouchEvent(ev);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        
        if (mTickListener != null && doit) {
        	doit = false;
        	return true;
        }
        return super.onTouchEvent(ev);
    }

    
    public void setTickListener(TickListener l){
    	mTickListener = l;
    }
    
    public interface TickListener {
    	void ticked(int item, int box);
    }
}
