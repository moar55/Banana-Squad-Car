package app.com.banana_squad.bananasquadcar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;
import android.view.View;

/**
 * Created by delll on 7/17/2016.
 */
public class Draw extends View {


     Paint brush = new Paint(Paint.ANTI_ALIAS_FLAG);
    CreatePath createPath;
     Path path = new Path();
    final float inchToCm =2.54f;
    Paint transparent = new Paint();
    boolean clear = false;
    Bitmap bitmap;
    Canvas temp;
    Paint transparentPaint;
    int x,y;
    boolean drawPoint=false;

    public Draw(Context context, int x, int y, CreatePath createPath){
        super(context);
        brush.setStyle(Paint.Style.STROKE);
        brush.setStrokeWidth(15);
        this.x=x;
        this.y=y;
        transparentPaint = new Paint();
        transparentPaint.setColor(getResources().getColor(android.R.color.transparent));
        transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        transparentPaint.setAntiAlias(true);
        this.createPath=createPath;



//       LayoutInflater inflater= LayoutInflater.from(context);
//        View view =  inflater.inflate(R.layout.draw_rem,null,false);
//        this.addView(view);
    }







    @Override
    protected void onDraw(Canvas canvas) {


        Log.v("Draw","In here");

        if(clear) {
            path=new Path();
            clear=false;
        }

        else if(drawPoint) {

            Log.v("draw a Point","True "+createPath.point.getX()+ " "+createPath.point.getY());
            Paint temp  = new Paint();
            temp.setColor(Color.RED);
            canvas.drawPoint(createPath.point.getX(), createPath.point.getY(), temp);
        }


        canvas.drawPath(path,brush);


        invalidate();

    }




    }



