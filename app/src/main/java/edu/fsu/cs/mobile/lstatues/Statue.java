package edu.fsu.cs.mobile.lstatues;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by robert on 7/17/17.
 */

public class Statue extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statue);
/*
        TextView name = (TextView) findViewById(R.id.editName);
        TextView lat = (TextView) findViewById(R.id.editLat);
        TextView lon = (TextView) findViewById(R.id.editLon);
        TextView des = (TextView) findViewById(R.id.editDes);

        name.setText(savedInstanceState.getString("name"));
        lat.setText(savedInstanceState.getString("lat"));
        lon.setText(savedInstanceState.getString("lon"));
        des.setText(savedInstanceState.getString("des"));

*/
    }
    @Override
    public void onNewIntent(Intent newIntent) {
        TextView name = (TextView) findViewById(R.id.editName);
        TextView lat = (TextView) findViewById(R.id.editLat);
        TextView lon = (TextView) findViewById(R.id.editLon);
        TextView des = (TextView) findViewById(R.id.editDes);

        name.setText(newIntent.getStringExtra("name"));
        lat.setText(newIntent.getStringExtra("lat"));
        lon.setText(newIntent.getStringExtra("lon"));
        des.setText(newIntent.getStringExtra("des"));

    }
}
