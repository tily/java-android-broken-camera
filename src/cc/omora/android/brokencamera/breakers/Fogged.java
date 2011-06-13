// based on LoadPixels 1 by video
package cc.omora.android.brokencamera.breakers;
import cc.omora.android.brokencamera.Breaker;
import java.util.Random;
import android.graphics.Bitmap;

public class Fogged extends Breaker {
        public Bitmap breakData(Bitmap bitmap, int level) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
                int[] source = new int[width*height];
                int[] target = new int[width*height];
		bitmap.getPixels(source, 0, width, 0, 0, width, height);
                for(int i = 0; i < source.length; i++) {
                        if(i < 50 || i > source.length -50) {
                                target[i] = source[i];
                        } else {
                                int friction = new Random().nextInt(100)-50;
                                target[i] = source[i + friction];
                        }
                }
		bitmap.setPixels(target, 0, width, 0, 0, width, height);
		return bitmap;
        }
}

