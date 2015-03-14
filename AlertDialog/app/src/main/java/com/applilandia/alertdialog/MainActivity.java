package com.applilandia.alertdialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends ActionBarActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonShow = (Button) findViewById(R.id.buttonShow);
        buttonShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = AlertDialog.newInstance(getString(R.string.delete_dialog_tile),
                        "", getString(R.string.delete_dialog_cancel_text),
                        getString(R.string.delete_dialog_ok_text));
                alertDialog.setButtonOnClickListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.v(LOG_TAG, dialog.toString());
                        Log.v(LOG_TAG, String.valueOf(which == AlertDialog.INDEX_BUTTON_YES));
                    }
                });
                alertDialog.show(getFragmentManager(), "dialog");
            }
        });

    }

}
