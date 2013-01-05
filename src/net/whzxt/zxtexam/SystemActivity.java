package net.whzxt.zxtexam;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.text.InputType;
import android.widget.Toast;

public class SystemActivity extends PreferenceActivity implements OnPreferenceClickListener, OnPreferenceChangeListener {
	private Metadata md;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.systemsettings);
		md = (Metadata) getApplicationContext();
		Preference preference = null;
		// 信号和脉冲名称
		for (int i = 0; i < 30; i++) {
			preference = findPreference("name" + i);
			if (preference != null) {
				preference.setTitle(md.getName(i));
				preference.setOnPreferenceChangeListener(this);
			}
		}
		// 密码设置
		preference = findPreference("password");
		((EditTextPreference) preference).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		// 路线设置
		preference = findPreference("RouteSetting");
		preference.setOnPreferenceClickListener(this);
		// 项目设置
		preference = findPreference("ItemSetting");
		preference.setOnPreferenceClickListener(this);
		// 扣分设置
		preference = findPreference("ErrSetting");
		preference.setOnPreferenceClickListener(this);
		// 导出数据
		preference = findPreference("exportdata");
		preference.setOnPreferenceClickListener(this);
		// 脉冲系数
		for (int i = 1; i < 10; i++) {
			preference = findPreference("maichongxs" + i);
			if (preference != null) {
				preference.setTitle(md.getName(19 + i) + " = 脉冲" + i + " * " + md.getMaichongXS(i));
				((EditTextPreference) preference).getEditText().setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
				preference.setOnPreferenceChangeListener(this);
			}
		}
		// GPS阈值
		preference = findPreference("range");
		preference.setTitle("阈值：" + md.getRange());
		((EditTextPreference) preference).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		preference.setOnPreferenceChangeListener(this);
		// 串口
		ListPreference devices = (ListPreference) findPreference("device");
		String[] entries = md.mSerialPortFinder.getAllDevices();
		String[] entryValues = md.mSerialPortFinder.getAllDevicesPath();
		devices.setEntries(entries);
		devices.setEntryValues(entryValues);
		devices.setSummary(devices.getValue());
		devices.setOnPreferenceChangeListener(this);
		// 波特率
		ListPreference baudrates = (ListPreference) findPreference("baudrate");
		baudrates.setSummary(baudrates.getValue());
		baudrates.setOnPreferenceChangeListener(this);
		// 数据来源
		preference = findPreference("dataresourcetype");
		preference.setSummary(md.getDataResourceType() == 0 ? "串口" : "蓝牙");
		preference.setOnPreferenceChangeListener(this);
	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference.getKey().startsWith("name")) {
			if (newValue.toString().trim().equals("")) {
				return false;
			}
			preference.setTitle(newValue.toString());
			int i = Integer.parseInt(preference.getKey().substring(4));
			if (i >= 20) {
				i -= 19;
				findPreference("maichongxs" + i).setTitle(newValue.toString() + " = 脉冲" + i + " * " + md.getMaichongXS(i));
			}
		} else if (preference.getKey().startsWith("maichongxs")) {
			if (newValue.toString().trim().equals("")) {
				return false;
			}
			try {
				Float.parseFloat(newValue.toString());
			} catch (NumberFormatException e) {
				return false;
			}
			int i = Integer.parseInt(preference.getKey().substring(10));
			preference.setTitle(md.getName(19 + i) + " = 脉冲" + i + " * " + newValue);
		} else if (preference.getKey().equals("range")) {
			if (newValue.toString().trim().equals("")) {
				return false;
			}
			preference.setTitle("阈值：" + newValue);
		} else if (preference.getKey().equals("device")) {
			preference.setSummary((String) newValue);
		} else if (preference.getKey().equals("baudrate")) {
			preference.setSummary((String) newValue);
		} else if (preference.getKey().equals("dataresourcetype")) {
			preference.setSummary(newValue.toString().equals("0") ? "串口" : "蓝牙");
		}
		return true;
	}

	public boolean onPreferenceClick(Preference preference) {
		if (preference.getKey().equals("ItemSetting")) {
			startActivity(new Intent().setClass(SystemActivity.this, ItemsActivity.class));
		} else if (preference.getKey().equals("ErrSetting")) {
			startActivity(new Intent().setClass(SystemActivity.this, ErrSettingsActivity.class));
		} else if (preference.getKey().equals("RouteSetting")) {
			startActivity(new Intent().setClass(SystemActivity.this, RouteActivity.class));
		} else if (preference.getKey().equals("exportdata")) {
			Toast.makeText(SystemActivity.this, "正在导出,请稍候...", Toast.LENGTH_LONG).show();
			StringBuffer sBuffer = new StringBuffer();
			Cursor cursor = md.rawQuery("select * from " + DBer.T_ITEM + " order by type,xuhao");
			if (cursor.moveToFirst()) {
				do {
					sBuffer.append("db.execSQL(\"INSERT INTO ");
					sBuffer.append(DBer.T_ITEM);
					sBuffer.append("(itemid, name, tts, timeout, type, xuhao) VALUES (");
					sBuffer.append(cursor.getInt(cursor.getColumnIndex("itemid")));
					sBuffer.append(",");
					sBuffer.append("'" + cursor.getString(cursor.getColumnIndex("name")) + "'");
					sBuffer.append(",");
					sBuffer.append("'" + cursor.getString(cursor.getColumnIndex("tts")) + "'");
					sBuffer.append(",");
					sBuffer.append(cursor.getInt(cursor.getColumnIndex("timeout")));
					sBuffer.append(",");
					sBuffer.append(cursor.getInt(cursor.getColumnIndex("type")));
					sBuffer.append(",");
					sBuffer.append(cursor.getInt(cursor.getColumnIndex("xuhao")));
					sBuffer.append(")\");\n");
				} while (cursor.moveToNext());
			}
			cursor.close();
			cursor = md.rawQuery("select * from " + DBer.T_ITEM_ERR);
			if (cursor.moveToFirst()) {
				do {
					sBuffer.append("db.execSQL(\"INSERT INTO ");
					sBuffer.append(DBer.T_ITEM_ERR);
					sBuffer.append("(errid, itemid, name, fenshu) VALUES (");
					sBuffer.append(cursor.getInt(cursor.getColumnIndex("errid")));
					sBuffer.append(",");
					sBuffer.append(cursor.getInt(cursor.getColumnIndex("itemid")));
					sBuffer.append(",");
					sBuffer.append("'" + cursor.getString(cursor.getColumnIndex("name")) + "'");
					sBuffer.append(",");
					sBuffer.append(cursor.getInt(cursor.getColumnIndex("fenshu")));
					sBuffer.append(")\");\n");
				} while (cursor.moveToNext());
			}
			cursor.close();
			cursor = md.rawQuery("select * from " + DBer.T_ROUTE);
			if (cursor.moveToFirst()) {
				do {
					sBuffer.append("db.execSQL(\"INSERT INTO ");
					sBuffer.append(DBer.T_ROUTE);
					sBuffer.append("(routeid, name, tts, auto) VALUES (");
					sBuffer.append(cursor.getInt(cursor.getColumnIndex("routeid")));
					sBuffer.append(",");
					sBuffer.append("'" + cursor.getString(cursor.getColumnIndex("name")) + "'");
					sBuffer.append(",");
					sBuffer.append("'" + cursor.getString(cursor.getColumnIndex("tts")) + "'");
					sBuffer.append(",");
					sBuffer.append(cursor.getInt(cursor.getColumnIndex("auto")));
					sBuffer.append(")\");\n");
				} while (cursor.moveToNext());
			}
			cursor.close();
			cursor = md.rawQuery("select * from " + DBer.T_ROUTE_ITEM);
			if (cursor.moveToFirst()) {
				do {
					sBuffer.append("db.execSQL(\"INSERT INTO ");
					sBuffer.append(DBer.T_ROUTE_ITEM);
					sBuffer.append("(routeid, itemid, lon, lat, xuhao) VALUES (");
					sBuffer.append(cursor.getInt(cursor.getColumnIndex("routeid")));
					sBuffer.append(",");
					sBuffer.append(cursor.getInt(cursor.getColumnIndex("itemid")));
					sBuffer.append(",");
					sBuffer.append(cursor.getInt(cursor.getColumnIndex("lon")));
					sBuffer.append(",");
					sBuffer.append(cursor.getInt(cursor.getColumnIndex("lat")));
					sBuffer.append(",");
					sBuffer.append(cursor.getInt(cursor.getColumnIndex("xuhao")));
					sBuffer.append(")\");\n");
				} while (cursor.moveToNext());
			}
			cursor.close();
			cursor = md.rawQuery("select * from " + DBer.T_ITEM_ACTION);
			if (cursor.moveToFirst()) {
				do {
					sBuffer.append("db.execSQL(\"INSERT INTO ");
					sBuffer.append(DBer.T_ITEM_ACTION);
					sBuffer.append("(itemid,dataid,times,min,max,errid,step) VALUES (");
					sBuffer.append(cursor.getInt(cursor.getColumnIndex("itemid")));
					sBuffer.append(",");
					sBuffer.append(cursor.getInt(cursor.getColumnIndex("dataid")));
					sBuffer.append(",");
					sBuffer.append(cursor.getInt(cursor.getColumnIndex("times")));
					sBuffer.append(",");
					sBuffer.append(cursor.getInt(cursor.getColumnIndex("min")));
					sBuffer.append(",");
					sBuffer.append(cursor.getInt(cursor.getColumnIndex("max")));
					sBuffer.append(",");
					sBuffer.append(cursor.getInt(cursor.getColumnIndex("errid")));
					sBuffer.append(",");
					sBuffer.append(cursor.getInt(cursor.getColumnIndex("step")));
					sBuffer.append(")\");\n");
				} while (cursor.moveToNext());
			}
			cursor.close();
			try {
				OutputStream outstream = new FileOutputStream("/sdcard/zxtexam.data", false);
				OutputStreamWriter out = new OutputStreamWriter(outstream);
				out.write(sBuffer.toString());
				out.close();
				Toast.makeText(SystemActivity.this, "数据已导出到/sdcard/zxtexam.data", Toast.LENGTH_SHORT).show();
			} catch (IOException e) {
				Toast.makeText(SystemActivity.this, "导出数据时出现异常", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		}
		return false;
	}

}
