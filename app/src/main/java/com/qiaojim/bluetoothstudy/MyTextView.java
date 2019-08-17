package com.qiaojim.bluetoothstudy;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * author : 简玉锋
 * e-mail : yufeng_jian@fpi-inc.com
 * date   : 2019/8/12 17:23
 * desc   :
 * version: 1.0
 */
public class MyTextView extends android.support.v7.widget.AppCompatTextView {

    public MyTextView(Context context) {
        super(context);
    }

    public MyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (downUpLister != null) {
                    downUpLister.downLister();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (downUpLister != null) {
                    downUpLister.upLister();
                }
                break;
        }
        return true;
    }

    public DownUpLister downUpLister;

    public void setDownUpLister(DownUpLister downUpLister) {
        this.downUpLister = downUpLister;
    }

    public interface DownUpLister {
        void downLister();

        void upLister();
    }
}
