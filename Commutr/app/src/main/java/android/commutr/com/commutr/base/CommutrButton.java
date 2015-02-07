package android.commutr.com.commutr.base;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Created by oviroa on 1/28/15.
 */
public class CommutrButton extends Button {

    public CommutrButton(Context context) {
        super(context);
        init();
    }

    public CommutrButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CommutrButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                "fonts/FuturaStd-Medium.ttf");
        setTypeface(tf);
    }
}
