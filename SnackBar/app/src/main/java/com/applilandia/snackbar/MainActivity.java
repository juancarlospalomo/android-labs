package com.applilandia.snackbar;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class MainActivity extends ActionBarActivity {

    private final static String LOG_TAG = MainActivity.class.getSimpleName();

    private RecyclerView mTaskRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTaskRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewTasks);
        //Change in content will not change the layout size of the recycler view
        //Of this way, we improve the performance
        mTaskRecyclerView.setHasFixedSize(true);
        //It will use a LinearLayout
        mLayoutManager = new LinearLayoutManager(this);
        mTaskRecyclerView.setLayoutManager(mLayoutManager);

        mTaskRecyclerView.addOnItemTouchListener(new RecyclerViewClickListener(this, new RecyclerViewClickListener.RecyclerViewOnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemSecondaryActionClick(View view, int position) {
                        SnackBar snackBar = (SnackBar) findViewById(R.id.snackBarTasks);
                        snackBar.setOnSnackBarListener(new SnackBar.OnSnackBarListener() {
                            @Override
                            public void onClose() {
                                Log.v(LOG_TAG, "onClose");
                            }

                            @Override
                            public void onUndo() {
                                Log.v(LOG_TAG, "onUndo");
                            }
                        });
                        snackBar.show(R.string.snack_bar_task_completed_text);
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {

                    }
                }));


//        Button button = (Button) findViewById(R.id.buttonSnackBar);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                SnackBar snackBar = (SnackBar) findViewById(R.id.snackBarTasks);
//                snackBar.setOnSnackBarListener(new SnackBar.OnSnackBarListener() {
//                    @Override
//                    public void onClose() {
//                        Log.v(LOG_TAG, "onClose");
//                    }
//
//                    @Override
//                    public void onUndo() {
//                        Log.v(LOG_TAG, "onUndo");
//                    }
//                });
//                snackBar.show(R.string.snack_bar_task_completed_text);
//            }
//        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
