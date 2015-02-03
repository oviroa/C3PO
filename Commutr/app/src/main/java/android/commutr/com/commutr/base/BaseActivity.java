package android.commutr.com.commutr.base;

import android.commutr.com.commutr.R;
import android.commutr.com.commutr.managers.DataManager;
import android.commutr.com.commutr.utils.TypefaceSpan;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.SpannableString;

/**
 * Base activity, decorated with app specific fonts, icons
 * Other activities extend this
 */
public class BaseActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        displayIconInActionBar();
        decorateTitle();
    }

    private void decorateTitle(){

        SpannableString spannableString = new SpannableString(getTitle());
        spannableString.setSpan(new TypefaceSpan(this, "FuturaStd-Medium"), 0, spannableString.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        setTitle(spannableString);
    }

    private void displayIconInActionBar(){
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(false);
        ab.setHomeButtonEnabled(true);
        ab.setIcon(R.drawable.ic_launcher);
        ab.setDisplayShowHomeEnabled(true);
    }

    /**
     * Retrieve instance of proxy that handles all model operations
     * @return
     */
    public DataManager getDataManager(){
        return DataManager.getInstance();
    }

    /**
     * Enhances bade onPause with new transition (fade) that will be used by all activities
     */
    @Override
    protected void onPause()
    {
        super.onPause();

        //set default transition (fade)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

    }



}
