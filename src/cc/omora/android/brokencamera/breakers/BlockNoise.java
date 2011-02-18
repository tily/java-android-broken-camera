package cc.omora.android.brokencamera.breakers;
import cc.omora.android.brokencamera.Breaker;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;

public class BlockNoise extends Breaker {
        public Bitmap breakData(Bitmap bitmap, int level) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 101-level, bos);
		byte[] data = bos.toByteArray();
		BitmapFactory.Options options = new BitmapFactory.Options();
		return BitmapFactory.decodeByteArray(data, 0, data.length, options).copy(Bitmap.Config.RGB_565, true);
        }
}
