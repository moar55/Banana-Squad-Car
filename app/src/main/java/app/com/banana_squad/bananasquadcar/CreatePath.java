package app.com.banana_squad.bananasquadcar;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;

public class CreatePath extends Activity {



    float gradient ;
    float firstGradient=0;

    Draw draw;
    BluetoothConnection connection;


    final float inchToCm = 2.54f;
    Coordinates lastDrawn;
    ArrayList<Coordinates> coordinates; //ArrayList of coordinates in cm
    Coordinates initialPosition, finalPosition;
    Coordinates point=new Coordinates(0,0);
    int straightCount;
    Coordinates curved_FirstPoint;
    Coordinates curved_LastPoint;
    Coordinates curved_MiddlePoint;
    int index_FirstCurved;

    ArrayList<OrderForATime> toBeSent = new ArrayList<OrderForATime>();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Display display= getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height= size.y;
        super.onCreate(savedInstanceState);
        draw = new Draw(this,width,height,this);
        setContentView(draw);
        final AlertDialog.Builder alertDialog  = new android.support.v7.app.AlertDialog.Builder(this);
        connection = new BluetoothConnection(this);
        connection.initiateBluetoothConnection();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        draw.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int eventAction = event.getAction();
                final float divisor = getResources().getDisplayMetrics().density * 160f; //Approximate PPI value of phone
                switch (eventAction & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        coordinates = new ArrayList<Coordinates>();
                        initialPosition=new Coordinates((event.getX() / divisor) * inchToCm  ,  -1 * (event.getY() / divisor) * inchToCm);
//                        coordinates.add(initialPosition);
                    lastDrawn=initialPosition;

                        draw.path.moveTo(event.getX(), event.getY());
                        Log.v("TOUCHED", "YOU supposedly touched the screen");
                        break;

                    case MotionEvent.ACTION_MOVE:
                        float xCm= (event.getX() / divisor) * inchToCm ;
                        float yCm =  -1 *(event.getY() / divisor) * inchToCm;


                        Log.v("Coordinate ",event.getX()+"f  " +event.getY()+"");
//                        Log.v("value",(Math.floor((double)((event.getX() / divisor) * inchToCm  *10))/10)+"");
//                        Log.v("Size of x and y",Math.abs(lastDrawn.x-xCm)+ " "+ Math.abs(lastDrawn.y-yCm));

                        float resultant = (float)Math.sqrt(Math.pow(lastDrawn.y-yCm,2) + Math.pow(lastDrawn.x-xCm,2));

                        Log.v("Resultant",resultant + "efefe");
                        if(resultant>=0.9 && resultant<=1) {
                            Log.v("Condition","It is in");
                            coordinates.add(new Coordinates(xCm, yCm));
                            lastDrawn=new Coordinates(xCm,yCm);
                        }

                        Log.v("Touch", "Moving");
                        draw.path.lineTo(event.getX(), event.getY());

                        break;

                    case MotionEvent.ACTION_UP: {
                        coordinates.add(new Coordinates((event.getX() / divisor) * inchToCm, -1 * (event.getY() / divisor) * inchToCm));

                       Log.v("Position", "It is " + (event.getX() / divisor) * inchToCm);
                        draw.path.lineTo(event.getX(), event.getY());
                        alertDialog.setTitle("Use Pattern ");
                        alertDialog.setMessage("Do you want to use this pattern?");

                        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                               Toast.makeText(getApplicationContext(),"Working on it...",Toast.LENGTH_SHORT).show();
                                print();
                                processTheData();


                            }
                        });

                        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                draw.clear=true;
                                draw.invalidate();
                                dialog.dismiss();
                            }
                        });

                        alertDialog.show();
                        break;
                    }

                }
                draw.invalidate();
                return true;
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        connection.getManageConnection().send('s');
        connection.closeAll();
    }

    @Override
    protected void onPause() {
        super.onPause();
        connection.getManageConnection().send('s');
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

    @Override
    protected void onResume() {
        super.onResume();
        connection.registerReceiver();
        connection.initiateBluetoothConnection();
    }

    public void sendThePath() throws InterruptedException {
        ArrayList temp = toBeSent;

        connection.getManageConnection().send('f');


        Thread send  =new Thread(new Runnable() {
            @Override
            public void run() {
                for(final OrderForATime order: toBeSent){


                    if(order.pathType==PathType.Curved){
                        connection.getManageConnection().send((int)(Math.floor(order.angle)));
                        Log.v("yo",order.length+ "true");

                    }

                    else
                    connection.getManageConnection().send(0);


                    try{
                        Thread.sleep((long)(Math.round(order.length*3*1000)));
                    }
                    catch (Exception e){
                        Log.e("Sleep",e.getMessage());
                    }
                    }

                connection.getManageConnection().send('s');




            }
        });

        send.start();




        Log.v("Stop","true");

    }

    public void wait(final float f,final char c){
        Runnable toRunInBackground = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep((long)(Math.round(f)));

                    if(c=='c')
                    connection.getManageConnection().send((int)(Math.floor(f/3000.0)));

                    else if(c=='f')
                        connection.getManageConnection().send('f');

                    else
                        connection.getManageConnection().send('s');
                }
                catch (Exception e){
                    Log.e("Sleep",e.getMessage());
                }
            }
        };
        new Thread(toRunInBackground).start();
    }


    public void processTheData(){

        curved_FirstPoint=null;
        curved_LastPoint=null;

        toBeSent=new ArrayList<OrderForATime>();

        draw.drawPoint=true;

        for (int i=0;i<coordinates.size()-1;i++) {


            float dy = coordinates.get(i+1).getY()-coordinates.get(i).getY();
            float dx = coordinates.get(i+1).getX()-coordinates.get(i).getX();

            if(Math.abs(dy)<=0.2 || Math.abs(dx)<=0.2)
                toBeSent.add(new OrderForATime(PathType.Straight,(float)Math.sqrt(Math.pow(dx,2)+ Math.pow(dy,2))));

            else
                toBeSent.add(new OrderForATime(PathType.Curved,(float)Math.atan(dy/dx)*45   ,(float)Math.sqrt(Math.pow(dx,2)+ Math.pow(dy,2))));



        }

        printToBeSent();

        try {
            sendThePath();
        }

        catch (Exception e){
            Log.e("Sending",e.getMessage());
        }
    }




    public void printToBeSent(){
        for (OrderForATime order :toBeSent ) {
            if(order.pathType==PathType.Straight)
            Log.v("ToBeSent", order.pathType.toString());

            else
                Log.v("ToBeSent",order.pathType.toString() + " "+" " + order.angle+" "+order.lastPoint);
        }
    }
    public void print(){
        Log.v("Coordinates array size",""+coordinates.size());
        for(Coordinates point : coordinates)
            Log.v("Points",point.toString());

    }}


    class OrderForATime{

        PathType pathType;
        float angle;
        float length;
        Coordinates lastPoint;

        public OrderForATime(PathType pathType,float length){
            this.pathType=pathType;
            this.length=length;


        }

        public OrderForATime(PathType pathType,float angle,float length){
            this.pathType=pathType;
            this.angle=angle;
            this.length =length;


        }
    }

    class Coordinates {
        float x, y;

        public Coordinates(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public String toString(){
            String output = "(" +x +","+ y+ ")";
            return output;
        }
    }