// based on LoadPixels 1 by video
package cc.omora.android.brokencamera.breakers;
import cc.omora.android.brokencamera.Breaker;
import java.util.Random;
import android.graphics.Bitmap;

public class Sandstorm extends Breaker {
        public Bitmap breakData(Bitmap bitmap, int level) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
                int[] source = new int[width*height];
                int[] target = new int[width*height];
		bitmap.getPixels(source, 0, width, 0, 0, width, height);
                for(int i = 0; i < source.length; i++){
            	        int p = new Random().nextInt(source.length);
            	        target[i] = source[p];
                }
		bitmap.setPixels(target, 0, width, 0, 0, width, height);
		return bitmap;
        }
}
