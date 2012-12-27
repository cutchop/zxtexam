package net.whzxt.zxtexam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class RouteEditActivity extends Activity {

	private EditText txtName, txtTts;
	private Button btnName, btnTts;
	private ListView listView;
	private Metadata md;
	private int routeid;
	private List<String> data;
	private Map<String, Integer> map;
	private Map<Integer, Integer> mapItems;
	private String[] strItems;
	private LocationManager locationManager;
	private Boolean bdjwd = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route_edit);
		md = (Metadata) getApplication();
		txtName = (EditText) findViewById(R.id.txtName);
		txtTts = (EditText) findViewById(R.id.txtTts);
		btnName = (Button) findViewById(R.id.btnName);
		btnTts = (Button) findViewById(R.id.btnTts);
		listView = (ListView) findViewById(R.id.listView1);

		Bundle bundle = this.getIntent().getExtras();
		routeid = bundle.getInt("routeid");

		if (routeid > -1) {
			Cursor cursor = md.rawQuery("select * from " + DBer.T_ROUTE + " where routeid=" + routeid);
			if (cursor.moveToFirst()) {
				txtName.setText(cursor.getString(cursor.getColumnIndex("name")));
				txtTts.setText(cursor.getString(cursor.getColumnIndex("tts")));
			}
			cursor.close();
		} else {
			txtName.setEnabled(true);
			btnName.setText("保存");
		}

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);

		btnName.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (btnName.getText().equals("修改")) {
					txtName.setEnabled(true);
					btnName.setText("保存");
				} else {
					if (txtName.getText().toString().trim().equals("")) {
						Toast.makeText(RouteEditActivity.this, "请填写路线名称", Toast.LENGTH_SHORT).show();
						return;
					}
					if (routeid == -1) {
						Cursor cursor = md.rawQuery("select max(routeid) as id from " + DBer.T_ROUTE);
						if (cursor.moveToFirst()) {
							routeid = cursor.getInt(0);
						}
						cursor.close();
						if (routeid > -1) {
							routeid++;
							md.execSQL("insert into " + DBer.T_ROUTE + "(routeid,name,tts) values(" + routeid + ",'" + txtName.getText() + "','')");
							btnName.setText("修改");
							txtName.setEnabled(false);
						} else {
							Toast.makeText(RouteEditActivity.this, "路线保存失败", Toast.LENGTH_SHORT).show();
						}
					} else {
						md.execSQL("update " + DBer.T_ROUTE + " set name='" + txtName.getText() + "' where routeid=" + routeid);
						btnName.setText("修改");
						txtName.setEnabled(false);
					}
				}
			}
		});
		btnTts.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (btnTts.getText().equals("修改")) {
					if (routeid == -1) {
						Toast.makeText(RouteEditActivity.this, "请先保存路线名称", Toast.LENGTH_SHORT).show();
					} else {
						txtTts.setEnabled(true);
						btnTts.setText("保存");
					}
				} else {
					if (txtTts.getText().toString().trim().equals("")) {
						Toast.makeText(RouteEditActivity.this, "请填写语音提示文本", Toast.LENGTH_SHORT).show();
						return;
					}
					md.execSQL("update " + DBer.T_ROUTE + " set tts='" + txtTts.getText() + "' where routeid=" + routeid);
					btnTts.setText("修改");
					txtTts.setEnabled(false);
				}
			}
		});

		data = new ArrayList<String>();
		map = new HashMap<String, Integer>();
		mapItems = new HashMap<Integer, Integer>();

		Cursor cursor = md.rawQuery("select * from " + DBer.T_ITEM);
		if (cursor.moveToFirst()) {
			strItems = new String[cursor.getCount()];
			int i = 0;
			do {
				strItems[i] = cursor.getString(cursor.getColumnIndex("name"));
				mapItems.put(i, cursor.getInt(cursor.getColumnIndex("itemid")));
				i++;
			} while (cursor.moveToNext());
		}
		cursor.close();
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				final String itemname = data.get(arg2);
				if (map.get(itemname) == null) {
					if (routeid == -1) {
						Toast.makeText(RouteEditActivity.this, "请先保存路线名称", Toast.LENGTH_SHORT).show();
					} else {
						bdjwd = true;
						if (md.getLatlon()[0] != 0) {
							String[] strs = { "绑定当前位置:" + md.getLatLonString(), "不绑定位置信息" };
							AlertDialog alertDialog = new AlertDialog.Builder(RouteEditActivity.this).setTitle("是否要绑定位置信息?").setIcon(android.R.drawable.ic_menu_help)
									.setItems(strs, new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											if (which == 1) {
												bdjwd = false;
											}
											AlertDialog dialog2 = new AlertDialog.Builder(RouteEditActivity.this).setTitle("请选择要添加的项目").setIcon(android.R.drawable.ic_menu_add).setItems(strItems, onselect)
													.setNegativeButton("取消", new DialogInterface.OnClickListener() {
														public void onClick(DialogInterface dialog, int which) {
															return;
														}
													}).create();
											dialog2.show();
										}
									}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											return;
										}
									}).create();
							alertDialog.show();
						} else {
							AlertDialog alertDialog = new AlertDialog.Builder(RouteEditActivity.this).setTitle("没有获取到位置信息,请检查GPS").setMessage("添加的项目将不会绑定位置信息，是否继续?").setIcon(android.R.drawable.ic_menu_help)
									.setPositiveButton("继续", new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											bdjwd = false;
											AlertDialog dialog2 = new AlertDialog.Builder(RouteEditActivity.this).setTitle("请选择要添加的项目").setIcon(android.R.drawable.ic_menu_add).setItems(strItems, onselect)
													.setNegativeButton("取消", new DialogInterface.OnClickListener() {
														public void onClick(DialogInterface dialog, int which) {
															return;
														}
													}).create();
											dialog2.show();
										}
									})
									.setNegativeButton("取消", new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											return;
										}
									}).create();
							alertDialog.show();
						}
					}
				} else {
					AlertDialog alertDialog = new AlertDialog.Builder(RouteEditActivity.this).setTitle("是否要删除该项目？").setIcon(android.R.drawable.ic_menu_help)
							.setPositiveButton("是", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									md.execSQL("delete from " + DBer.T_ROUTE_ITEM + " where routeid=" + routeid + " and itemid=" + map.get(itemname));
									load();
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
			}
		});
		load();
	}

	private void load() {
		data.clear();
		map.clear();
		Cursor cursor = md.rawQuery("select a.itemid,b.name from " + DBer.T_ROUTE_ITEM + " a left join " + DBer.T_ITEM + " b on a.itemid=b.itemid where a.routeid=" + routeid);
		if (cursor.moveToFirst()) {
			do {
				data.add(cursor.getString(cursor.getColumnIndex("name")));
				map.put(cursor.getString(cursor.getColumnIndex("name")), cursor.getInt(cursor.getColumnIndex("itemid")));
			} while (cursor.moveToNext());
		}
		cursor.close();
		data.add("添加项目...");
		listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, data));
	}

	private DialogInterface.OnClickListener onselect = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			float[] latlon = md.getLatlon();
			if (!bdjwd) {
				latlon[0] = 0;
				latlon[1] = 0;
			}
			md.execSQL("insert into " + DBer.T_ROUTE_ITEM + "(routeid,itemid,lon,lat) values(" + routeid + "," + mapItems.get(which) + "," + latlon[1] + "," + latlon[0] + ")");
			load();
		}
	};

	private final LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			if (location != null) {
				md.setGPSSpeed(location.getSpeed());
				md.setGPSLatlon((float) location.getLatitude(), (float) location.getLongitude());
				md.setData(31, Math.round(location.getBearing()));
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

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			this.setResult(RESULT_OK);
			this.finish();
		}
		return false;
	}

}
