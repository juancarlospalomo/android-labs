package com.applilandia.alertdialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by JuanCarlos on 14/03/2015.
 */
public class AlertDialog extends DialogFragment {

    private final static String LOG_TAG = AlertDialog.class.getSimpleName();

    //Arguments Keys
    private final static String KEY_TITLE = "key_title";
    private final static String KEY_CONTENT = "key_content";
    private final static String KEY_CANCEL_TEXT = "key_cancel_text";
    private final static String KEY_OK_TEXT = "key_ok_text";
    //Buttons index
    public final static int INDEX_BUTTON_YES = -1;
    public final static int INDEX_BUTTON_NO = -2;

    private String mTitle;
    private String mContent;
    private String mCancelText;
    private String mOkText;

    private DialogInterface.OnClickListener mOnClickListener;

    /**
     * Instance a Dialog with the arguments
     *
     * @param title
     * @param content
     * @param cancelText
     * @param okText
     * @return
     */
    public static AlertDialog newInstance(String title, String content, String cancelText, String okText) {
        AlertDialog alertDialog = new AlertDialog();
        Bundle args = new Bundle();
        args.putString(KEY_TITLE, title);
        args.putString(KEY_CONTENT, content);
        args.putString(KEY_CANCEL_TEXT, cancelText);
        args.putString(KEY_OK_TEXT, okText);
        alertDialog.setArguments(args);
        return alertDialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        loadArguments();
        return new android.app.AlertDialog.Builder(getActivity())
                .setTitle(mTitle)
                .setNegativeButton(mCancelText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.v(LOG_TAG, "Negative Button: " + which);
                        if (mOnClickListener != null) {
                            mOnClickListener.onClick(dialog, which);
                        }
                    }
                })
                .setPositiveButton(mOkText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.v(LOG_TAG, "Positive Button: " + which);
                        if (mOnClickListener != null) {
                            mOnClickListener.onClick(dialog, which);
                        }
                    }
                })
                .create();
    }

    public void setButtonOnClickListener(DialogInterface.OnClickListener l) {
        mOnClickListener = l;
    }

    private void loadArguments() {
        Bundle args = getArguments();
        if (args != null) {
            mTitle = args.getString(KEY_TITLE);
            mContent = args.getString(KEY_CONTENT);
            mCancelText = args.getString(KEY_CANCEL_TEXT);
            mOkText = args.getString(KEY_OK_TEXT);
        }
    }

}
