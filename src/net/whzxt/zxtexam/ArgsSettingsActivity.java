package net.whzxt.zxtexam;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.text.InputType;

public class ArgsSettingsActivity extends PreferenceActivity implements OnPreferenceChangeListener {
	private Metadata md;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.argssettings);		
		md = (Metadata) getApplicationContext();
		Preference preference = null;
		// 密码设置
		preference = findPreference("password");
		((EditTextPreference) preference).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		// 脉冲系数
		for (int i = 1; i < 5; i++) {
			preference = findPreference("maichongxs" + i);
			if (preference != null) {
				preference.setTitle(md.getName(19 + i) + " = 脉冲" + i + " * " + md.getMaichongXS(i));
				((EditTextPreference) preference).getEditText().setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
				preference.setOnPreferenceChangeListener(this);
			}
		}
		// GPS速度修正系数
		preference = findPreference("gpsspeedxs");
		preference.setTitle("GPS速度修正系数：" + md.getGpsSpeedXS());
		((EditTextPreference) preference).getEditText().setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
		preference.setOnPreferenceChangeListener(this);
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
		preference.setSummary(md.getDataResourceType() == 0 ? "串口" : "无线");
		preference.setOnPreferenceChangeListener(this);
	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference.getKey().startsWith("maichongxs")) {
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
		} else if (preference.getKey().equals("gpsspeedxs")) {
			if (newValue.toString().trim().equals("")) {
				return false;
			}
			try {
				Float.parseFloat(newValue.toString());
			} catch (NumberFormatException e) {
				return false;
			}
			preference.setTitle("GPS速度修正系数：" + newValue);
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
			preference.setSummary(newValue.toString().equals("0") ? "串口" : "无线");
		}
		return true;
	}
}
