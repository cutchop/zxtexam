package net.whzxt.zxtexam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.w3c.dom.ls.LSException;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;

public class ExamActivity extends SerialPortActivity implements OnInitListener {

	private int routeid;
	private String routeTts;
	private ListView listView;
	private GridView gridView;
	private ArrayList<HashMap<String, Object>> errList;
	private ArrayList<HashMap<String, Object>> itemList;
	private Metadata md;
	private int fenshu;
	private Timer _timer, _timerSerial;
	private Date start;
	private TextView txtRouteName, txtTime, txtDefen;
	private Button btnRgpp, btnStop;
	private ArrayList<Action> listActions;
	private int _timeout = 0;
	private HashMap<Integer, Integer> hashdata;
	private int startAngle = 0;
	private TextToSpeech mTts;
	private static final int REQ_TTS_STATUS_CHECK = 0;
	private LocationManager locationManager;
	private Boolean execing = false;
	private byte[] mBuffer;
	private int currId = -1;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what >= 0) {
				addListItem(msg.what);
			} else {
				txtTime.setText("用时：" + getTimeDiff(start, new Date()));
			}
		}
	};

	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			speak(routeTts, -1);
			mTts.setOnUtteranceCompletedListener(new OnUtteranceCompletedListener() {
				public void onUtteranceCompleted(final String utteranceId) {
					if (utteranceId != null) {
						if (Integer.parseInt(utteranceId) > -1) {
							runOnUiThread(new Runnable() {
								public void run() {
									try {
										execItem(Integer.parseInt(utteranceId));
									} catch (NumberFormatException e) {
										e.printStackTrace();
									}
								}
							});
						} else {
							if (routeid == 1) {
								// 灯光自动执行
								runOnUiThread(new Runnable() {
									public void run() {
										try {
											if (itemList.size() > 0) {
												speak(itemList.get(0).get("tts").toString(), 0);
											}
										} catch (NumberFormatException e) {
											e.printStackTrace();
										}
									}
								});
							}
						}
					}
				}
			});
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQ_TTS_STATUS_CHECK) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				mTts = new TextToSpeech(this, this);
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_exam);
		start = new Date();
		txtRouteName = (TextView) findViewById(R.id.txtRouteName);
		txtTime = (TextView) findViewById(R.id.txtTime);
		txtDefen = (TextView) findViewById(R.id.txtDefen);
		listView = (ListView) findViewById(R.id.listView1);
		gridView = (GridView) findViewById(R.id.gridView1);
		btnRgpp = (Button) findViewById(R.id.btnRgpp);
		btnStop = (Button) findViewById(R.id.btnStop);
		md = (Metadata) getApplication();
		Bundle bundle = this.getIntent().getExtras();
		routeid = bundle.getInt("routeid");
		txtRouteName.setText("考试项目列表：(" + bundle.getString("routename") + ")");
		fenshu = 100;
		listActions = new ArrayList<Action>();
		errList = new ArrayList<HashMap<String, Object>>();
		itemList = new ArrayList<HashMap<String, Object>>();
		hashdata = new HashMap<Integer, Integer>();
		resetdata();

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);

		Cursor cursor = md.rawQuery("select * from " + DBer.T_ROUTE + " where routeid=" + routeid);
		if (cursor.moveToFirst()) {
			routeTts = cursor.getString(cursor.getColumnIndex("tts"));
		}
		cursor.close();
		// TTS
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, REQ_TTS_STATUS_CHECK);

		// ITEMS
		cursor = md.rawQuery("select a.itemid,a.lon,a.lat,b.name as itemname,b.tts,b.timeout from " + DBer.T_ROUTE_ITEM + " a left join " + DBer.T_ITEM + " b on a.itemid=b.itemid where a.routeid="
				+ routeid);
		if (cursor.moveToFirst()) {
			HashMap<String, Object> map = null;
			do {
				map = new HashMap<String, Object>();
				map.put("itemid", cursor.getInt(cursor.getColumnIndex("itemid")));
				map.put("itemname", cursor.getString(cursor.getColumnIndex("itemname")));
				map.put("tts", cursor.getString(cursor.getColumnIndex("tts")));
				map.put("lon", cursor.getFloat(cursor.getColumnIndex("lon")));
				map.put("lat", cursor.getFloat(cursor.getColumnIndex("lat")));
				map.put("timeout", cursor.getInt(cursor.getColumnIndex("timeout")));
				itemList.add(map);
			} while (cursor.moveToNext());
		}
		cursor.close();
		gridView.setAdapter(new SimpleAdapter(ExamActivity.this, itemList, R.layout.gridlayout, new String[] { "itemname" }, new int[] { R.id.textView1 }));
		gridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (execing) {
					Toast.makeText(ExamActivity.this, "请稍候,另一个考试项目正在评判中", Toast.LENGTH_SHORT).show();
				} else {
					@SuppressWarnings("unchecked")
					HashMap<String, Object> map = (HashMap<String, Object>) gridView.getItemAtPosition(arg2);
					speak(map.get("tts").toString(), arg2);
				}
			}
		});

		// 扣分
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {
				AlertDialog alertDialog = new AlertDialog.Builder(ExamActivity.this).setTitle("是否要取消这个扣分？").setIcon(android.R.drawable.ic_menu_help)
						.setPositiveButton("是", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								delListItem(arg2);
							}
						}).setNegativeButton("否", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								return;
							}
						}).create();
				alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						if (keyCode == KeyEvent.KEYCODE_HOME)
							return true;
						return false;
					}
				});
				alertDialog.show();
				alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
			}
		});

		// 人工评判
		btnRgpp.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

			}
		});
		// 结束考试
		btnStop.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				_timer.cancel();
				_timerSerial.cancel();
				ExamActivity.this.finish();
			}
		});

		mBuffer = new byte[7];
		mBuffer[0] = 0x1A;
		mBuffer[1] = 0x01;
		mBuffer[2] = 0x00;
		mBuffer[3] = 0x00;
		mBuffer[4] = 0x00;
		mBuffer[5] = 0x00;
		mBuffer[6] = 0x1D;

		_timerSerial = new Timer();
		_timerSerial.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					if (mOutputStream != null) {
						mOutputStream.write(mBuffer);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}, 50, 50);

		_timer = new Timer();
		_timer.schedule(new TimerTask() {
			@Override
			public void run() {
				handler.sendEmptyMessage(-1);
				if (_timeout > 0) {
					_timeout--;
					if (_timeout == 0) {
						for (int i = 0; i < listActions.size(); i++) {
							if (listActions.get(i).getDataid() < 20) {
								if (hashdata.get(listActions.get(i).getDataid()) < listActions.get(i).getTimes()) {
									handler.sendEmptyMessage(i);
								}
							} else if (listActions.get(i).getDataid() == 31) {
								if (listActions.get(i).getMin() > 0) {
									if (hashdata.get(31) < listActions.get(i).getMin()) {
										handler.sendEmptyMessage(i);
									}
								}
							}
						}
						execing = false;
					} else {
						for (int i = 0; i < listActions.size(); i++) {
							if (listActions.get(i).getDataid() < 20) {
								if (listActions.get(i).getTimes() == 0) {
									if (md.getData(listActions.get(i).getDataid()) == 1) {
										handler.sendEmptyMessage(i);
									}
								} else if (listActions.get(i).getTimes() == 9) {
									if (md.getData(listActions.get(i).getDataid()) == 0) {
										handler.sendEmptyMessage(i);
									}
								} else {
									hashdata.put(listActions.get(i).getDataid(), hashdata.get(listActions.get(i).getDataid()) + md.getData(listActions.get(i).getDataid()));
								}
							} else {
								if (listActions.get(i).getDataid() == 31) {
									// GPS角度偏差
									if (hashdata.get(31) == -1) {
										startAngle = md.getData(31);
										hashdata.put(31, 0);
									} else {
										if (listActions.get(i).getMax() > 0) {
											if (md.getData(31) - startAngle > listActions.get(i).getMax()) {
												handler.sendEmptyMessage(i);
											}
										} else {
											if (md.getData(31) - startAngle > hashdata.get(31)) {
												hashdata.put(31, md.getData(31) - startAngle);
											}
										}
									}
								} else {
									// 速度、转速、GPS速度
									if (listActions.get(i).getMax() > 0) {
										if (md.getData(listActions.get(i).getDataid()) > listActions.get(i).getMax()) {
											handler.sendEmptyMessage(i);
										}
									}
									if (listActions.get(i).getMin() > 0) {
										if (md.getData(listActions.get(i).getDataid()) < listActions.get(i).getMin()) {
											handler.sendEmptyMessage(i);
										}
									}
								}
							}
						}
					}
				} else {
					if (!execing && routeid == 1 && currId > -1) {
						if (itemList.size() > currId + 1) {
							runOnUiThread(new Runnable() {
								public void run() {
									try {
										speak(itemList.get(currId + 1).get("tts").toString(), currId + 1);
									} catch (NumberFormatException e) {
										e.printStackTrace();
									}
								}
							});
						}
					}
				}
			}
		}, 1000, 1000);
	}

	private void resetdata() {
		for (int i = 0; i < 16; i++) {
			hashdata.put(i, 0);
		}
		hashdata.put(31, -1);
	}

	private void execItem(int index) {
		listActions.clear();
		@SuppressWarnings("unchecked")
		HashMap<String, Object> map = (HashMap<String, Object>) gridView.getItemAtPosition(index);
		Cursor cursor = md.rawQuery("select a.*,b.name as errname,b.fenshu from " + DBer.T_ITEM_ACTION + " a left join " + DBer.T_ITEM_ERR + " b on a.errid=b.errid where a.itemid="
				+ map.get("itemid"));
		if (cursor.moveToFirst()) {
			do {
				Action action = new Action();
				if (routeid == 1) {
					action.setItemname("灯光");
				} else {
					action.setItemname(map.get("itemname").toString());
				}
				action.setDataid(cursor.getInt(cursor.getColumnIndex("dataid")));
				action.setTimes(cursor.getInt(cursor.getColumnIndex("times")));
				action.setMin(cursor.getInt(cursor.getColumnIndex("min")));
				action.setMax(cursor.getInt(cursor.getColumnIndex("max")));
				action.setErr(cursor.getString(cursor.getColumnIndex("errname")));
				action.setFenshu(cursor.getInt(cursor.getColumnIndex("fenshu")));
				listActions.add(action);
			} while (cursor.moveToNext());
			cursor.close();
			resetdata();
			this._timeout = Integer.parseInt(map.get("timeout").toString());
			Log.i("exam", "start execute " + map.get("itemname").toString() + ",timeout:" + this._timeout);
		} else {
			execing = false;
		}
	}

	private void addListItem(int index) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("itemname", listActions.get(index).getItemname());
		map.put("fenshu", listActions.get(index).getFenshu());
		map.put("errname", listActions.get(index).getErr());
		errList.add(map);
		ExamListAdapter adapter = new ExamListAdapter(ExamActivity.this, errList);
		listView.setAdapter(adapter);
		fenshu -= listActions.get(index).getFenshu();
		if (fenshu < 0) {
			fenshu = 0;
		}
		if (fenshu < 90) {
			if (_timeout > 1) {
				_timeout = 1;
			}
			speak("考试不合格,您的扣分项目为," + listActions.get(index).getErr());
		}
		txtDefen.setText(String.valueOf(fenshu));
	}

	private void delListItem(int index) {
		errList.remove(index);
		ExamListAdapter adapter = new ExamListAdapter(ExamActivity.this, errList);
		listView.setAdapter(adapter);
		fenshu = 100;
		for (int i = 0; i < errList.size(); i++) {
			fenshu = fenshu - Integer.parseInt(errList.get(i).get("fenshu").toString());
		}
		if (fenshu < 0) {
			fenshu = 0;
		}
		txtDefen.setText(String.valueOf(fenshu));
	}

	private final LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			if (location != null) {
				md.setGPSSpeed(location.getSpeed());
				md.setGPSLatlon((float) location.getLatitude(), (float) location.getLongitude());
				md.setData(31, Math.round(location.getBearing()));
				if (!execing && location.getLatitude() != 0) {
					for (int i = 0; i < itemList.size(); i++) {
						if (Float.parseFloat(itemList.get(i).get("lat").toString()) != 0f) {
							Location loa = new Location("reverseGeocoded");
							loa.setLatitude((Double) itemList.get(i).get("lat"));
							loa.setLongitude((Double) itemList.get(i).get("lon"));
							if (location.distanceTo(loa) < md.getRange()) {
								speak(itemList.get(i).get("tts").toString(), i);
								break;
							}
						}
					}
				}
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

	private String getTimeDiff(Date start, Date end) {
		int between = (int) (end.getTime() - start.getTime()) / 1000;
		int hour = between / 3600;
		int minute = between % 3600 / 60;
		int second = between % 60;
		return String.format("%02d:%02d:%02d", hour, minute, second);
	}

	private void speak(String str) {
		if (mTts != null) {
			try {
				mTts.speak(str, TextToSpeech.QUEUE_FLUSH, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void speak(String str, int index) {
		if (index > -1) {
			Cursor cursor = md.rawQuery("select * from " + DBer.T_ITEM_ACTION + " where itemid=" + itemList.get(index).get("itemid"));
			if (cursor.getCount() > 0) {
				execing = true;
				currId = index;
				HashMap<String, String> myHashAlarm = new HashMap<String, String>();
				myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, String.valueOf(index));
				mTts.speak(str, TextToSpeech.QUEUE_FLUSH, myHashAlarm);
			} else {
				currId = itemList.size();
				Toast.makeText(ExamActivity.this, "该项目还没有设置评判条件,请在[系统设置]-[项目设置]里设置", Toast.LENGTH_SHORT).show();
			}
		} else {
			HashMap<String, String> myHashAlarm = new HashMap<String, String>();
			myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, String.valueOf(index));
			mTts.speak(str, TextToSpeech.QUEUE_FLUSH, myHashAlarm);
		}
	}

	@Override
	protected void onDataReceived(byte[] buffer, int size) {
		if (buffer[0] == 0x1A && buffer[size - 1] == 0x1D) {
			String data = "";
			for (int i = 0; i < size; i++) {
				if (Integer.toHexString(buffer[i] & 0xFF).length() == 1) {
					data += "0" + Integer.toHexString(buffer[i] & 0xFF);
				} else {
					data += Integer.toHexString(buffer[i] & 0xFF);
				}
			}
			data = data.toUpperCase();
			data = data.replace("1B11", "1A").replace("1B14", "1D").replace("1B0B", "1B");
			int t1 = 0;
			int t2 = 0;
			for (int i = 2; i < 20; i++) {
				t1 += Integer.parseInt(data.substring(i, i + 2), 16);
				i++;
			}
			for (int i = 24; i < 32; i++) {
				t2 += Integer.parseInt(data.substring(i, i + 2), 16);
				i++;
			}
			if (t1 != t2) {
				return;
			}
			if (data.substring(2, 4).equals("02")) {
				String str = md.toBinaryString(Integer.parseInt(data.substring(4, 6), 16));
				for (int i = 0; i < str.length(); i++) {
					if (str.substring(i, i + 1).equals("1")) {
						md.setData(7 - i, 1);
					} else {
						md.setData(7 - i, 0);
					}
				}
				str = md.toBinaryString(Integer.parseInt(data.substring(6, 8), 16));
				for (int i = 0; i < str.length(); i++) {
					if (str.substring(i, i + 1).equals("1")) {
						md.setData(15 - i, 1);
					} else {
						md.setData(15 - i, 0);
					}
				}
				md.setData(20, Integer.parseInt(data.substring(10, 12) + data.substring(8, 10), 16));
				md.setData(21, Integer.parseInt(data.substring(18, 20) + data.substring(16, 18), 16));
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			_timer.cancel();
			_timerSerial.cancel();
			_timer = null;
			_timerSerial = null;
			ExamActivity.this.finish();
		}
		return false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
