package app.com.banana_squad.bananasquadcar;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by delll on 6/17/2016.
 */
public class RotationListener implements SensorEventListener {

    private float z;
   private float x;
    private float y;
    private TextView directions;
    float [] gravity;
    float [] geomagnetic;


    public RotationListener(TextView directions){
        this.directions=directions;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.v("Sensor","Entered");

        if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER)
            gravity=event.values;

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            geomagnetic = event.values;

        if (gravity != null && geomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);

            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                z = -1* orientation[0]; // orientation contains: azimut, pitch and roll
                x = -1 * orientation[1];
                y = orientation[2];
                directions.setText("Z: "+z+"\n X: "+x+"\n Y: "+y);
                Log.v("Sensor","Success");
            }
            else
                Log.e("Sensor","Failure!");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public float getZ() {
        return z;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
