package net.whzxt.zxtexam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.app.AlertDialog;
import android.app.Dialog;
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
	private CheckBox cbHideAuto;
	private TextView txtCurrentName;
	private ArrayList<HashMap<String, String>> errList;
	private ArrayList<HashMap<String, Object>> itemList, itemAllList;
	private Metadata md;
	private int fenshu;
	private Timer _timer, _timerSerial;
	private Date start;
	private TextView txtRouteName, txtTime, txtDefen;
	private Button btnRgpp, btnStop;
	private static final int REQ_TTS_STATUS_CHECK = 0;
	private LocationManager locationManager;
	private byte[] mBuffer;
	private int currId = 0;
	private ActionManager actionManager;
	private Boolean isAuto = false;
	private Boolean needCheckLight = false;
	private Boolean needCheckDevice = false;
	private Boolean islatlonMatched = false;
	private Boolean isMatching = false;
	private Map<Integer, Integer> itemNoMap;
	private WakeLock wakeLock;
	private final int DL_SEARCHING = 0x01;
	private final int DL_CONNECTING = 0x02;
	private final int DL_CHECKDEVICESTATUS = 0x03;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				refreshListView();
			} else {
				txtTime.setText("用时：" + getTimeDiff(start, new Date()));
				txtStatus.setText("经纬度:" + md.getLatLonString() + " 角度:" + md.getData(31) + "\n" + md.getName(20) + ":" + md.getData(20) + " " + md.getName(21) + ":" + md.getData(21) + "\n信号:" + md.get16DataString());
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
											if (itemAllList.size() > 0 && isAuto) {
												if (itemAllList.get(0).get("type").toString().equals("1")) {
													speak(itemAllList.get(0).get("tts").toString(), 0);
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
			if (isDeviceOK) {
				loadInit();
			} else {
				if (md.getDataResourceType() == 0) {
					showDialog(DL_CHECKDEVICESTATUS);
				}
				needCheckDevice = true;
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

	private void refreshListView() {
		listView.setAdapter(new ExamListAdapter(ExamActivity.this, errList));
		txtDefen.setText(String.valueOf(fenshu));
	}

	private void loadInit() {
		if (itemAllList.size() > 0) {
			if (Integer.parseInt(itemAllList.get(0).get("type").toString()) == 1) {
				if (md.getData(0) == 1 || md.getData(1) == 1 || md.getData(2) == 1 || md.getData(3) == 1 || md.getData(4) == 1 || md.getData(6) == 1 || md.getData(9) == 1) {
					needCheckLight = true;
					speak("请关闭所有灯光,准备考试");
				} else {
					speak(routeTts, -1);
				}
			} else {
				speak(routeTts);
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_exam);

		// 控制屏幕长亮
		PowerManager manager = ((PowerManager) getSystemService(POWER_SERVICE));
		wakeLock = manager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "ATAAW");
		wakeLock.acquire();

		start = new Date();
		txtRouteName = (TextView) findViewById(R.id.txtRouteName);
		txtTime = (TextView) findViewById(R.id.txtTime);
		txtDefen = (TextView) findViewById(R.id.txtDefen);
		listView = (ListView) findViewById(R.id.listView1);
		gridView = (GridView) findViewById(R.id.gridView1);
		btnRgpp = (Button) findViewById(R.id.btnRgpp);
		btnStop = (Button) findViewById(R.id.btnStop);
		txtStatus = (TextView) findViewById(R.id.txtStatus);
		cbHideAuto = (CheckBox) findViewById(R.id.cbHideAuto);
		txtCurrentName = (TextView) findViewById(R.id.txtCurrentName);
		md = (Metadata) getApplication();
		Bundle bundle = this.getIntent().getExtras();
		routeid = bundle.getInt("routeid");
		txtRouteName.setText("考试项目列表：(" + bundle.getString("routename") + ")");
		fenshu = 100;
		errList = new ArrayList<HashMap<String, String>>();
		itemList = new ArrayList<HashMap<String, Object>>();
		itemAllList = new ArrayList<HashMap<String, Object>>();
		itemNoMap = new HashMap<Integer, Integer>();
		actionManager = new ActionManager();
		actionManager.setOnStatusChange(new ActionManager.OnStatusChange() {
			public void onStop() {
				runOnUiThread(new Runnable() {
					public void run() {
						txtCurrentName.setText("");
					}
				});
				if (fenshu >= 90) {
					if (isAuto) {
						islatlonMatched = false;
						if (currId < itemAllList.size() - 1) {
							currId++;
							if (itemAllList.get(currId).get("type").toString().equals("1")) {
								try {
									Thread.sleep(2000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								if (!actionManager.IsRunning) {
									speak(itemAllList.get(currId).get("tts").toString(), currId);
								}
							} else {
								speak("完成");
							}
						} else {
							speak("考试合格,您的得分为:" + fenshu + "分");
						}
					} else {
						speak("完成");
					}
				} else {
					speak("考试不合格,您的扣分项目为," + errList.get(errList.size() - 1).get("errname") + ",请回中心打印成绩单");
				}
			}

			// 扣分
			public void onFault(List<Integer> list) {
				if (list.size() > 0) {
					for (int i = 0; i < list.size(); i++) {
						HashMap<String, String> map = new HashMap<String, String>();
						map.put("itemname", actionManager.getAction(list.get(i)).Itemname);
						map.put("fenshu", String.valueOf(actionManager.getAction(list.get(i)).Fenshu));
						map.put("errname", actionManager.getAction(list.get(i)).Err);
						errList.add(map);
						fenshu -= actionManager.getAction(list.get(i)).Fenshu;
					}
				}
				if (fenshu < 0) {
					fenshu = 0;
				}
				handler.sendEmptyMessage(1);
			}
		});

		Cursor cursor = md.rawQuery("select * from " + DBer.T_ROUTE + " where routeid=" + routeid);
		if (cursor.moveToFirst()) {
			routeTts = cursor.getString(cursor.getColumnIndex("tts"));
			isAuto = (cursor.getInt(cursor.getColumnIndex("auto")) == 1);
		}
		cursor.close();
		// ITEMS
		cursor = md.rawQuery("select a.itemid,a.lon,a.lat,b.name as itemname,b.tts,b.timeout,b.type from " + DBer.T_ROUTE_ITEM + " a left join " + DBer.T_ITEM + " b on a.itemid=b.itemid where a.routeid=" + routeid + " order by a.xuhao");
		int i, j;
		i = j = 0;
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
				if (cursor.getFloat(cursor.getColumnIndex("lon")) == 0 && cursor.getFloat(cursor.getColumnIndex("lat")) == 0) {
					itemList.add(map);
					itemNoMap.put(j, i);
					j++;
				}
				itemAllList.add(map);
				i++;
			} while (cursor.moveToNext());
		}
		cursor.close();
		gridView.setAdapter(new SimpleAdapter(ExamActivity.this, itemList, R.layout.gridlayout, new String[] { "itemname" }, new int[] { R.id.textView1 }));
		gridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (cbHideAuto.isChecked()) {
					arg2 = itemNoMap.get(arg2);
				}
				if (actionManager.IsRunning) {
					Toast.makeText(ExamActivity.this, "请稍候,另一个考试项目正在评判中", Toast.LENGTH_SHORT).show();
				} else {
					if (fenshu >= 90) {
						if (needCheckLight) {
							speak("请关闭所有灯光，准备考试");
						} else {
							currId = arg2;							
							speak(itemAllList.get(arg2).get("tts").toString(), arg2);
						}
					} else {
						speak("考试不合格");
					}
				}
			}
		});

		// 扣分
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {
				AlertDialog alertDialog = new AlertDialog.Builder(ExamActivity.this).setTitle("是否要取消这个扣分？").setIcon(android.R.drawable.ic_menu_help).setPositiveButton("是", new DialogInterface.OnClickListener() {
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
				String[] strs = new String[itemAllList.size()];
				for (int i = 0; i < itemAllList.size(); i++) {
					strs[i] = itemAllList.get(i).get("itemname").toString();
				}
				AlertDialog alertDialog = new AlertDialog.Builder(ExamActivity.this).setTitle("请选择项目").setIcon(android.R.drawable.ic_menu_add).setItems(strs, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						final String tmpitemname = itemAllList.get(which).get("itemname").toString();
						Cursor cursor = md.rawQuery("select * from " + DBer.T_ITEM_ERR + " where itemid=" + itemAllList.get(which).get("itemid"));
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
							AlertDialog dialog2 = new AlertDialog.Builder(ExamActivity.this).setTitle("请选择扣分项").setIcon(android.R.drawable.ic_menu_add).setItems(strs2, new DialogInterface.OnClickListener() {
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
		// 隐藏自动触发的项目
		cbHideAuto.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					gridView.setAdapter(new SimpleAdapter(ExamActivity.this, itemList, R.layout.gridlayout, new String[] { "itemname" }, new int[] { R.id.textView1 }));
				} else {
					gridView.setAdapter(new SimpleAdapter(ExamActivity.this, itemAllList, R.layout.gridlayout, new String[] { "itemname" }, new int[] { R.id.textView1 }));
				}
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

		if (_timerSerial == null) {
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
			}, 1000, 100);
		}

		// 计时
		if (_timer == null) {
			_timer = new Timer();
			_timer.schedule(new TimerTask() {
				@Override
				public void run() {
					handler.sendEmptyMessage(-1);
					// 检查设备是否就绪
					if (needCheckDevice) {
						if (isDeviceOK) {
							needCheckDevice = false;
							runOnUiThread(new Runnable() {
								public void run() {
									if (md.getDataResourceType() == 0) {
										dismissDialog(DL_CHECKDEVICESTATUS);
									}
									loadInit();
								}
							});
						}
					}
					// 灯光考试时检查是否已经关闭所有灯光，然后开始考试
					if (needCheckLight) {
						if (md.getData(0) == 0 && md.getData(1) == 0 && md.getData(2) == 0 && md.getData(3) == 0 && md.getData(4) == 0 && md.getData(6) == 0 && md.getData(9) == 0) {
							needCheckLight = false;
							speak(routeTts, -1);
						}
					}
				}
			}, 1000, 1000);
		}

		// TTS
		if (mTts == null) {
			Intent checkIntent = new Intent();
			checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
			startActivityForResult(checkIntent, REQ_TTS_STATUS_CHECK);
		}
		// GPS
		if (locationManager == null) {
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 200, 0, locationListener);
		}
	}

	private void execItem(int index) {
		Cursor cursor = md.rawQuery("select a.*,b.name as errname,b.fenshu from " + DBer.T_ITEM_ACTION + " a left join " + DBer.T_ITEM_ERR + " b on a.errid=b.errid where a.itemid=" + itemAllList.get(index).get("itemid"));
		if (cursor.moveToFirst()) {
			List<BaseAction> list = new ArrayList<BaseAction>();
			do {
				int dataid = cursor.getInt(cursor.getColumnIndex("dataid"));
				int times = cursor.getInt(cursor.getColumnIndex("times"));
				int min = cursor.getInt(cursor.getColumnIndex("min"));
				int max = cursor.getInt(cursor.getColumnIndex("max"));
				BaseAction action = actionManager.GetActionObject(md, dataid, times, max, min);
				action.Itemname = itemAllList.get(index).get("itemname").toString();
				if (cursor.getInt(cursor.getColumnIndex("errid")) != 999) {
					action.Err = cursor.getString(cursor.getColumnIndex("errname"));
					action.Fenshu = cursor.getInt(cursor.getColumnIndex("fenshu"));
				} else {
					action.Err = "";
					action.Fenshu = 0;
				}
				action.Step = cursor.getInt(cursor.getColumnIndex("step"));
				list.add(action);
			} while (cursor.moveToNext());
			cursor.close();
			txtCurrentName.setText(itemAllList.get(index).get("itemname").toString());
			actionManager.setTimeout(Integer.parseInt(itemAllList.get(index).get("timeout").toString()));
			actionManager.TotalPoints = fenshu;
			actionManager.setActions(list);
			actionManager.Start();
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
				if (!isMatching && !islatlonMatched && isAuto && !actionManager.IsRunning && location.getLatitude() != 0) {
					isMatching = true;
					for (int i = 0; i < itemAllList.size(); i++) {
						if (Float.parseFloat(itemAllList.get(i).get("lat").toString()) != 0f) {
							Location loa = new Location("reverseGeocoded");
							loa.setLatitude(Double.parseDouble(itemAllList.get(i).get("lat").toString()));
							loa.setLongitude(Double.parseDouble(itemAllList.get(i).get("lon").toString()));
							if (location.distanceTo(loa) < md.getRange()) {
								islatlonMatched = true;
								final int finai = i;
								runOnUiThread(new Runnable() {
									public void run() {
										speak(itemAllList.get(finai).get("tts").toString(), finai);
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
			Cursor cursor = md.rawQuery("select * from " + DBer.T_ITEM_ACTION + " where itemid=" + itemAllList.get(index).get("itemid"));
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
	protected Dialog onCreateDialog(int id, Bundle args) {
		AlertDialog dialog = null;
		switch (id) {
		case DL_SEARCHING:
			dialog = new AlertDialog.Builder(ExamActivity.this).setCancelable(false).setMessage("正在搜索无线设备...").setIcon(android.R.drawable.ic_dialog_info).create();
			break;
		case DL_CONNECTING:
			dialog = new AlertDialog.Builder(ExamActivity.this).setCancelable(false).setMessage("正在连接无线设备...").setIcon(android.R.drawable.ic_dialog_info).create();
			break;
		case DL_CHECKDEVICESTATUS:
			dialog = new AlertDialog.Builder(ExamActivity.this).setCancelable(false).setMessage("正在检测设备,请稍候...").setIcon(android.R.drawable.ic_dialog_info).create();
			break;
		default:
			return null;
		}
		return dialog;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			destroy();
			ExamActivity.this.finish();
		}
		return false;
	}

	@Override
	protected void destroy() {
		_timer.cancel();
		_timerSerial.cancel();
		_timer = null;
		_timerSerial = null;
		actionManager.destroy();
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
