package net.whzxt.zxtexam;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;

@SuppressLint("HandlerLeak")
public class DetectActivity extends SerialPortActivity {

	private TextView textView0, textView1, textView2, textView3, textView4,
			textView5, textView6, textView7, textView8, textView9, textView10,
			textView11, textView12, textView13, textView14, textView15;
	private TextView textView20, textView21;
	private TextView textView30, textView31, textView32;
	private EditText txtReception;
	private Timer _timer;
	private Boolean readFlag = false;
	private LocationManager locationManager;
	private String data;
	private byte[] mBuffer;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				changeSerial();
				break;
			case 1:
				changeGPS();
				break;
			case 2:
				appendText();
				break;
			default:
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detect);
		mBuffer = new byte[7];
		mBuffer[0] = 0x1A;
		mBuffer[1] = 0x01;
		mBuffer[2] = 0x00;
		mBuffer[3] = 0x00;
		mBuffer[4] = 0x00;
		mBuffer[5] = 0x00;
		mBuffer[6] = 0x1D;
		txtReception = (EditText) findViewById(R.id.txtReception);
		textView0 = (TextView) findViewById(R.id.textView0);
		textView1 = (TextView) findViewById(R.id.textView1);
		textView2 = (TextView) findViewById(R.id.textView2);
		textView3 = (TextView) findViewById(R.id.textView3);
		textView4 = (TextView) findViewById(R.id.textView4);
		textView5 = (TextView) findViewById(R.id.textView5);
		textView6 = (TextView) findViewById(R.id.textView6);
		textView7 = (TextView) findViewById(R.id.textView7);
		textView8 = (TextView) findViewById(R.id.textView8);
		textView9 = (TextView) findViewById(R.id.textView9);
		textView10 = (TextView) findViewById(R.id.textView10);
		textView11 = (TextView) findViewById(R.id.textView11);
		textView12 = (TextView) findViewById(R.id.textView12);
		textView13 = (TextView) findViewById(R.id.textView13);
		textView14 = (TextView) findViewById(R.id.textView14);
		textView15 = (TextView) findViewById(R.id.textView15);
		textView20 = (TextView) findViewById(R.id.textView20);
		textView21 = (TextView) findViewById(R.id.textView21);
		textView30 = (TextView) findViewById(R.id.textView30);
		textView31 = (TextView) findViewById(R.id.textView31);
		textView32 = (TextView) findViewById(R.id.textView32);
		textView0.setText(md.getName(0));
		textView1.setText(md.getName(1));
		textView2.setText(md.getName(2));
		textView3.setText(md.getName(3));
		textView4.setText(md.getName(4));
		textView5.setText(md.getName(5));
		textView6.setText(md.getName(6));
		textView7.setText(md.getName(7));
		textView8.setText(md.getName(8));
		textView9.setText(md.getName(9));
		textView10.setText(md.getName(10));
		textView11.setText(md.getName(11));
		textView12.setText(md.getName(12));
		textView13.setText(md.getName(13));
		textView14.setText(md.getName(14));
		textView15.setText(md.getName(15));
		textView20.setText(md.getName(20));
		textView21.setText(md.getName(21));
		textView0.setBackgroundResource(R.color.lightoff);
		textView1.setBackgroundResource(R.color.lightoff);
		textView2.setBackgroundResource(R.color.lightoff);
		textView3.setBackgroundResource(R.color.lightoff);
		textView4.setBackgroundResource(R.color.lightoff);
		textView5.setBackgroundResource(R.color.lightoff);
		textView6.setBackgroundResource(R.color.lightoff);
		textView7.setBackgroundResource(R.color.lightoff);
		textView8.setBackgroundResource(R.color.lightoff);
		textView9.setBackgroundResource(R.color.lightoff);
		textView10.setBackgroundResource(R.color.lightoff);
		textView11.setBackgroundResource(R.color.lightoff);
		textView12.setBackgroundResource(R.color.lightoff);
		textView13.setBackgroundResource(R.color.lightoff);
		textView14.setBackgroundResource(R.color.lightoff);
		textView15.setBackgroundResource(R.color.lightoff);
		textView20.setBackgroundResource(R.color.lightoff);
		textView21.setBackgroundResource(R.color.lightoff);
		textView30.setBackgroundResource(R.color.lightoff);
		textView31.setBackgroundResource(R.color.lightoff);
		textView32.setBackgroundResource(R.color.lightoff);
		_timer = new Timer();
		_timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (!readFlag) {
					readFlag = !readFlag;
					writeSerial();
					readFlag = !readFlag;
				}
			}
		}, 50, 50);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				1000, 0, locationListener);
		changeGPS();
	}

	private void writeSerial() {
		try {
			if (mOutputStream != null) {
				mOutputStream.write(mBuffer);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private final LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			// log it when the location changes
			if (location != null) {
				md.setGPSSpeed(location.getSpeed());
				md.setGPSLatlon((float) location.getLatitude(),
						(float) location.getLongitude());
				md.setData(31, Math.round(location.getBearing()));
				changeGPS();
			}
		}

		public void onProviderDisabled(String provider) {
			// Provider被disable时触发此函数，比如GPS被关闭
		}

		public void onProviderEnabled(String provider) {
			// Provider被enable时触发此函数，比如GPS被打开
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			// Provider的转态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
		}
	};

	private void changeSerial() {
		textView0.setBackgroundResource(md.getData(0) == 1 ? R.color.lighton
				: R.color.lightoff);
		textView1.setBackgroundResource(md.getData(1) == 1 ? R.color.lighton
				: R.color.lightoff);
		textView2.setBackgroundResource(md.getData(2) == 1 ? R.color.lighton
				: R.color.lightoff);
		textView3.setBackgroundResource(md.getData(3) == 1 ? R.color.lighton
				: R.color.lightoff);
		textView4.setBackgroundResource(md.getData(4) == 1 ? R.color.lighton
				: R.color.lightoff);
		textView5.setBackgroundResource(md.getData(5) == 1 ? R.color.lighton
				: R.color.lightoff);
		textView6.setBackgroundResource(md.getData(6) == 1 ? R.color.lighton
				: R.color.lightoff);
		textView7.setBackgroundResource(md.getData(7) == 1 ? R.color.lighton
				: R.color.lightoff);
		textView8.setBackgroundResource(md.getData(8) == 1 ? R.color.lighton
				: R.color.lightoff);
		textView9.setBackgroundResource(md.getData(9) == 1 ? R.color.lighton
				: R.color.lightoff);
		textView10.setBackgroundResource(md.getData(10) == 1 ? R.color.lighton
				: R.color.lightoff);
		textView11.setBackgroundResource(md.getData(11) == 1 ? R.color.lighton
				: R.color.lightoff);
		textView12.setBackgroundResource(md.getData(12) == 1 ? R.color.lighton
				: R.color.lightoff);
		textView13.setBackgroundResource(md.getData(13) == 1 ? R.color.lighton
				: R.color.lightoff);
		textView14.setBackgroundResource(md.getData(14) == 1 ? R.color.lighton
				: R.color.lightoff);
		textView15.setBackgroundResource(md.getData(15) == 1 ? R.color.lighton
				: R.color.lightoff);

		textView20.setBackgroundResource(md.getData(20) == 0 ? R.color.lightoff
				: R.color.lighton);
		textView21.setBackgroundResource(md.getData(21) == 0 ? R.color.lightoff
				: R.color.lighton);
		if (md.getData(20) == 0) {
			textView20.setText(md.getName(20));
		} else {
			textView20.setText(md.getName(20) + ":" + md.getData(20));
		}
		if (md.getData(21) == 0) {
			textView21.setText(md.getName(21));
		} else {
			textView21.setText(md.getName(21) + ":" + md.getData(21));
		}
	}

	private void changeGPS() {
		textView30.setBackgroundResource(md.getData(30) == 0 ? R.color.lightoff
				: R.color.lighton);
		textView31.setBackgroundResource(md.getData(31) == 0 ? R.color.lightoff
				: R.color.lighton);
		textView32.setBackgroundResource(md.getLatLonString().equals(
				"0.000000,0.000000") ? R.color.lightoff : R.color.lighton);

		textView30.setText("GPS速度:" + md.getData(30) + "km/h");
		textView31.setText("GPS角度:" + md.getData(31) + "度");
		textView32.setText("GPS经纬度:" + md.getLatLonString());
	}

	@Override
	protected void onDataReceived(byte[] buffer, int size) {
		data = "";
		for (int i = 0; i < size; i++) {
			if (Integer.toHexString(buffer[i]&0xFF).length() == 1) {
				data += "0"+Integer.toHexString(buffer[i]&0xFF)+" ";
			} else {
				data += Integer.toHexString(buffer[i]&0xFF)+" ";
			}
		}
		data = data.toUpperCase();
		handler.sendEmptyMessage(2);
		if (buffer[0] == 0x1A && buffer[size-1] == 0x1D) {
			String bfs = data;
			bfs = bfs.replace(" ", "").replace("1B11", "1A").replace("1B14", "1D").replace("1B0B", "1B");
			int t1 = 0;
			int t2 = 0;
			for (int i = 2; i < 20; i++) {
				t1 += Integer.parseInt(bfs.substring(i,i+2),16);
				i++;
			}
			for (int i = 24; i < 32; i++) {
				t2 += Integer.parseInt(bfs.substring(i,i+2),16);
				i++;
			}
			if (t1 != t2) {
				return;
			}
			if (bfs.substring(2, 4).equals("02")) {				
				String str = md.toBinaryString(Integer.parseInt(bfs.substring(4,6),16));
				for (int i = 0; i < str.length(); i++) {
					if (str.substring(i, i + 1).equals("1")) {
						md.setData(7-i, 1);
					} else {
						md.setData(7-i, 0);
					}
				}
				str = md.toBinaryString(Integer.parseInt(bfs.substring(6,8),16));
				for (int i = 0; i < str.length(); i++) {
					if (str.substring(i, i + 1).equals("1")) {
						md.setData(15-i, 1);
					} else {
						md.setData(15-i, 0);
					}
				}
				md.setData(20, Integer.parseInt(bfs.substring(10,12)+bfs.substring(8,10),16));				
				md.setData(21, Integer.parseInt(bfs.substring(18,20)+bfs.substring(16,18),16));
				handler.sendEmptyMessage(0);
			}
		}
	}
	
	private void appendText() {
		txtReception.setText(data);
	}
	

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			_timer.cancel();
			DetectActivity.this.finish();
		}
		return false;
	}
}
