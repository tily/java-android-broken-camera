// based on http://labs.techfirm.co.jp/android/cho/1647
package cc.omora.android.brokencamera;

import android.hardware.Camera;
import android.hardware.Camera.*;
import android.util.Log;
import java.lang.reflect.*;
import java.util.*;

public class Reflect{
	private final static String LOGTAG="Reflect";
	private static Method Parameters_getSupportedPreviewSizes;
	private static Method Parameters_getSupportedPictureSizes;
	static{
		initCompatibility();
	}
	private static void initCompatibility(){
		try{
			Parameters_getSupportedPreviewSizes = Camera.Parameters.class
				.getMethod("getSupportedPreviewSizes",new Class[]{});
			Parameters_getSupportedPictureSizes = Camera.Parameters.class
				.getMethod("getSupportedPictureSizes",new Class[]{});
		}catch(NoSuchMethodException e){
			Log.d(LOGTAG,"no such method exception");
		}
	}
	@SuppressWarnings("unchecked")
	public static List<Size> getSupportedSizes(Camera.Parameters p, boolean flag){
		try{
		        Method method = flag ? Parameters_getSupportedPreviewSizes : Parameters_getSupportedPictureSizes;
			if(method != null){
				return (List<Size>)method.invoke(p);
			}else{
				return null;
			}
		}catch(InvocationTargetException e){
			Log.e(LOGTAG,"InvocationTargetException");
			Throwable cause=e.getCause();
			if(cause instanceof RuntimeException){
				throw (RuntimeException)cause;
			}else if(cause instanceof Error){
				throw (Error)cause;
			}else{
				throw new RuntimeException(e);
			}
		}catch(IllegalAccessException e){
			Log.e(LOGTAG,"IllegalAccessException");
			return null;
		}
	}
}
