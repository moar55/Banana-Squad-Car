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

    private int z;
   private int x;
    private int y;
    private TextView directions;
    float [] gravity;
    float [] geomagnetic;
    MainActivity mainActivity;
    int lastSent ;


    public RotationListener(TextView directions, MainActivity mainActivity){
        this.directions=directions;
        this.mainActivity= mainActivity;
    }

    private static  float round (double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (float) Math.round(value * scale) / scale;
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
                SensorManager.getOrientation(R,orientation);
                z = (int)(Math.round(Math.toDegrees(-1 * orientation[0]))); // orientation contains: azimut, pitch and roll
                x =(int)(Math.round(Math.toDegrees(-1 * orientation[1])));
                y = (int)(Math.round(Math.toDegrees(orientation[2])));
                directions.setText("Z: "+z+"\n X: "+x+"\n Y: "+y);

                Log.v("Sensor","Success");

               if(x%5==0 && x!=lastSent) {
                   mainActivity.send(x);
                    lastSent=x;
               }

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
