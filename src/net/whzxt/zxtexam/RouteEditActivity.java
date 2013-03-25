package net.whzxt.zxtexam;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class RouteEditActivity extends Activity {

	private EditText txtName, txtTts;
	private Button btnName, btnTts;
	private ListView listView;
	private RadioButton radAutoTrue, radAutoFalse;
	private RadioButton radStopTrue;
	private Metadata md;
	private int routeid;
	private List<String> data;
	private List<Integer> itemids,xuhaos;
	private LocationManager locationManager;
	private static Integer[] itemids_0 = { 1, 2, 40, 6, 7, 39, 21, 9, 8, 41, 42, 43 };
	private static Integer[] itemids_1 = { 38, 4, 36, 23, 37, 24, 5, 25, 26, 39 };

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
		radAutoFalse = (RadioButton) findViewById(R.id.radAutoFalse);
		radAutoTrue = (RadioButton) findViewById(R.id.radAutoTrue);
		radStopTrue = (RadioButton) findViewById(R.id.radStopTrue);
		Bundle bundle = this.getIntent().getExtras();
		routeid = bundle.getInt("routeid");

		if (routeid > -1) {
			Cursor cursor = md.rawQuery("select * from " + DBer.T_ROUTE + " where routeid=" + routeid);
			if (cursor.moveToFirst()) {
				txtName.setText(cursor.getString(cursor.getColumnIndex("name")));
				txtTts.setText(cursor.getString(cursor.getColumnIndex("tts")));
				radAutoFalse.setChecked(cursor.getInt(cursor.getColumnIndex("auto")) == 0);
				radStopTrue.setChecked(cursor.getInt(cursor.getColumnIndex("errstop")) == 1);
			}
			cursor.close();
		} else {
			txtName.setEnabled(true);
			btnName.setText("保存");
		}

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 200, 0, locationListener);

		radAutoTrue.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (routeid > -1) {
					md.execSQL("update " + DBer.T_ROUTE + " set auto=" + (radAutoTrue.isChecked() ? 1 : 0) + " where routeid=" + routeid);
				}
			}
		});
		radStopTrue.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (routeid > -1) {
					md.execSQL("update " + DBer.T_ROUTE + " set errstop=" + (radStopTrue.isChecked() ? 1 : 0) + " where routeid=" + routeid);
				}
			}
		});
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
							if (routeid < 50) {
								routeid = 50;
							}
							routeid++;
							md.execSQL("insert into " + DBer.T_ROUTE + "(routeid,name,tts,auto) values(" + routeid + ",'" + txtName.getText() + "','当前路线为" + txtName.getText() + "',1)");
							for (int i = 0; i < itemids_0.length; i++) {
								md.execSQL("insert into " + DBer.T_ROUTE_ITEM + "(routeid,itemid,lon,lat,angle,xuhao) values(" + routeid + "," + itemids_0[i] + ",0,0,0," + (i + 1) + ")");
							}
							btnName.setText("修改");
							txtName.setEnabled(false);
							txtTts.setText("当前路线为" + txtName.getText());
							load();
						} else {
							Toast.makeText(RouteEditActivity.this, "路线保存失败", Toast.LENGTH_SHORT).show();
						}
					} else {
						md.execSQL("update " + DBer.T_ROUTE + " set name='" + txtName.getText() + "' where routeid=" + routeid);
						btnName.setText("修改");
						txtName.setEnabled(false);
						if (txtTts.getText().toString().trim().equals("")) {
							md.execSQL("update " + DBer.T_ROUTE + " set tts='当前路线为" + txtName.getText() + "' where routeid=" + routeid);
							txtTts.setText("当前路线为" + txtName.getText());
						}
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
		itemids = new ArrayList<Integer>();
		xuhaos = new ArrayList<Integer>();

		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {
				if (arg2 == data.size() - 1) {
					if (routeid == -1) {
						Toast.makeText(RouteEditActivity.this, "请先保存路线名称", Toast.LENGTH_SHORT).show();
					} else {
						if (md.getLatlon()[0] != 0) {
							add();
						} else {
							Toast.makeText(RouteEditActivity.this, "正在等待GPS定位,请稍候...", Toast.LENGTH_SHORT).show();
						}
					}
				} else {
					AlertDialog alertDialog = new AlertDialog.Builder(RouteEditActivity.this).setTitle("请选择操作").setIcon(android.R.drawable.ic_menu_help).setItems(new String[] { "上移", "下移", "修改", "删除" }, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							int itemid = itemids.get(arg2);
							int xuhao = xuhaos.get(arg2);
							if (which < 3) {
								if (which == 0) {
									if (arg2 > 0) {
										md.execSQL("update " + DBer.T_ROUTE_ITEM + " set xuhao=-1 where routeid=" + routeid + " and xuhao=" + xuhao);
										md.execSQL("update " + DBer.T_ROUTE_ITEM + " set xuhao=" + xuhao + " where routeid=" + routeid + " and xuhao=" + (xuhao - 1));
										md.execSQL("update " + DBer.T_ROUTE_ITEM + " set xuhao=" + (xuhao - 1) + " where routeid=" + routeid + " and xuhao=-1");
										load();
									}
								} else if (which == 1) {
									if (arg2 < data.size() - 2) {
										md.execSQL("update " + DBer.T_ROUTE_ITEM + " set xuhao=-1 where routeid=" + routeid + " and xuhao=" + xuhao);
										md.execSQL("update " + DBer.T_ROUTE_ITEM + " set xuhao=" + xuhao + " where routeid=" + routeid + " and xuhao=" + (xuhao + 1));
										md.execSQL("update " + DBer.T_ROUTE_ITEM + " set xuhao=" + (xuhao + 1) + " where routeid=" + routeid + " and xuhao=-1");
										load();
									}
								} else {
									Intent intent = new Intent();
									Bundle bundle = new Bundle();
									bundle.putInt("routeid", routeid);
									bundle.putInt("itemid", itemid);
									bundle.putInt("xuhao", xuhao);
									intent.putExtras(bundle);
									intent.setClass(RouteEditActivity.this, RouteItemEditActivity.class);
									startActivityForResult(intent, 0);
								}
							} else {
								md.execSQL("delete from " + DBer.T_ROUTE_ITEM + " where routeid=" + routeid + " and itemid=" + itemid + " and xuhao=" + xuhao);
								load();
							}
						}
					}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							return;
						}
					}).create();
					alertDialog.show();
				}
			}
		});
		load();
	}

	private void load() {
		data.clear();
		itemids.clear();
		xuhaos.clear();
		Cursor cursor = md.rawQuery("select a.itemid,b.name,a.xuhao from " + DBer.T_ROUTE_ITEM + " a left join " + DBer.T_ITEM + " b on a.itemid=b.itemid where a.routeid=" + routeid + " order by a.xuhao");
		int i = 0;
		if (cursor.moveToFirst()) {
			do {
				i++;
				data.add("(" + i + ")" + cursor.getString(cursor.getColumnIndex("name")));
				itemids.add(cursor.getInt(cursor.getColumnIndex("itemid")));
				xuhaos.add(cursor.getInt(cursor.getColumnIndex("xuhao")));
			} while (cursor.moveToNext());
		}
		cursor.close();
		data.add("添加项目...");
		listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, data));
	}

	private void add() {
		int xuhao = 1;
		Cursor cursor = md.rawQuery("select xuhao from " + DBer.T_ROUTE_ITEM + " where routeid=" + routeid + " order by xuhao desc");
		if (cursor.moveToFirst()) {
			xuhao = cursor.getInt(0) + 1;
		}
		cursor.close();
		float[] latlon = md.getLatlon();
		md.execSQL("insert into " + DBer.T_ROUTE_ITEM + "(routeid,itemid,lon,lat,angle,xuhao) values(" + routeid + ",5," + latlon[1] + "," + latlon[0] + "," + (md.getData(31) + 1) + "," + xuhao + ")");
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putInt("routeid", routeid);
		bundle.putInt("xuhao", xuhao);
		bundle.putInt("itemid", 0);
		intent.putExtras(bundle);
		intent.setClass(RouteEditActivity.this, RouteItemEditActivity.class);
		startActivityForResult(intent, 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			load();
		}
	}

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
	public void onAttachedToWindow() {
		this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
		super.onAttachedToWindow();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			this.setResult(RESULT_OK);
			this.finish();
		}
		return false;
	}

}
