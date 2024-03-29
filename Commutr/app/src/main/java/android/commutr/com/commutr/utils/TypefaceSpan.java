package android.commutr.com.commutr.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;
import android.support.v4.util.LruCache;

/**
 * Style a Spannable} with a custom {@link Typeface}.
 *
 * @author Tristan Waddington
 */
public class TypefaceSpan extends MetricAffectingSpan {
    /** An <code>LruCache</code> for previously loaded typefaces. */
    private static LruCache<String, Typeface> sTypefaceCache =
            new LruCache<String, Typeface>(12);
    private Typeface mTypeface;

    /**
     * Load the {@link Typeface} and apply to a Spannable.
     */
    public TypefaceSpan(Context context, String typefaceName) {
        mTypeface = sTypefaceCache.get(typefaceName);
        if (mTypeface == null)
        {
            mTypeface = Typeface.createFromAsset(context.getApplicationContext()
                    .getAssets(), String.format("fonts/%s.ttf", typefaceName));
            // Cache the loaded Typeface

            sTypefaceCache.put(typefaceName, mTypeface);
        }
    }

    @Override
    public void updateMeasureState(TextPaint p)
    {
        p.setTypeface(mTypeface);
    }

    @Override
    public void updateDrawState(TextPaint tp)
    {
        tp.setTypeface(mTypeface);
    }
}
