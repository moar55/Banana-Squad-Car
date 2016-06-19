package app.com.banana_squad.bananasquadcar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by delll on 6/19/2016.
 */
public class BluetoothConnection extends  Thread {


    private final  static String strUUID = "35fd5be3-c9bf-46e0-8ff8-438d28b6a33c";
    private BluetoothSocket myBluetoothSocket;
    private BluetoothAdapter myBluetoothAdapter;
    private BluetoothDevice myBluetoothDevice;
    private char state;
    private RcCar rcCar;

    public BluetoothConnection(char state, BluetoothAdapter myBluetoothAdapter, BluetoothDevice myBluetoothDevice, RcCar rcCar){
        this.state = state;
        this.myBluetoothAdapter=myBluetoothAdapter;
        this.myBluetoothDevice=myBluetoothDevice;
        this.rcCar = this.rcCar;


    }

    public void startConnection(){
        try{
            myBluetoothSocket=myBluetoothDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString(strUUID));
        }
        catch(IOException e){
            rcCar.toast("Something went wrong setting to the bluetooth socket");
        }

        myBluetoothAdapter.cancelDiscovery();

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            myBluetoothSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            try {
                myBluetoothSocket.close();
            } catch (IOException closeException) { }
            return;
        }

        // Do work to manage the connection (in a separate thread)
        new Thread{

        }

    }
}
