package cc.omora.android.brokencamera.breakers;
import cc.omora.android.brokencamera.Breaker;
import android.graphics.Bitmap;
import java.util.Arrays;

public class SortPixels extends Breaker {
    public Bitmap breakData(Bitmap source, int level) {
        final int width = source.getWidth();
        final int height = source.getHeight();
        int pixels[] = new int[width * height];
        Bitmap bitmap = Bitmap.createBitmap(width, height, source.getConfig());

        source.getPixels(pixels, 0, width, 0, 0, width, height);
        Arrays.sort(pixels);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        return bitmap;
    }
}
