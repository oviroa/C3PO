package android.commutr.com.commutr.base;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by oviroa on 1/26/15.
 */

public class CommutrTextView extends TextView
{
    public CommutrTextView(Context context)
    {
        super(context);
        init();
    }

    public CommutrTextView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public CommutrTextView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init();
    }

    public void init()
    {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                "fonts/FuturaStd-Medium.ttf");
        setTypeface(tf);
    }
}

