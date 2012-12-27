package net.whzxt.zxtexam;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;
import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortFinder;

@SuppressLint("UseSparseArrays")
public class Metadata extends Application {
	private HashMap<Integer, Integer> data_Xinhao;// 信号量数据
	private HashMap<Integer, Integer> data_Maichong;// 脉冲数据
	private int gpsspeed = 0;// GPS速度
	private int gpsangle = 0;// GPS角度
	private float lat = 0f;
	private float lon = 0f;
	private SharedPreferences settings;
	private static final String[] DEF_XINHAO_NAME = { "夜行灯", "近光灯", "远光灯", "左转向灯", "右转向灯", "停车制动器", "应急灯", "行车制动器", "点火信号", "雾灯", "信号11", "信号12", "车门", "信号14", "喇叭", "信号16" };
	private static final String[] DEF_MAICHONG_NAME = { "转速", "速度" };
	private static final float[] DEF_MAICHONG_XS = { 30f, 0.75f };// 脉冲修正系数
	private static final String DEF_PASSWORD = "027";
	private static final int DEF_RANGE = 30;
	private static final String DEF_SERIAL = "/dev/ttyS1";
	private static final String DEF_BAUDRATE = "115200";

	private static float NMDIVIDED = 1.852f; // 海里换算成公里

	private static final int DBVERSION = 6;
	private static final String DBNAME = "zxtexam.db";
	private DBer sqlHelper;
	private SQLiteDatabase db;

	public SerialPortFinder mSerialPortFinder = new SerialPortFinder();
	private SerialPort mSerialPort = null;

	@Override
	public void onCreate() {
		super.onCreate();
		data_Xinhao = new HashMap<Integer, Integer>();
		data_Maichong = new HashMap<Integer, Integer>();
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		data_Xinhao.put(0, 0);
		data_Xinhao.put(1, 0);
		data_Xinhao.put(2, 0);
		data_Xinhao.put(3, 0);
		data_Xinhao.put(4, 0);
		data_Xinhao.put(5, 0);
		data_Xinhao.put(6, 0);
		data_Xinhao.put(7, 0);
		data_Xinhao.put(8, 0);
		data_Xinhao.put(9, 0);
		data_Xinhao.put(10, 0);
		data_Xinhao.put(11, 0);
		data_Xinhao.put(12, 0);
		data_Xinhao.put(13, 0);
		data_Xinhao.put(14, 0);
		data_Xinhao.put(15, 0);

		data_Maichong.put(20, 0);
		data_Maichong.put(21, 0);

		sqlHelper = new DBer(this, Metadata.DBNAME, null, Metadata.DBVERSION);
		db = sqlHelper.getWritableDatabase();
	}

	public Cursor rawQuery(String sql) {
		Log.i("database", sql);
		return db.rawQuery(sql, null);
	}

	public void execSQL(String sql) {
		Log.i("database", sql);
		db.execSQL(sql);
	}

	public void setData(int id, int val) {
		if (id < 20) {
			data_Xinhao.put(id, val);
			return;
		}
		if (id < 30) {
			data_Maichong.put(id, val);
			return;
		}
		if (id == 30) {
			gpsspeed = val;
			return;
		}
		if (id == 31) {
			gpsangle = val;
		}
	}

	public int setGPSSpeed(float f) {
		if (settings.getBoolean("haili", false)) {
			gpsspeed = Math.round(f / NMDIVIDED * 60 * 60 / 1000);
		} else {
			gpsspeed = Math.round(f * 60 * 60 / 1000);
		}
		return gpsspeed;
	}

	public void setGPSLatlon(float lat, float lon) {
		this.lat = lat;
		this.lon = lon;
	}

	public float[] getLatlon() {
		return new float[] { lat, lon };
	}

	public String getLatLonString() {
		return String.format("%.6f", lon) + "," + String.format("%.6f", lat);
	}

	public int getData(int id) {
		if (id < 20) {
			return data_Xinhao.get(id);
		}
		if (id < 30) {
			return Math.round((float) data_Maichong.get(id) * getMaichongXS(id - 19));
		}
		if (id == 30) {
			return gpsspeed;
		}
		if (id == 31) {
			return gpsangle;
		}
		return 0;
	}

	public int getRange() {
		return Integer.parseInt(settings.getString("range", String.valueOf(DEF_RANGE)));
	}

	public String getName(int id) {
		if (id < 20) {
			return settings.getString("name" + id, DEF_XINHAO_NAME[id]);
		}
		if (id < 30) {
			return settings.getString("name" + id, DEF_MAICHONG_NAME[id - 20]);
		}
		return "";
	}

	public void setName(int id, String val) {
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("name" + id, val);
		editor.commit();
	}

	public String getPassword() {
		return settings.getString("password", DEF_PASSWORD);
	}

	public float getMaichongXS(int n) {
		float ret = DEF_MAICHONG_XS[n - 1];
		try {
			ret = Float.parseFloat(settings.getString("maichongxs" + n, String.valueOf(DEF_MAICHONG_XS[n - 1])));
		} catch (Exception e) {
			ret = DEF_MAICHONG_XS[n - 1];
		}
		return ret;
	}

	public SerialPort getSerialPort() throws SecurityException, IOException, InvalidParameterException {
		if (mSerialPort == null) {
			/* Read serial port parameters */
			String path = settings.getString("device", DEF_SERIAL);
			int baudrate = Integer.decode(settings.getString("baudrate", DEF_BAUDRATE));

			/* Check parameters */
			if ((path.length() == 0) || (baudrate == -1)) {
				throw new InvalidParameterException();
			}

			/* Open the serial port */
			mSerialPort = new SerialPort(new File(path), baudrate, 0);
		}
		return mSerialPort;
	}

	public void closeSerialPort() {
		if (mSerialPort != null) {
			mSerialPort.close();
			mSerialPort = null;
		}
	}

	public String toBinaryString(int i) {
		char[] digits = { '0', '1' };
		char[] buf = new char[8];
		int pos = 8;
		int mask = 1;
		do {
			buf[--pos] = digits[i & mask];
			i >>>= 1;
		} while (pos > 0);

		return new String(buf, pos, 8);
	}

}
