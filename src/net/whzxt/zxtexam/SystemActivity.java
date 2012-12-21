package net.whzxt.zxtexam;

import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.text.InputType;

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
		ListPreference devices = (ListPreference)findPreference("device");
        String[] entries = md.mSerialPortFinder.getAllDevices();
        String[] entryValues = md.mSerialPortFinder.getAllDevicesPath();
		devices.setEntries(entries);
		devices.setEntryValues(entryValues);
		devices.setSummary(devices.getValue());
		devices.setOnPreferenceChangeListener(this);
		// 波特率
		ListPreference baudrates = (ListPreference)findPreference("baudrate");
		baudrates.setSummary(baudrates.getValue());
		baudrates.setOnPreferenceChangeListener(this);
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
			preference.setSummary((String)newValue);
		} else if (preference.getKey().equals("baudrate")) {
			preference.setSummary((String)newValue);
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
		}
		return false;
	}

}
