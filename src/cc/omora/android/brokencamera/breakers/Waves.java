// based on LoadPixels 2 by video
package cc.omora.android.brokencamera.breakers;
import cc.omora.android.brokencamera.Breaker;
import android.graphics.Bitmap;

public class Waves extends Breaker {
        public Bitmap breakData(Bitmap bitmap, int level) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
                int[] source = new int[width*height];
                int[] target = new int[width*height];
		bitmap.getPixels(source, 0, width, 0, 0, width, height);
                int slide = 0;
                for(int y = 0; y < height; y++) {
                        for(int x = 0; x < width; x++) {
                                if(y * width + x + slide < width * height) {
                                        target[y * width + x] = source[y * width + x + slide];
                                } else {
                                        target[y * width + x] = source[y * width + x - slide];
                                }
                        }
                        slide = slide + y;
                }
		bitmap.setPixels(target, 0, width, 0, 0, width, height);
		return bitmap;
        }
}
