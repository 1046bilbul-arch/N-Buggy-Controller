package au.simplecontrol.nbuggytest;

import android.graphics.*;
import android.graphics.drawable.Drawable;
import java.util.Random;

/** Reproducible concrete grain and hairline cracks; no external assets needed. */
public class CamoBackground extends Drawable {
 private final Paint paint=new Paint(3);
 private final Bitmap tile;
 public CamoBackground(){
  tile=Bitmap.createBitmap(384,384,Bitmap.Config.ARGB_8888);
  Canvas c=new Canvas(tile);c.drawColor(Color.rgb(190,190,179));
  Random r=new Random(1103);
  for(int i=0;i<15000;i++){
   int tone=70+r.nextInt(140);paint.setColor(Color.argb(12+r.nextInt(23),tone,tone,tone-5));
   c.drawCircle(r.nextInt(384),r.nextInt(384),.3f+r.nextFloat()*1.1f,paint);
  }
  paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(.65f);paint.setColor(0x2842493e);
  for(int n=0;n<4;n++){
   float x=r.nextInt(384),y=r.nextInt(384);Path p=new Path();p.moveTo(x,y);
   for(int i=0;i<9;i++){x+=r.nextInt(33)-16;y+=10+r.nextInt(20);p.lineTo(x,y);}c.drawPath(p,paint);
  }
  paint.setStyle(Paint.Style.FILL);
 }
 @Override public void draw(Canvas c){
  paint.setShader(new BitmapShader(tile,Shader.TileMode.REPEAT,Shader.TileMode.REPEAT));
  c.drawRect(getBounds(),paint);paint.setShader(null);
  paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(4);paint.setColor(0xff435247);c.drawRect(getBounds(),paint);paint.setStyle(Paint.Style.FILL);
 }
 @Override public void setAlpha(int alpha){paint.setAlpha(alpha);}
 @Override public void setColorFilter(ColorFilter f){paint.setColorFilter(f);}
 @Override public int getOpacity(){return PixelFormat.OPAQUE;}
}
