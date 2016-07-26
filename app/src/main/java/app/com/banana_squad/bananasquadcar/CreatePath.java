package app.com.banana_squad.bananasquadcar;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

public class CreatePath extends Activity {

    MainActivity activity;

    public CreatePath(){
        this.activity=activity;
    }
    float gradient ;
    float firstGradient=0;
    ArrayList<Coordinates> coordinates; //ArrayList of coordinates in cm
    Coordinates initialPosition, finalPosition;


    ImageView drawPathView;
    final float inchToCm = 2.54f;
    Coordinates lastDrawn;
    Bitmap bitmap;
    Canvas canvas;
    Draw draw;
    int straightCount;
    Coordinates curved_FirstPoint;
    Coordinates curved_LastPoint;

    ArrayList<OrderForATime> toBeSent = new ArrayList<OrderForATime>();


    class OrderForATime{

        PathType pathType;
        int count;
        float angle;
        float arcLength;
        CurveDirection curveDirection;


        public OrderForATime(PathType pathType,int count){
            this.pathType=pathType;
            this.count=count;


        }

        public OrderForATime(PathType pathType,float angle,float arcLength){
            this.pathType=pathType;
            this.angle=angle;
            this.arcLength=arcLength;
            this.curveDirection=curveDirection;
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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Display display= getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height= size.y;
        super.onCreate(savedInstanceState);
        draw = new Draw(this,width,height);
        FrameLayout x = new FrameLayout(this);

        setContentView(draw);
        final AlertDialog.Builder alertDialog  = new android.support.v7.app.AlertDialog.Builder(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);






        draw.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int eventAction = event.getAction();
                final float divisor = getResources().getDisplayMetrics().density * 160f; //Approximate PPI value of phone
                switch (eventAction & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        coordinates = new ArrayList<Coordinates>();
                        initialPosition=new Coordinates((event.getX() / divisor) * inchToCm  ,(event.getY() / divisor) * inchToCm);
//                        coordinates.add(initialPosition);
                    lastDrawn=initialPosition;

                        draw.path.moveTo(event.getX(), event.getY());
                        Log.v("TOUCHED", "YOU supposedly touched the screen");
                        break;

                    case MotionEvent.ACTION_MOVE:
                        float xCm= (event.getX() / divisor) * inchToCm ;
                        float yCm = (event.getY() / divisor) * inchToCm;

//                        Log.v("value",(Math.floor((double)((event.getX() / divisor) * inchToCm  *10))/10)+"");
                        Log.v("Size of x and y",Math.abs(lastDrawn.x-xCm)+ " "+ Math.abs(lastDrawn.y-yCm));


                        if((Math.abs(lastDrawn.x-xCm)>=0.40 && Math.abs(lastDrawn.x-xCm)<=0.55)|| (Math.abs(lastDrawn.y-yCm)>=0.40 && Math.abs(lastDrawn.y-yCm)<=0.55)) {
                            Log.v("Condition","It is in");
                            coordinates.add(new Coordinates(xCm, yCm));
                            lastDrawn=new Coordinates(xCm,yCm);
                        }

                        Log.v("Touch", "Moving");
                        draw.path.lineTo(event.getX(), event.getY());

                        break;

                    case MotionEvent.ACTION_UP: {
                        coordinates.add(new Coordinates((event.getX() / divisor) * inchToCm, (event.getY() / divisor) * inchToCm));

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




    public void sendThePath() throws InterruptedException {
        ArrayList temp = toBeSent;

        for(final OrderForATime order: toBeSent){
            if(order.pathType==PathType.Curved){
//               activity.send((int)(Math.floor(order.angle)));
                wait(order.arcLength * 3 * 1000);
            }

            else {
                activity.send('f');
                wait(order.count*3*1000);
            }
        }
    }

    public void wait(final float f){
        Runnable toRunInBackground = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep((long)(Math.round(f)));
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

        for (int i=0;i<coordinates.size()-1;i++) {


            if(curved_FirstPoint!=null &&( curved_FirstPoint.getX()==coordinates.get(i).getX() || curved_FirstPoint.getY()==coordinates.get(i).getY())){
                addingCurvedPath(coordinates.get(i));
            }


            float xDifference = Math.abs(coordinates.get(i).getX() - coordinates.get(i + 1).getX());
            float yDifference = Math.abs(coordinates.get(i).getY() - coordinates.get(i + 1).getY());

            if (xDifference>=0.40 && xDifference<=0.55 && Math.abs(coordinates.get(i).getY()-coordinates.get(i+1).getY())<0.30
                    || yDifference>=0.40 && yDifference<=0.55 && Math.abs(coordinates.get(i).getX()-coordinates.get(i+1).getX())<0.30) {
                //Next Point will be considered as the first point connecting a straight line


                if(curved_FirstPoint!=null) {

                    addingCurvedPath(coordinates.get(i));
                }

                straightCount++;

            }


            else{


//                if(firstGradient==0)
//                gradient= yDifference/xDifference;

                if (straightCount != 0) {
                    toBeSent.add(new OrderForATime(PathType.Straight, straightCount));
                    straightCount = 0;
                }

                if (curved_FirstPoint == null)
                    curved_FirstPoint = new Coordinates(coordinates.get(i).getX(), coordinates.get(i).getY());



            }

        }

        try {
            sendThePath();
        }

        catch (Exception e){
            Log.e("Sending",e.getMessage());
        }
    }



    public void addingCurvedPath(Coordinates coordinates){
        //Assumed  angle of the curved path

        float distX= Math.abs(coordinates.getX()-curved_FirstPoint.getX());
        float distY= Math.abs(coordinates.getY()-curved_FirstPoint.getY());

        float myAngle =  distX<0.4 || distY<0.4?180:90;

        float radius =(myAngle==180)?((distX<0.4)?distY/2:distX/2):distY; //Approximated radius of the curved path

        Log.v("path: ",Float.toString(radius));

        float myArcLength= radius*myAngle;

        float myCarAngle = (float) Math.toDegrees(Math.asin(18/radius));  // Approximated angle that the car should move

        toBeSent.add(new OrderForATime(PathType.Curved,myCarAngle,myArcLength));
        curved_FirstPoint=null;
    }

    public void print(){
        Log.v("Coordinates array size",""+coordinates.size());
        for(Coordinates point : coordinates)
            Log.v("Point",point.toString());

    }
}