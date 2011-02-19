package cc.omora.android.brokencamera.breakers;
import cc.omora.android.brokencamera.Breaker;
import java.util.Random;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;

public class Abstraction extends Breaker {
	public Bitmap breakData(Bitmap b, int l) {
		Canvas c = new Canvas(b);
		int w = b.getWidth(),
		    h = b.getHeight();
		int s = (int)((double)(w < h ? w : h) * ((double)l/100));
		switch(new Random().nextInt(3)) {
			case(0): circle(c, w, h, s, paint()); break;
			case(1): triangle(c, w, h, s, paint()); break;
			case(2): rectangle(c, w, h, s, paint()); break;
		}
		BitmapDrawable bd = new BitmapDrawable(b);
		bd.setBounds(0, 0, w, h);
		bd.draw(c);
		return bd.getBitmap();
	}
	public void circle(Canvas c, int w, int h, int s, Paint p) {
		c.drawCircle(w/2, h/2, s/2, p);
	}
	public void triangle(Canvas c, int w, int h, int s, Paint p) {
		double r = 0.0174632929265499; // pi/180
		int x1 = w/2;
		s = (int)(s*0.57);
		int y1 = h/2 - (int)s;
		int x2 = (int)(w/2 + Math.cos(r*30) * s);
		int y2 = (int)(h/2 + Math.sin(r*30) * s);
		int x3 = (int)(w/2 - Math.cos(r*-30) * s);
		int y3 = (int)(h/2 - Math.sin(r*-30) * s);
		Path path = new Path();
		path.moveTo(x1, y1);
		path.lineTo(x2, y2);
		path.lineTo(x3, y3);
		path.close();
		c.drawPath(path, p);
	}
	public void rectangle(Canvas c, int w, int h, int s, Paint p) {
		float x1 = w/2 - s/2;
		float y1 = h/2 - s/2;
		float x2 = w/2 + s/2;
		float y2 = h/2 + s/2;
		c.drawRect(x1, y1, x2, y2, p);
	}
	public Paint paint() {
	    	int r = new Random().nextInt(256),
	    	    g = new Random().nextInt(256),
	    	    b = new Random().nextInt(256);
		int c = Color.rgb(r, g, b);
		Paint p = new Paint();
		p.setAntiAlias(true);
		p.setColor(c);
		p.setStyle(Paint.Style.FILL);
		return p;
	}
}
