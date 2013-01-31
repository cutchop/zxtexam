package net.whzxt.zxtexam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter;

public class ErrSettingsActivity extends Activity {

	private ListView listView;
	private Metadata md;
	private List<Map<String, String>> data;
	private List<Map<String, Object>> itemList;
	private String[] itemnames;
	private int tempitemid;
	private String[] fenshus = { "5", "10", "100" };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_errsettings);
		listView = (ListView) findViewById(R.id.listView1);
		md = (Metadata) getApplication();

		itemList = new ArrayList<Map<String, Object>>();
		Cursor cursor = md.rawQuery("select * from " + DBer.T_ITEM + " order by type,xuhao");
		if (cursor.moveToFirst()) {
			do {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("itemname", cursor.getString(cursor.getColumnIndex("name")));
				map.put("itemid", cursor.getInt(cursor.getColumnIndex("itemid")));
				itemList.add(map);
			} while (cursor.moveToNext());
		}
		cursor.close();
		itemnames = new String[itemList.size()];
		for (int i = 0; i < itemList.size(); i++) {
			itemnames[i] = itemList.get(i).get("itemname").toString();
		}
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {
				if (arg2 == 0) {
					// 添加
					AlertDialog alertDialog = new AlertDialog.Builder(ErrSettingsActivity.this).setTitle("请选择所属项目").setIcon(android.R.drawable.ic_menu_add)
							.setItems(itemnames, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									tempitemid = Integer.parseInt(itemList.get(which).get("itemid").toString());
									final EditText txtName = new EditText(ErrSettingsActivity.this);
									AlertDialog alertDialog2 = new AlertDialog.Builder(ErrSettingsActivity.this).setTitle("请输入扣分说明").setIcon(android.R.drawable.ic_menu_add).setView(txtName)
											.setPositiveButton("确定", new DialogInterface.OnClickListener() {
												public void onClick(DialogInterface dialog, int which) {
													if (txtName.getText().toString().trim().equals("")) {
														return;
													}
													AlertDialog alertDialog3 = new AlertDialog.Builder(ErrSettingsActivity.this).setTitle("请设定分数").setIcon(android.R.drawable.ic_menu_add)
															.setItems(fenshus, new DialogInterface.OnClickListener() {
																public void onClick(DialogInterface dialog, int which) {
																	Cursor cur = md.rawQuery("select max(errid)+1 as id from " + DBer.T_ITEM_ERR);
																	if (cur.moveToFirst()) {
																		md.execSQL("insert into " + DBer.T_ITEM_ERR + "(errid, itemid, name, fenshu) values(" + cur.getInt(0) + "," + tempitemid + ",'"
																				+ txtName.getText() + "'," + fenshus[which] + ")");
																		Toast.makeText(ErrSettingsActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
																		reload();
																	} else {
																		Toast.makeText(ErrSettingsActivity.this, "出现错误,请重试", Toast.LENGTH_SHORT).show();
																	}
																	cur.close();
																}
															}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
																public void onClick(DialogInterface dialog, int which) {
																	return;
																}
															}).create();
													alertDialog3.show();
												}
											}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
												public void onClick(DialogInterface dialog, int which) {
													return;
												}
											}).create();
									alertDialog2.show();
								}
							}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									return;
								}
							}).create();
					alertDialog.show();
				} else {
					AlertDialog alertDialog = new AlertDialog.Builder(ErrSettingsActivity.this).setTitle("请选择操作").setIcon(android.R.drawable.ic_menu_help)
							.setPositiveButton("修改", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									final EditText txtName = new EditText(ErrSettingsActivity.this);
									txtName.setText(data.get(arg2 - 1).get("name").toString());
									AlertDialog alertDialog2 = new AlertDialog.Builder(ErrSettingsActivity.this).setTitle("请输入扣分说明").setIcon(android.R.drawable.ic_menu_add).setView(txtName)
											.setPositiveButton("确定", new DialogInterface.OnClickListener() {
												public void onClick(DialogInterface dialog, int which) {
													if (txtName.getText().toString().trim().equals("")) {
														return;
													}
													md.execSQL("update " + DBer.T_ITEM_ERR + " set name='" + txtName.getText().toString() + "' where errid=" + data.get(arg2 - 1).get("errid"));
													Toast.makeText(ErrSettingsActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
												}
											}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
												public void onClick(DialogInterface dialog, int which) {
													return;
												}
											}).create();
									alertDialog2.show();
								}
							}).setNegativeButton("删除", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									md.execSQL("delete from " + DBer.T_ITEM_ERR + " where errid=" + data.get(arg2 - 1).get("errid"));
									Toast.makeText(ErrSettingsActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
								}
							}).create();
					alertDialog.show();
				}
			}
		});
		reload();// 加载数据
	}

	private void reload() {
		if (data == null) {
			data = new ArrayList<Map<String, String>>();
		} else {
			data.clear();
		}
		Map<String, String> map = new HashMap<String, String>();
		map.put("errid", "999");
		map.put("name", "添加新扣分项...");
		map.put("fenshu", "");
		data.add(map);
		Cursor cursor = md.rawQuery("select a.errid,a.name,a.fenshu,b.name as itemname,b.type from " + DBer.T_ITEM_ERR + " a left join " + DBer.T_ITEM
				+ " b on a.itemid=b.itemid order by b.type,b.xuhao,a.errid");
		if (cursor.moveToFirst()) {
			do {
				Map<String, String> datum = new HashMap<String, String>();
				datum.put("errid", cursor.getString(cursor.getColumnIndex("errid")));
				datum.put("name", cursor.getString(cursor.getColumnIndex("name")));
				datum.put(
						"fenshu",
						"[" + (cursor.getInt(cursor.getColumnIndex("type")) == 0 ? "路考" : "灯光") + "]" + cursor.getString(cursor.getColumnIndex("itemname")) + "  "
								+ cursor.getString(cursor.getColumnIndex("fenshu")) + "分");
				data.add(datum);
			} while (cursor.moveToNext());
		}
		cursor.close();
		listView.setAdapter(new SimpleAdapter(this, data, android.R.layout.simple_expandable_list_item_2, new String[] { "name", "fenshu" }, new int[] { android.R.id.text1, android.R.id.text2 }));
	}

	@Override
	public void onAttachedToWindow() {
		this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
		super.onAttachedToWindow();
	}
}
