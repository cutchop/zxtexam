package net.whzxt.zxtexam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class ChooseActivity extends Activity implements OnInitListener {

	private ListView listView;
	private TextView textView1;
	private Metadata md;
	private List<String> data;
	private Map<Integer, Integer> mapRouteID;
	private LocationManager locationManager;
	private double lat = 0f;
	private TextToSpeech mTts;
	private static final int REQ_TTS_STATUS_CHECK = 1;
	private Boolean needCheckGPS = true;

	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			if (needCheckGPS && lat != 0) {
				if (mTts != null) {
					needCheckGPS = false;
					mTts.speak("GPS定位成功,可以开始路考", TextToSpeech.QUEUE_FLUSH, null);
					Toast.makeText(ChooseActivity.this, "GPS定位成功,可以开始路考", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQ_TTS_STATUS_CHECK) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				mTts = new TextToSpeech(this, this);
			}
		} else {
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 200, 0, locationListener);
			Intent checkIntent = new Intent();
			checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
			startActivityForResult(checkIntent, REQ_TTS_STATUS_CHECK);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose);
		listView = (ListView) findViewById(R.id.listView1);
		textView1 = (TextView) findViewById(R.id.textView1);
		md = (Metadata) getApplication();
		if (md.isLargeText()) {
			textView1.setTextSize(28);
		}
		data = new ArrayList<String>();
		mapRouteID = new HashMap<Integer, Integer>();
		int i = 0;
		Cursor cursor = md.rawQuery("select routeid,name from " + DBer.T_ROUTE + " order by routeid");
		if (cursor.moveToFirst()) {
			do {
				data.add(cursor.getString(cursor.getColumnIndex("name")));
				mapRouteID.put(i, cursor.getInt(cursor.getColumnIndex("routeid")));
				i++;
			} while (cursor.moveToNext());
		}
		cursor.close();
		listView.setAdapter(new ArrayAdapter<String>(this, md.isLargeText() ? R.layout.choose_list_large : android.R.layout.simple_expandable_list_item_1, data));
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (data.get(arg2).indexOf("灯光") == -1) {
					if (lat == 0) {
						Toast.makeText(ChooseActivity.this, "正在等待GPS定位,请稍候,或者您可以模拟灯光考试", Toast.LENGTH_SHORT).show();
						if (mTts != null) {
							mTts.speak("正在等待GPS定位,请稍候,或者您可以模拟灯光考试", TextToSpeech.QUEUE_FLUSH, null);
						}
						return;
					}
				}
				if (mTts != null) {
					mTts.shutdown();
				}
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putInt("routeid", mapRouteID.get(arg2));
				bundle.putString("routename", data.get(arg2));
				intent.putExtras(bundle);
				intent.setClass(ChooseActivity.this, ExamActivity.class);
				startActivityForResult(intent, 0);
			}
		});
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 200, 0, locationListener);
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, REQ_TTS_STATUS_CHECK);
	}

	private final LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			if (location != null) {
				lat = location.getLatitude();
				runOnUiThread(new Runnable() {
					public void run() {
						if (needCheckGPS && lat != 0) {
							if (mTts != null) {
								needCheckGPS = false;
								mTts.speak("GPS定位成功,可以开始路考", TextToSpeech.QUEUE_FLUSH, null);
								Toast.makeText(ChooseActivity.this, "GPS定位成功,可以开始路考", Toast.LENGTH_SHORT).show();
							}
						}
					}
				});
			}
		}

		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			ChooseActivity.this.finish();
		}
		return false;
	}

	@Override
	public void onAttachedToWindow() {
		this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
		super.onAttachedToWindow();
	}
}
