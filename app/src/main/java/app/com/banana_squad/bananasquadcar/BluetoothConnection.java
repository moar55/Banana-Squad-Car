package app.com.banana_squad.bananasquadcar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

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
    private char state;
    private MainActivity mainActivity;
    private String enabled;
    private boolean connected = true;
    ManageConnection manageConnection;
    int success =0;

    public BluetoothConnection(char state, BluetoothAdapter myBluetoothAdapter, BluetoothDevice myBluetoothDevice, MainActivity mainActivity){
        this.state = state;
        this.myBluetoothAdapter=myBluetoothAdapter;
        this.myBluetoothDevice=myBluetoothDevice;
        this.mainActivity = mainActivity;

    }


    public void toast(final String str){
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.toast(str);
            }
        });
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
        mainActivity.test();
    }



     class ManageConnection extends  Thread {


         public ManageConnection() {


             try {
                 myInputStream = myBluetoothSocket.getInputStream();
                 myOutputStream = myBluetoothSocket.getOutputStream();

             } catch (IOException e) {
                 Log.e("Connection", "Problem with I/O streams: " + e.getMessage());
                 mainActivity.terminate();
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
                 mainActivity.begin();
             }


         }

     }


    public void closeAll(){
        try {
            myOutputStream.close();
            myInputStream.close();
            myBluetoothSocket.close();
        }
        catch (Exception e ){
            Log.e("Connection","Error closing sockets");
        }

    }


    public ManageConnection getManageConnection() {
        return manageConnection;
    }



}



