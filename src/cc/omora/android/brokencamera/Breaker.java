package cc.omora.android.brokencamera;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;

import dalvik.system.DexFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class Breaker {
	public void breakData(byte[] data,   int level) {}
	public void breakData(Bitmap bitmap, int level) {}

	public static Breaker getObject(Context context, String className) {
		String packageName = "cc.omora.android.brokencamera";
		Breaker breaker = null;
		try {
			Context packageContext = context.createPackageContext(packageName, Context.CONTEXT_INCLUDE_CODE + Context.CONTEXT_IGNORE_SECURITY);
			Class<?> klass = packageContext.getClassLoader().loadClass(className); 
			breaker = (Breaker) klass.newInstance();
		} catch(NameNotFoundException e) {
		} catch(InstantiationException e) {
		} catch(IllegalAccessException e) {
		} catch(ClassNotFoundException e) {
		}
		return breaker;
	}
	public static ArrayList<Breaker> getEnabledObjects(Context context, JSONArray breakerSettings) {
		ArrayList<Breaker> breakers = new ArrayList<Breaker>();
		for(int i = 0; i < breakerSettings.length(); i++) {
			try {
				JSONObject jsonObject = breakerSettings.getJSONObject(i);
				if(jsonObject.getBoolean("enabled"))
					breakers.add(getObject(context, jsonObject.getString("className")));
			} catch(JSONException e) {
			}
		}
		return breakers;
	}
	public static String[] getItems(JSONArray breakerSettings) {
		String[] items = new String[breakerSettings.length()];
		try {
			for(int i = 0; i < breakerSettings.length(); i++) {
				String item = breakerSettings.getJSONObject(i).getString("className");
				items[i] = item.substring(39, item.length());
			}
		} catch(JSONException e) {
		}
		return items;
	}
	public static boolean[] getEnabledItems(JSONArray breakerSettings) {
		boolean[] enabledItems = new boolean[breakerSettings.length()];
		try {
			for(int i = 0; i < breakerSettings.length(); i++)
				enabledItems[i] = breakerSettings.getJSONObject(i).getBoolean("enabled");
		} catch(JSONException e) {
		}
		return enabledItems;
	}
	public static JSONArray getSettings(Context context, String savedSettingStr) {
		JSONArray breakers = null;
		DexFile dexFile = null;
		try {
			dexFile = new DexFile(new File(context.getApplicationInfo().sourceDir));
			Enumeration entries = dexFile.entries();
			breakers = new JSONArray(savedSettingStr);
			while(entries.hasMoreElements()) {
				String entry = entries.nextElement().toString();
				if(!entry.startsWith("cc.omora.android.brokencamera.breakers.")) continue;
				boolean flag = false;
				for(int i = 0; i < breakers.length(); i++) {
					JSONObject jsonObject = breakers.optJSONObject(i);
					if(jsonObject != null && jsonObject.getString("className").equals(entry)) {
						flag = true;
						break;
					}
				}
				if(flag) continue;
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("className", entry);
				jsonObject.put("enabled", false);
				breakers.put(jsonObject);
			}
		} catch(IOException e) {
		} catch(JSONException e) {
		}
		return breakers;
	}
}
