package net.whzxt.zxtexam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
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
	private TextView txtStatus;
	private ArrayList<HashMap<String, String>> errList;
	private ArrayList<HashMap<String, Object>> itemList;
	private Metadata md;
	private int fenshu;
	private Timer _timer, _timerSerial;
	private Date start;
	private TextView txtRouteName, txtTime, txtDefen;
	private Button btnRgpp, btnStop;
	private TextToSpeech mTts;
	private static final int REQ_TTS_STATUS_CHECK = 0;
	private LocationManager locationManager;
	private byte[] mBuffer;
	private int currId = 0;
	private ItemManager itemManager;
	private Boolean isAuto = false;
	private Boolean needCheckLight = false;
	private Boolean islatlonMatched = false;
	private Boolean isMatching = false;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what > -1) {
				addListItem(msg.what);
			} else {
				txtTime.setText("用时：" + getTimeDiff(start, new Date()));
				txtStatus
						.setText("经纬度:" + md.getLatLonString() + " 角度:" + md.getData(31) + "\n" + md.getName(20) + ":" + md.getData(20) + " " + md.getName(21) + ":" + md.getData(21) + "\n信号:" + md.get16DataString());
			}
		}
	};

	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			mTts.setOnUtteranceCompletedListener(new OnUtteranceCompletedListener() {
				public void onUtteranceCompleted(final String utteranceId) {
					if (utteranceId != null) {
						if (Integer.parseInt(utteranceId) == currId) {
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
							if (Integer.parseInt(utteranceId) == -1) {
								// 自动执行
								runOnUiThread(new Runnable() {
									public void run() {
										try {
											if (itemList.size() > 0 && isAuto) {
												if (itemList.get(0).get("type").toString().equals("1")) {
													speak(itemList.get(0).get("tts").toString(), 0);
												}
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
			if (itemList.size() > 0) {
				if (Integer.parseInt(itemList.get(0).get("itemid").toString()) > 10 && Integer.parseInt(itemList.get(0).get("itemid").toString()) < 21) {
					if (md.getData(0) == 1 || md.getData(1) == 1 || md.getData(2) == 1 || md.getData(3) == 1 || md.getData(4) == 1 || md.getData(6) == 1 || md.getData(9) == 1) {
						needCheckLight = true;
						speak("请关闭所有灯光,准备考试");
					} else {
						speak(routeTts, -1);
					}
				} else {
					speak(routeTts, -1);
				}
			}
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQ_TTS_STATUS_CHECK) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				mTts = new TextToSpeech(this, this);
			}
		}
	}

	private void addListItem(int index) {
		listView.setAdapter(new ExamListAdapter(ExamActivity.this, errList));
		txtDefen.setText(String.valueOf(fenshu));
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
		txtStatus = (TextView) findViewById(R.id.txtStatus);
		md = (Metadata) getApplication();
		Bundle bundle = this.getIntent().getExtras();
		routeid = bundle.getInt("routeid");
		txtRouteName.setText("考试项目列表：(" + bundle.getString("routename") + ")");
		fenshu = 100;
		errList = new ArrayList<HashMap<String, String>>();
		itemList = new ArrayList<HashMap<String, Object>>();
		itemManager = new ItemManager(new ItemManager.OnStatusChange() {
			public void onStop() {
				if (isAuto && fenshu >= 90) {
					islatlonMatched = false;// 继续匹配经纬度
				}
				if (isAuto && currId < itemList.size() - 1 && fenshu >= 90) {
					if (itemList.get(currId).get("type").toString().equals("1")) {
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if (itemManager._pause) {
							currId++;
							speak(itemList.get(currId).get("tts").toString(), currId);
						}
					}
				} else {
					if (fenshu < 90) {
						speak("考试不合格,您的扣分项目为," + errList.get(errList.size() - 1).get("errname") + ",请回中心打印成绩单");
					} else {
						if (!isAuto) {
							speak("完成");
						} else {
							speak("考试合格,请继续完成考试");
						}
					}
				}
			}

			// 扣分
			public void onFault(int index) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("itemname", itemManager._listActions.get(index).Itemname);
				map.put("fenshu", String.valueOf(itemManager._listActions.get(index).Fenshu));
				map.put("errname", itemManager._listActions.get(index).Err);
				errList.add(map);
				fenshu -= itemManager._listActions.get(index).Fenshu;
				if (fenshu < 0) {
					fenshu = 0;
				}
				handler.sendEmptyMessage(index);
			}
		}, md);

		Cursor cursor = md.rawQuery("select * from " + DBer.T_ROUTE + " where routeid=" + routeid);
		if (cursor.moveToFirst()) {
			routeTts = cursor.getString(cursor.getColumnIndex("tts"));
			isAuto = (cursor.getInt(cursor.getColumnIndex("auto")) == 1);
		}
		cursor.close();
		// ITEMS
		cursor = md.rawQuery("select a.itemid,a.lon,a.lat,b.name as itemname,b.tts,b.timeout,b.type from " + DBer.T_ROUTE_ITEM + " a left join " + DBer.T_ITEM
				+ " b on a.itemid=b.itemid where a.routeid=" + routeid + " order by a.xuhao");
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
				map.put("type", cursor.getInt(cursor.getColumnIndex("type")));
				itemList.add(map);
			} while (cursor.moveToNext());
		}
		cursor.close();
		gridView.setAdapter(new SimpleAdapter(ExamActivity.this, itemList, R.layout.gridlayout, new String[] { "itemname" }, new int[] { R.id.textView1 }));
		gridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (itemManager._pause) {
					if (fenshu >= 90) {
						if (needCheckLight) {
							speak("请关闭所有灯光，准备考试");
						} else {
							currId = arg2;
							@SuppressWarnings("unchecked")
							HashMap<String, Object> map = (HashMap<String, Object>) gridView.getItemAtPosition(arg2);
							speak(map.get("tts").toString(), arg2);
						}
					} else {
						speak("考试不合格");
					}
				} else {
					Toast.makeText(ExamActivity.this, "请稍候,另一个考试项目正在评判中", Toast.LENGTH_SHORT).show();
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
				String[] strs = new String[itemList.size()];
				for (int i = 0; i < itemList.size(); i++) {
					strs[i] = itemList.get(i).get("itemname").toString();
				}
				AlertDialog alertDialog = new AlertDialog.Builder(ExamActivity.this).setTitle("请选择项目").setIcon(android.R.drawable.ic_menu_add).setItems(strs, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						final String tmpitemname = itemList.get(which).get("itemname").toString();
						Cursor cursor = md.rawQuery("select * from " + DBer.T_ITEM_ERR + " where itemid=" + itemList.get(which).get("itemid"));
						if (cursor.moveToFirst()) {
							final String[] strs2 = new String[cursor.getCount()];
							final String[] fenshus = new String[cursor.getCount()];
							final String[] errnames = new String[cursor.getCount()];
							int k = 0;
							do {
								strs2[k] = "[" + cursor.getString(cursor.getColumnIndex("fenshu")) + "分]" + " " + cursor.getString(cursor.getColumnIndex("name"));
								errnames[k] = cursor.getString(cursor.getColumnIndex("name"));
								fenshus[k] = cursor.getString(cursor.getColumnIndex("fenshu"));
								k++;
							} while (cursor.moveToNext());
							AlertDialog dialog2 = new AlertDialog.Builder(ExamActivity.this).setTitle("请选择扣分项").setIcon(android.R.drawable.ic_menu_add)
									.setItems(strs2, new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											HashMap<String, String> map = new HashMap<String, String>();
											map.put("itemname", tmpitemname);
											map.put("fenshu", fenshus[which]);
											map.put("errname", errnames[which]);
											errList.add(map);
											fenshu -= Integer.parseInt(fenshus[which]);
											if (fenshu < 0) {
												fenshu = 0;
											}
											if (fenshu < 90) {
												speak("考试不合格,扣分项目为," + strs2[which]);
											}
											listView.setAdapter(new ExamListAdapter(ExamActivity.this, errList));
											txtDefen.setText(String.valueOf(fenshu));
										}
									}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											return;
										}
									}).create();
							dialog2.show();
						} else {
							Toast.makeText(ExamActivity.this, "该项目没有扣分项", Toast.LENGTH_SHORT).show();
						}
					}
				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						return;
					}
				}).create();
				alertDialog.show();
			}
		});
		// 结束考试
		btnStop.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				destroy();
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
		}, 1000, 50);

		// 计时
		_timer = new Timer();
		_timer.schedule(new TimerTask() {
			@Override
			public void run() {
				handler.sendEmptyMessage(-1);
				// 灯光考试时检查是否已经关闭所有灯光，然后开始考试
				if (needCheckLight) {
					if (md.getData(0) == 0 && md.getData(1) == 0 && md.getData(2) == 0 && md.getData(3) == 0 && md.getData(4) == 0 && md.getData(6) == 0 && md.getData(9) == 0) {
						needCheckLight = false;
						speak(routeTts, -1);
					}
				}
			}
		}, 1000, 1000);

		// TTS
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, REQ_TTS_STATUS_CHECK);
		// GPS
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 200, 0, locationListener);
	}

	private void execItem(int index) {
		Cursor cursor = md.rawQuery("select a.*,b.name as errname,b.fenshu from " + DBer.T_ITEM_ACTION + " a left join " + DBer.T_ITEM_ERR + " b on a.errid=b.errid where a.itemid="
				+ itemList.get(index).get("itemid"));
		if (cursor.moveToFirst()) {
			if (itemManager._listActions == null) {
				itemManager._listActions = new ArrayList<Action>();
			}
			itemManager._listActions.clear();
			do {
				Action action = new Action();
				action.Itemname = itemList.get(index).get("itemname").toString();
				action.Dataid = cursor.getInt(cursor.getColumnIndex("dataid"));
				action.Times = cursor.getInt(cursor.getColumnIndex("times"));
				action.Min = cursor.getInt(cursor.getColumnIndex("min"));
				action.Max = cursor.getInt(cursor.getColumnIndex("max"));
				action.Err = cursor.getString(cursor.getColumnIndex("errname"));
				action.Fenshu = cursor.getInt(cursor.getColumnIndex("fenshu"));
				action.Step = cursor.getInt(cursor.getColumnIndex("step"));
				action.IsOK = false;
				if (action.Dataid < 20) {
					if (Math.abs(action.Times) == 1) {
						action.IsOK = true;
					}
				} else {
					if (Math.abs(action.Max) > 0) {
						action.IsOK = true;
					}
				}
				itemManager._listActions.add(action);
			} while (cursor.moveToNext());
			cursor.close();
			itemManager.setTimeout(Integer.parseInt(itemList.get(index).get("timeout").toString()));
			itemManager.setFenshu(fenshu);
			itemManager.Start();
		}
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
		if (isAuto && fenshu >= 90) {
			islatlonMatched = false;// 继续匹配经纬度
		}
	}

	private final LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			if (location != null) {
				md.setGPSSpeed(location.getSpeed());
				md.setGPSLatlon((float) location.getLatitude(), (float) location.getLongitude());
				md.setData(31, Math.round(location.getBearing()));
				if (!isMatching && !islatlonMatched && isAuto && itemManager._pause && location.getLatitude() != 0) {
					isMatching = true;
					for (int i = 0; i < itemList.size(); i++) {
						if (Float.parseFloat(itemList.get(i).get("lat").toString()) != 0f) {
							Location loa = new Location("reverseGeocoded");
							loa.setLatitude(Double.parseDouble(itemList.get(i).get("lat").toString()));
							loa.setLongitude(Double.parseDouble(itemList.get(i).get("lon").toString()));
							if (location.distanceTo(loa) < md.getRange()) {
								islatlonMatched = true;
								final int finai = i;
								runOnUiThread(new Runnable() {
									public void run() {
										speak(itemList.get(finai).get("tts").toString(), finai);
									}
								});
								break;
							}
						}
					}
					isMatching = false;
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
				HashMap<String, String> myHashAlarm = new HashMap<String, String>();
				myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, String.valueOf(index));
				mTts.speak(str, TextToSpeech.QUEUE_FLUSH, myHashAlarm);
			} else {
				Toast.makeText(ExamActivity.this, "该项目还没有设置评判条件,请在[系统设置]-[项目设置]里设置", Toast.LENGTH_SHORT).show();
			}
		} else {
			HashMap<String, String> myHashAlarm = new HashMap<String, String>();
			myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "-1");
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
			t2 = Integer.parseInt(data.substring(26, 28) + data.substring(24, 26), 16);
			if (t1 != t2) {
				return;
			}
			// debug
			if (Integer.parseInt(data.substring(28, 30) + data.substring(30, 32), 16) > 0) {
				md.setData(31, Integer.parseInt(data.substring(28, 30) + data.substring(30, 32), 16));
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
			destroy();
			ExamActivity.this.finish();
		}
		return false;
	}

	private void destroy() {
		_timer.cancel();
		_timerSerial.cancel();
		_timer = null;
		_timerSerial = null;
		itemManager.Destroy();
		if (mTts != null) {
			try {
				mTts.shutdown();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
