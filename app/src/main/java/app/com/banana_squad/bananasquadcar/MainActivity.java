package app.com.banana_squad.bananasquadcar;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

    public class MainActivity extends AppCompatActivity implements RotationDrive {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private SensorManager mySensorManger;
    private Button start;
    private Button reverse;
    Sensor accSensor;
    Sensor magSensor;
    RotationListener mySensorListener;
    private BluetoothConnection connection;
    private Button drawPath;
    private Button voiceControl;
    MainActivity activity ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mySensorManger = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accSensor = mySensorManger.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magSensor = mySensorManger.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        setContentView(R.layout.activity_main);
        mySensorListener = new RotationListener((TextView) findViewById(R.id.Direction), this);
        if (((TextView) findViewById(R.id.Direction)) != null)
            Log.v("Text", "IT is not null!!");


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        activity=this;
        connection = new BluetoothConnection(activity);
        connection.initiateBluetoothConnection();
        start = (Button) findViewById(R.id.StartButton);
        reverse = (Button) findViewById(R.id.ReverseButton);

        voiceControl=(Button)findViewById(R.id.voice_control);
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);


        drawPath=(Button)findViewById(R.id.drawPath);

        drawPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent(getApplicationContext(),CreatePath.class);
                startActivity(mIntent);
            }
        });


        voiceControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent someIntent = new Intent(getApplicationContext(),VoiceControl.class);
                startActivity(someIntent);
            }
        });


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


        public void setListeners(){
            start.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Drawable background;

                    if (reverse.getVisibility() == View.INVISIBLE) {
                        background = ContextCompat.getDrawable(getBaseContext(), R.drawable.round_button_selected);
                        start.setBackground(background);
                        start.setText("Stop");
                        mySensorManger.registerListener(mySensorListener, accSensor, SensorManager.SENSOR_DELAY_UI);
                        mySensorManger.registerListener(mySensorListener, magSensor, SensorManager.SENSOR_DELAY_UI);
                        connection.getManageConnection().send('f');
                        reverse.setVisibility(View.VISIBLE);
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        drawPath.setVisibility(View.INVISIBLE);
                        voiceControl.setVisibility(View.INVISIBLE);

                    }

                    else {
                        send('s');
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        start.setText("Start");
                        background = ContextCompat.getDrawable(getBaseContext(), R.drawable.round_button);
                        start.setBackground(background);
                        reverse.setVisibility(View.INVISIBLE);
                        mySensorManger.unregisterListener(mySensorListener);

                        drawPath.setVisibility(View.VISIBLE);
                        voiceControl.setVisibility(View.VISIBLE);

                    }
                }
            });


            reverse.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    if(event.getAction()==MotionEvent.ACTION_DOWN)
                        send('b');

                    else if(event.getAction()==MotionEvent.ACTION_UP)
                        send('f');

                    return true;
                }
            });
        }


    //Set the listeners for senseor and begin the fun!
    public void begin() {
//

    }

    public void test() {
        connection.getManageConnection().send('f');
    }

    @Override
    protected void onPause() {
        super.onPause();
        send('s');
        try{
        connection.closeAll();
    }
        catch (Exception e) {
            ;
        }
        mySensorManger.unregisterListener(mySensorListener);
    }



    @Override
    protected void onResume() {
        super.onResume();
//        Start is pressed
        if (reverse.getVisibility() == View.VISIBLE) {
            connection.registerReceiver();
            mySensorManger.registerListener(mySensorListener, accSensor, SensorManager.SENSOR_DELAY_UI);
            mySensorManger.registerListener(mySensorListener, magSensor, SensorManager.SENSOR_DELAY_UI);
            connection.initiateBluetoothConnection();
        }

    }

    public void send(int i) {
        connection.getManageConnection().send(i);
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




    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            connection.closeAll();
        }
        catch (Exception e){
            Log.e("Destroy","Error in closing stuff");
        }
    }




    public void toast(String str) {
        Toast toast = Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onStart() {

        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://app.com.banana_squad.bananasquadcar/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        send('s');
        try {
            connection.closeAll();
        }

        catch (Exception e){
            ;
        }



        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://app.com.banana_squad.bananasquadcar/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    public BluetoothConnection getConnection() {
        return connection;
    }



}