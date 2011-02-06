package cc.omora.android.brokencamera;

import java.io.IOException;
import java.lang.Runnable;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.Display;
import android.view.KeyEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
//import android.util.Log;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

public class BrokenCamera extends Activity implements KeyEvent.Callback
{
	Preview mPreview;
	PostView mPostView;
	LevelControl mLevelControl;
	ProcessingDialog mProcessingDialog;
	
	Camera mCameraDevice;
	Handler mLevelControlHandler;
	Runnable mLevelControlRunnable;
	OrientationEventListener mOrientationEventListener;
	
	int mWidth;
	int mHeight;
	int mLevel;
	boolean mIsTakingPicture;
	long mLastLevelControlTimeMillis;
	int mOrientation;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Display display = getWindowManager().getDefaultDisplay();
		mWidth = display.getWidth();
		mHeight = display.getHeight(); 
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		mLevel = PreferenceManager.getDefaultSharedPreferences(this).getInt("Level", 50);

		if(mPreview == null) {
			mPreview = new Preview(this);
			setContentView(mPreview);
			mPreview.setOnClickListener(mOnClickListener);
		}
		if(mPostView == null) {
			mPostView = new PostView(this);
			addContentView(mPostView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		}
		if(mLevelControl == null) {
			mLevelControl = new LevelControl(this);
			mLevelControl.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mLevelControl.setTitle("Glitch Level");
			mLevelControl.setProgress(mLevel);
		}
		if(mProcessingDialog == null) {
			mProcessingDialog = new ProcessingDialog(this);
			mProcessingDialog.setTitle("Processing ...");
		}
		mOrientationEventListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_UI) {
			public void onOrientationChanged(int orientation) {
				if(!mIsTakingPicture && mCameraDevice != null) {
					orientation = (orientation + 45) / 90 * 90;
					orientation = orientation % 360;
					orientation = orientation == 270 ? 0 : orientation + 90;
					Camera.Parameters params = mCameraDevice.getParameters();
					params.setRotation(orientation);
					mCameraDevice.setParameters(params);
					mOrientation = orientation;
				}
			}
		};
		mOrientationEventListener.enable();
	}

	protected void onStop() {
		super.onStop();
		if(mCameraDevice != null) {
			mCameraDevice.stopPreview();
			mCameraDevice.release();
			mCameraDevice = null;
		}
	}
	
	public void onDestroy() {
		super.onDestroy();
		mOrientationEventListener.disable();
	}

	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return true;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(mIsTakingPicture) {
			return true;
		}
		if(keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			showGlitchLevelControl(keyCode);
		} else if(keyCode == KeyEvent.KEYCODE_BACK) {
			if(mCameraDevice != null) {
				mCameraDevice.stopPreview();
				mCameraDevice.release();
				mCameraDevice = null;
			}
			SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
			editor.putInt("Level", mLevel);
			editor.commit();
			setResult(Activity.RESULT_CANCELED);
			finish();
		}
		return true;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		setResult(resultCode);
		finish();
	}

	public void showGlitchLevelControl(int keyCode) {
		if(keyCode == KeyEvent.KEYCODE_VOLUME_UP && mLevel < 100) {
			mLevel++;
		} else if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && mLevel > 1) {
			mLevel--;
		}
		if(mLevelControlRunnable != null) {
			mLevelControlHandler.removeCallbacks(mLevelControlRunnable);
		}
		if(!mLevelControl.isShowing()) {
			mLevelControl.show();
		}
		mLevelControl.setProgress(mLevel);
		mLastLevelControlTimeMillis = System.currentTimeMillis();
		mLevelControlHandler = new Handler();
		mLevelControlRunnable = new Runnable() {
			public void run() {
				if(System.currentTimeMillis() - mLastLevelControlTimeMillis > 1000 && mLevelControl.isShowing()) {
					mLevelControl.dismiss();
				}
			}
		};
		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(1000);
				} catch(InterruptedException e) {
				}
				mLevelControlHandler.post(mLevelControlRunnable);
			}
		}).start();
	}

	public void showErrorAndFinish(String message) {
		Toast.makeText(this, "Error: "+message, Toast.LENGTH_SHORT).show();
		setResult(RESULT_CANCELED);
		finish();
	}

	class Preview extends SurfaceView implements SurfaceHolder.Callback {
		Preview(Context context) {
			super(context);
			try {
				SurfaceHolder holder = getHolder();
				holder.addCallback(this);
				holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			} catch(NullPointerException e) {
				showErrorAndFinish(e.getMessage());
			}
		}
		public void surfaceCreated(SurfaceHolder holder) {
			try {
				if(mCameraDevice == null) {
					mCameraDevice = Camera.open();
				}
				mCameraDevice.setPreviewDisplay(holder);
			} catch(IOException e) {
				showErrorAndFinish(e.getMessage());
			} catch(NullPointerException e) {
				showErrorAndFinish(e.getMessage());
			} catch(RuntimeException e) {
				showErrorAndFinish(e.getMessage());
			}
		}
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			Camera.Parameters params = mCameraDevice.getParameters();
			List<Size> previewSizes = Reflect.getSupportedSizes(params, true);
			List<Size> pictureSizes = Reflect.getSupportedSizes(params, false);
			if(null != previewSizes) {
				Size optimalSize = getOptimalSize(previewSizes, width, height);
				params.setPreviewSize(optimalSize.width, optimalSize.height);
			} else {
				params.setPreviewSize(width, height);
			}
			if(null != pictureSizes) {
				Size optimalSize = getOptimalSize(pictureSizes, width, height);
				params.setPictureSize(optimalSize.width, optimalSize.height);
			} else {
				params.setPictureSize(width, height);
			}
			mCameraDevice.setParameters(params);
			if(mCameraDevice != null) {
				mCameraDevice.stopPreview();
				mCameraDevice.startPreview();
			}
		}
		public void surfaceDestroyed(SurfaceHolder holder) {
		}
		private Size getOptimalSize(List<Size> sizes, int w, int h) {
			final double ASPECT_TOLERANCE = 0.05;
			double targetRatio = (double) w / h;
			if (sizes == null) return null;
			Size optimalSize = null;
			for (Size size : sizes) {
				double ratio = (double) size.width / size.height;
				if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
					optimalSize = size;
					break;
				}
			}
			return optimalSize;
		}
	}

	class PostView extends SurfaceView {
		PostView(Context context) {
			super(context);
			try {
				getHolder().setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
			} catch(NullPointerException e) {
				showErrorAndFinish(e.getMessage());
			}
		}
		public void drawBitmapToCanvas(Bitmap bitmap) {
			Canvas canvas = getHolder().lockCanvas();
			Matrix matrix = new Matrix();
			matrix.postRotate(360 - mOrientation);
			Bitmap drawn = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
			Rect rect = new Rect(0, 0, mWidth, mHeight);
			canvas.drawBitmap(drawn, null, rect, null);
			getHolder().unlockCanvasAndPost(canvas);
		}
	}

	class LevelControl extends ProgressDialog {
		LevelControl(Context context) {
			super(context);
		}
		public boolean onKeyUp(int keyCode, KeyEvent keyEvent) {
			return true;
		}
		public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
			if(mIsTakingPicture) {
				return true;
			}
			if(keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
				showGlitchLevelControl(keyCode);
			}
			return true;
		}
	}

	class ProcessingDialog extends ProgressDialog {
		ProcessingDialog(Context context) {
			super(context);
		}
		public boolean onKeyUp(int keyCode, KeyEvent keyEvent) {
			return true;
		}
		public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
			return true;
		}
	}

	OnClickListener mOnClickListener = new OnClickListener() {
		public void onClick(View v) {
			try {
				mCameraDevice.autoFocus(mAutoFocusCallback);
				mIsTakingPicture = true;
			} catch(NullPointerException e) {
				showErrorAndFinish(e.getMessage());
			}
		}
	};

	AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback() {
		public void onAutoFocus(boolean success, final Camera camera) {
			camera.takePicture(mShutterCallback, mRawPictureCallback, mJpgPictureCallback);
		}
	};

	PictureCallback mJpgPictureCallback = new PictureCallback() {
		public void onPictureTaken(byte [] data, final Camera camera) {
			for(int i = 0; i < data.length; i++) {
				if(data[i] == 48 && new Random().nextInt((101 - mLevel)*5) == 0) {
					data[i] = (byte) (new Random().nextInt(10) + 47);
				}
			}
	
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 4;
			Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
	
			String file_name = String.valueOf(System.currentTimeMillis()) + ".jpg";
			MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, file_name, null);
	
			mProcessingDialog.dismiss();
			mPreview.setVisibility(View.INVISIBLE);
			mPostView.setVisibility(View.VISIBLE);
			mPostView.drawBitmapToCanvas(bitmap);
	
			final Handler handler = new Handler();
			new Thread(new Runnable() {
				public void run() {
					try {
						Thread.sleep(1000);
					} catch(InterruptedException e) {
					}
					handler.post(new Runnable() {
						public void run() {
							mPreview.setVisibility(View.VISIBLE);
							mPostView.setVisibility(View.INVISIBLE);
							mIsTakingPicture = false;
							camera.startPreview();
						}
					});
				}
			}).start();
		}
	};

	PictureCallback mRawPictureCallback = new PictureCallback() {
		public void onPictureTaken(byte [] data, Camera camera) {
		}
	};

	ShutterCallback mShutterCallback = new ShutterCallback() {
		public void onShutter() {
			mProcessingDialog.show();
		}
	};

	ErrorCallback mErrorCallback = new ErrorCallback() {
		public void onError(int error, android.hardware.Camera camera) {
			if (error == Camera.CAMERA_ERROR_SERVER_DIED) {
				showErrorAndFinish("CAMERA_ERROR_SERVER_DIED");
			}
		}
	};
}
