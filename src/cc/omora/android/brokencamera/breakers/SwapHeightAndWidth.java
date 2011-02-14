package cc.omora.android.brokencamera.breakers;
import cc.omora.android.brokencamera.Breaker;

public class SwapHeightAndWidth extends Breaker {
        public void breakData(byte[] data, int level) {
                for(int i = 0; i < data.length; i++) {
                        if((data[i] & 0xFF) == 0xFF && ((data[i+1] & 0xFF) == 0xC0 || (data[i+1] & 0xFF) == 0xC2)) {
                                byte[] bytes = {data[i+5], data[i+6], data[i+7], data[i+8]};
                                data[i+5] = bytes[2];
                                data[i+6] = bytes[3];
                                data[i+7] = bytes[0];
                                data[i+8] = bytes[1];
                        }
                }
        }
}
