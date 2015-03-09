package com.applilandia.errorview;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;

import com.applilandia.widget.ValidationField;


public class MainActivity extends ActionBarActivity {

    private Button mButton;
    private ValidationField mValidationField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton = (Button) findViewById(R.id.button1);
        mValidationField = (ValidationField) findViewById(R.id.fieldTaskName);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mValidationField.setError("Cannot be empty");
                ValidationField validationField = (ValidationField) findViewById(R.id.fieldTaskName1);
                validationField.setText(mValidationField.getText());
            }
        });

        mValidationField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mValidationField.removeError();
            }
        });

    }



}
