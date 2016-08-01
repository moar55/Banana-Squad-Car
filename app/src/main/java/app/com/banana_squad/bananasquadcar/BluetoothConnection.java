package app.com.banana_squad.bananasquadcar;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

/**
 * Created by delll on 6/19/2016.
 */

public class BluetoothConnection extends  Thread {

    private OutputStream myOutputStream ;
    private InputStream myInputStream;
    private final  static String strUUID = "e45ac435-602b-445c-9caa-91ca078dd431";
    private BluetoothSocket myBluetoothSocket;
    private BluetoothAdapter myBluetoothAdapter;
    private BluetoothDevice myBluetoothDevice;
    private boolean connected = true;
    ManageConnection manageConnection;
    int success =0;
    String chosenDeviceName;
    Activity activity;
    IntentFilter filter ;



    public BluetoothConnection(Activity activity){

        myBluetoothAdapter= BluetoothAdapter.getDefaultAdapter();

        this.activity=activity;

        filter=new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver();

    }

    public void registerReceiver(){
        activity.registerReceiver(myBroadcastReceiver,filter);
    }


    public void initiateBluetoothConnection(){

        myBluetoothAdapter.enable();
        success=0;

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

        final AlertDialog.Builder myBuilder = new AlertDialog.Builder(activity);
        myBuilder.setTitle("Paired Devices");
        devices.add("Not a paired device ? Pair and re-launch the application");


        final CharSequence[] listOfDevices = devices.toArray(new CharSequence[devices.size()]);
        myBuilder.setItems(listOfDevices, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int option) {
                Log.v("Choice", listOfDevices[option].toString());
                if (listOfDevices[option].toString().equals("Not a paired device ? Pair and re-launch the application")) {
                    dialog.dismiss();
//                    openBluetoothDeviceChooser();

                }

                else {
                    String splittedName[] = listOfDevices[option].toString().split("\n");
                    chosenDeviceName = splittedName[0];
                    Log.v("Connetion", chosenDeviceName);
                    dialog.dismiss();
                    initiateBluetoothConnection2();
                }


            }
        });
        myBuilder.show();


    }

    public void toast(final String str) {

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast toast = Toast.makeText(activity.getApplicationContext(), str, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });


    }

    public void initiateBluetoothConnection2() {
        setBluetoothDevice();

        Log.v("Connection", "Started");


        if (myBluetoothDevice != null) {
            Log.v("Connection WORKED :O ", "Started");

            try {

                start();
            } catch (Exception e) {
                Log.e("Oh no!", e.getMessage());
            }


        } else {
            toast("Please Pair with the bluetooth module and try again");


        }

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







    public void run() {

//        try {
//
//            myBluetoothSocket = myBluetoothDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString(strUUID));
//        } catch (IOException e) {
//            connected = false;
//            Log.e("Connection", "Something went wrong");
//            toast("Something went wrong setting to the bluetooth socket");
//        }
//
//        Log.v("Connection","The bluetooth device is: "+myBluetoothDevice.getName());

        Method m;
        try {
            m = myBluetoothDevice.getClass().getMethod("createInsecureRfcommSocket", new Class[]{int.class});
            myBluetoothSocket = (BluetoothSocket) m.invoke(myBluetoothDevice, 1);

        }
        catch (Exception e) {
            connected = false;
            Log.e("Connection", "Some ERROR: "+ e.getMessage());
        }
        myBluetoothAdapter.cancelDiscovery();


            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                myBluetoothSocket.connect();
                }
            catch (IOException connectException) {
                connected = false;
                connectException.printStackTrace();
                toast("Failed to connect to bluteooth module restart the app and try again");
                // Unable to connect; close the socket and get out
                try {
                    myBluetoothSocket.close();
            } catch (IOException closeException) {
                return;
            }

        }

        manageConnection = new ManageConnection();
        manageConnection.start();

       getManageConnection().send('z');

        if(activity instanceof  RotationDrive)
            ((RotationDrive) activity).setListeners();
    }





    // BT Transfer

     class ManageConnection extends  Thread {


         public ManageConnection() {


             try {
                 myInputStream = myBluetoothSocket.getInputStream();
                 myOutputStream = myBluetoothSocket.getOutputStream();

             } catch (IOException e) {
                 Log.e("Connection", "Problem with I/O streams: " + e.getMessage());


             }
         }
         @Override
         public void run() {

             }

         public void send (int i){

             boolean sent = true;
             Log.v("Connection","In the send Method");
             Log.v("Connection","Character to be sent is: "+ i);
             try{
                 myOutputStream.write(i);
             }
             catch (Exception e){
                 sent=false;
                 Log.e("Connection","error sending character "+ e.getMessage());
             }


             if(sent)
                 Log.v("Connection","No errors sending character");

             if(sent && success==0) {
                 success++;
                 toast("Connection Established");
                 if(activity instanceof RotationDrive)
                     ((RotationDrive)( activity)).begin();
             }


         }

     }


    public void closeAll(){
        try {
            myOutputStream.close();
            myInputStream.close();
            myBluetoothSocket.close();
            activity.unregisterReceiver(myBroadcastReceiver);
            myBluetoothAdapter.disable();
        }
        catch (Exception e ){
            Log.e("Connection","Error closing sockets");
        }

    }


    public ManageConnection getManageConnection() {
        return manageConnection;
    }



    private final BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);


                switch (state) {
                    case BluetoothAdapter.STATE_TURNING_ON: toast("Enabling Bluetooth");break;
                    case BluetoothAdapter.STATE_ON: Log.v("Connection","ON");showBtDevices();break;
                    case BluetoothAdapter.STATE_CONNECTED: toast("Connected");break;
                    case BluetoothAdapter.STATE_DISCONNECTED: toast("Connection Lost");initiateBluetoothConnection2();break;
                    case BluetoothAdapter.STATE_OFF: toast("Bluetooth is turned off please enable it and try again")
                    ;break;

                }

            }
        }
    };


}



