package app.com.banana_squad.bananasquadcar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.view.View;

/**
 * Created by delll on 7/17/2016.
 */
public class Draw extends View {


     Paint brush = new Paint(Paint.ANTI_ALIAS_FLAG);
     Path path = new Path();
    final float inchToCm =2.54f;
    Paint transparent = new Paint();
    boolean clear = false;
    Bitmap bitmap;
    Canvas temp;
    Paint transparentPaint;
    int x,y;



    public Draw(Context context,int x,int y){
        super(context);
        brush.setStyle(Paint.Style.STROKE);
        brush.setStrokeWidth(15);
        this.x=x;
        this.y=y;
        transparentPaint = new Paint();
        transparentPaint.setColor(getResources().getColor(android.R.color.transparent));
        transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        transparentPaint.setAntiAlias(true);



//       LayoutInflater inflater= LayoutInflater.from(context);
//        View view =  inflater.inflate(R.layout.draw_rem,null,false);
//        this.addView(view);
    }





    @Override
    protected void onDraw(Canvas canvas) {

        if(clear) {
            path=new Path();

        }
        canvas.drawPath(path,brush);
        invalidate();

        clear=false;
    }


    }



