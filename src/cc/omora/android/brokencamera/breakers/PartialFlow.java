// based on LoadPixels 1 by video
package cc.omora.android.brokencamera.breakers;
import cc.omora.android.brokencamera.Breaker;
import android.graphics.Bitmap;

public class PartialFlow extends Breaker {
        public Bitmap breakData(Bitmap bitmap, int level) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
                int[] source = new int[width*height];
		int[] target = new int[width*height];
		bitmap.getPixels(source, 0, width, 0, 0, width, height);
                for(int y = 0; y < height; y++) {
            	        for(int x = 0; x < width; x++) {
            	                if(x > width - y) {
            	            	        target[y * width + x] = source[y * width + x];
            	                }
            	                else {
            	            	        target[y * width + x] = source[y * width + y];
            	                }
            	        }
                }
		bitmap.setPixels(target, 0, width, 0, 0, width, height);
		return bitmap;
        }
}

