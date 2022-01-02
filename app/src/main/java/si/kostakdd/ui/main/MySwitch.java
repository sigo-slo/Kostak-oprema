package si.kostakdd.ui.main;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

public class MySwitch extends SwitchCompat {


    private float x=0;

    public MySwitch(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MySwitch(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

            boolean response;
            switch (ev.getAction()){
                case MotionEvent.ACTION_DOWN:
                    x = ev.getX();
                    response = super.onTouchEvent(ev);
                    break;
                case MotionEvent.ACTION_UP:
                    response = Math.abs(x - ev.getX()) <= 10 || super.onTouchEvent(ev);
                    break;
                default:
                    response = super.onTouchEvent(ev);
                    break;
            }
            return response;
        }




}

