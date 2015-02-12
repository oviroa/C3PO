package android.commutr.com.commutr;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;


/**
 * A simple {@link DialogFragment} subclass.
 */
public class CheckInFragment extends DialogFragment {

    private AlertDialog alert;

    private boolean isShown = false;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_check_in, null));
        alert = builder.create();
        //alert.setCanceledOnTouchOutside(false);
        //setCancelable(false);
        return alert;
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        if (isShown) return;
        super.show(manager, tag);
        isShown = true;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        isShown = false;
        super.onDismiss(dialog);
    }

    public boolean isShown() {
        return isShown;
    }
}
