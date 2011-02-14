package cc.omora.android.brokencamera.breakers;
import cc.omora.android.brokencamera.Breaker;
import java.util.Random;

public class MonkeyGlitch extends Breaker {
	public void breakData(byte[] data, int level) {
		for(int i = 0; i < data.length; i++) {
			if(data[i] == 48 && new Random().nextInt((101 - level)*5) == 0) {
				data[i] = (byte) (new Random().nextInt(10) + 47);
			}
		}
	}
}
