package app.com.banana_squad.bananasquadcar;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

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
    private BluetoothAdapter myBluetoothAdapter;
    private BluetoothDevice myBluetoothDevice;
    private final static int REQUEST_ENABLE_BT = 2;
    static char curentState;
    private String chosenDeviceName;
    private BluetoothConnection connection = null;
    boolean deviceChoosen = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mySensorManger = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accSensor = mySensorManger.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magSensor = mySensorManger.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mySensorListener = new RotationListener((TextView) findViewById(R.id.Direction), this);
        if (((TextView) findViewById(R.id.Direction)) != null)
            Log.v("Text", "IT is not null!!");
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        start = (Button) findViewById(R.id.StartButton);
        reverse = (Button) findViewById(R.id.ReverseButton);
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(myBroadcastReceiver,filter);
        start.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Drawable background;

                if (reverse.getVisibility() == View.INVISIBLE) {
                    background = ContextCompat.getDrawable(getBaseContext(), R.drawable.round_button_selected);
                    start.setBackground(background);
                    start.setText("Stop");
                    reverse.setVisibility(View.VISIBLE);
                    initiateBluetoothConnection('T');


                } else {
                    start.setText("Start");
                    background = ContextCompat.getDrawable(getBaseContext(), R.drawable.round_button);
                    start.setBackground(background);
                    reverse.setVisibility(View.INVISIBLE);
                    mySensorManger.unregisterListener(mySensorListener);
                    connection.closeAll();

                }
            }
        });

        reverse.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                send(3);
            }
        });


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    //Set the listeners for senseor and begin the fun!
    public void begin() {
        mySensorManger.registerListener(mySensorListener, accSensor, SensorManager.SENSOR_DELAY_UI);
        mySensorManger.registerListener(mySensorListener, magSensor, SensorManager.SENSOR_DELAY_UI);
    }

    public void test() {
        connection.getManageConnection().send(9);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mySensorManger.unregisterListener(mySensorListener);
    }



    @Override
    protected void onResume() {
        super.onResume();
        //Start is pressed
        if (reverse.getVisibility() != View.INVISIBLE) {
            mySensorManger.registerListener(mySensorListener, accSensor, SensorManager.SENSOR_DELAY_UI);
            mySensorManger.registerListener(mySensorListener, magSensor, SensorManager.SENSOR_DELAY_UI);
            initiateBluetoothConnection(curentState);
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

    public void initiateBluetoothConnection(char c) {

        if (!myBluetoothAdapter.isEnabled())
            myBluetoothAdapter.enable();


        else
            showBtDevices();


    }

    public void showBtDevices()  {


        Set<BluetoothDevice> pairedDevices = myBluetoothAdapter.getBondedDevices();
        ArrayList<String> devices = new ArrayList<String>();
// If there are paired devices


        if (pairedDevices.size() > 0) {
            Log.d("Connection", "There are paired devices");
            // Loop through paired devices

            for (BluetoothDevice device : pairedDevices) {
                devices.add(device.getName() + "\n" + device.getAddress());

            }
        }

        final AlertDialog.Builder myBuilder = new AlertDialog.Builder(this);
        myBuilder.setTitle("Paired Devices");
        devices.add("Not a paired device ? Pair and re-launch the application");


        final CharSequence[] listOfDevices = devices.toArray(new CharSequence[devices.size()]);
        myBuilder.setItems(listOfDevices, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int option) {
                Log.v("Choice", listOfDevices[option].toString());
                if (listOfDevices[option].toString().equals("Not a paired device ? Pair and re-launch the application")) {
                    dialog.dismiss();
                    openBluetoothDeviceChooser();

                }

                else {
                    String splittedName[] = listOfDevices[option].toString().split("\n");
                    chosenDeviceName = splittedName[0];
                    Log.v("Connetion", chosenDeviceName);
                    dialog.dismiss();
                    initiateBluetoothConnection2('C');
                }


            }
        });
        myBuilder.show();


    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myBroadcastReceiver);
        connection.closeAll();
    }

    public void setBluetoothDevice() {
        Set<BluetoothDevice> pairedDevices = myBluetoothAdapter.getBondedDevices();


        Log.v("Connetion", chosenDeviceName);

        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().equals(chosenDeviceName)) {
                myBluetoothDevice = device;
                break;
            }
        }

        if (myBluetoothDevice == null)
            toast("Cannot set device for connection check your connection or call a developer ");
    }

    public void initiateBluetoothConnection2(char c) {
        setBluetoothDevice();

        Log.v("Connection", "Started");


        if (myBluetoothDevice != null) {
            Log.v("Connection WORKED :O ", "Started");

            try {
                connection = new BluetoothConnection(c, myBluetoothAdapter, myBluetoothDevice, this);
                connection.start();
            } catch (Exception e) {
                Log.e("Oh no!", e.getMessage());
            }


        } else {
            toast("Please Pair with the bluetooth module and try again");


        }

    }

//    public void test(){
//        connection.getManageConnection().send(2);
//
//    }




    public void terminate() {

    }

    public void openBluetoothDeviceChooser() {


        Log.v("Pair", "Here");
        Intent myIntent = new Intent("com.android.bluetooth");
        try {
            startActivityForResult(myIntent, 2);
        } catch (Exception e) {
            Log.e("yo", e.getMessage());
            toast("Where is the bluetooth Application! :O ");
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
        connection.closeAll();
        unregisterReceiver(myBroadcastReceiver);


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


    private final BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);


                switch (state) {
                    case BluetoothAdapter.STATE_TURNING_ON: toast("Enabling Bluetooth");break;
                    case BluetoothAdapter.STATE_ON:  initiateBluetoothConnection('2');break;
                    case BluetoothAdapter.STATE_CONNECTED: toast("Connected");break;
                    case BluetoothAdapter.STATE_DISCONNECTED: toast("Connection Lost");break;
                    case BluetoothAdapter.STATE_OFF: toast("Bluetooth is turned off please enable it and try again")
                       ;break;

                }

            }
        }
    };
}