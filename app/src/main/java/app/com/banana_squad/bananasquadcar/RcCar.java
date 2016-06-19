package app.com.banana_squad.bananasquadcar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Set;

public class RcCar extends AppCompatActivity {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private SensorManager mySensorManger;
    private Button start ;
    private Button reverse ;
    Sensor accSensor;
    Sensor magSensor;
    RotationListener mySensorListener;
    private BluetoothAdapter myBluetoothAdapter;
    private BluetoothDevice myBluetoothDevice;
    private final static int REQUEST_ENABLE_BT = 2;
    static char curentState;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        mySensorManger=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
        accSensor= mySensorManger.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magSensor= mySensorManger.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mySensorListener=new RotationListener((TextView)findViewById(R.id.Direction)) ;
        if(((TextView)findViewById(R.id.Direction))!=null)
        Log.v("Text","IT is not null!!");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        start = (Button)findViewById(R.id.StartButton);
        reverse =(Button)findViewById(R.id.ReverseButton);

        start.setOnClickListener(new View.OnClickListener(){

                                     @Override
                                     public void onClick(View v) {
                                         Drawable background;

                                         if(reverse.getVisibility()==View.INVISIBLE) {
                                            background=ContextCompat.getDrawable(getBaseContext(),R.drawable.round_button_selected);
                                             start.setBackground(background);
                                             start.setText("Stop");
                                             reverse.setVisibility(View.VISIBLE);
                                             mySensorManger.registerListener(mySensorListener, accSensor,SensorManager.SENSOR_DELAY_UI);
                                             mySensorManger.registerListener(mySensorListener, magSensor,SensorManager.SENSOR_DELAY_UI);
                                             initiateBluetoothConnection('T');

                                             }
                                         else {
                                             start.setText("Start");
                                             background =ContextCompat.getDrawable(getBaseContext(),R.drawable.round_button);
                                             start.setBackground(background);
                                             reverse.setVisibility(View.INVISIBLE);
                                             mySensorManger.unregisterListener(mySensorListener);

                                         }
                                     }
                                 });



        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    @Override
    protected void onPause() {
        super.onPause();
        mySensorManger.unregisterListener(mySensorListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(reverse.getVisibility()!=View.INVISIBLE){
            mySensorManger.registerListener(mySensorListener, accSensor,SensorManager.SENSOR_DELAY_UI);
            mySensorManger.registerListener(mySensorListener, magSensor,SensorManager.SENSOR_DELAY_UI);
            initiateBluetoothConnection(curentState);
        }

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

    public void initiateBluetoothConnection(char c){
        if (!myBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        Set<BluetoothDevice> pairedDevices = myBluetoothAdapter.getBondedDevices();
// If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                    if(device.getName().equals("BQ_BT"))
                    {
                        myBluetoothDevice=device;
                        break;
                    }
                }

            if(myBluetoothDevice!=null){

              BluetoothConnection connection=  new BluetoothConnection(c,myBluetoothAdapter,myBluetoothDevice,this);
                connection.startConnection();
                getApplicationContext();
            }



        }


    }

    public void toast(String str){
        Toast toast = Toast.makeText(getApplicationContext(),str,Toast.LENGTH_SHORT);
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
}
