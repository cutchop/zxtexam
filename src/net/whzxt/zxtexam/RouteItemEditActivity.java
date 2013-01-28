package net.whzxt.zxtexam;

import java.util.HashMap;
import java.util.Map;

import android.database.Cursor;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.text.InputType;
import android.view.KeyEvent;

public class RouteItemEditActivity extends PreferenceActivity implements OnPreferenceClickListener, OnPreferenceChangeListener {

	private Metadata md;
	private int routeid;
	private int itemid = 0;
	private int xuhao;
	private String[] itemnames;
	private String[] itemvalues;
	private Map<String, String> itemmap;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.routeitemedit);
		md = (Metadata) getApplication();
		itemmap = new HashMap<String, String>();

		Bundle bundle = this.getIntent().getExtras();
		routeid = bundle.getInt("routeid");
		xuhao = bundle.getInt("xuhao");
		itemid = bundle.getInt("itemid");

		Cursor cursor = md.rawQuery("select * from " + DBer.T_ITEM + " order by type,xuhao");
		if (cursor.moveToFirst()) {
			itemnames = new String[cursor.getCount()];
			itemvalues = new String[cursor.getCount()];
			int i = 0;
			do {
				itemnames[i] = cursor.getString(cursor.getColumnIndex("name"));
				itemvalues[i] = cursor.getString(cursor.getColumnIndex("itemid"));
				itemmap.put(itemvalues[i], itemnames[i]);
				i++;
			} while (cursor.moveToNext());
		}
		cursor.close();
		Preference preference = findPreference("ri_item");
		((ListPreference) preference).setEntries(itemnames);
		((ListPreference) preference).setEntryValues(itemvalues);
		preference.setSummary(itemmap.get(String.valueOf(itemid)));
		preference.setOnPreferenceChangeListener(this);
		preference = findPreference("ri_gpsrange");
		preference.setSummary(String.valueOf(md.getRange()));
		((EditTextPreference) preference).getEditText().setText(String.valueOf(md.getRange()));
		((EditTextPreference) preference).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		preference.setOnPreferenceChangeListener(this);
		preference = findPreference("ri_timeout");
		((EditTextPreference) preference).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		preference.setOnPreferenceChangeListener(this);
		preference = findPreference("ri_delay");
		((EditTextPreference) preference).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		preference.setOnPreferenceChangeListener(this);
		preference = findPreference("ri_range");
		((EditTextPreference) preference).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		preference.setOnPreferenceChangeListener(this);
		preference = findPreference("ri_ok");
		preference.setOnPreferenceClickListener(this);
	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference.getKey().equals("ri_item")) {
			itemid = Integer.parseInt(newValue.toString());
			md.execSQL("update " + DBer.T_ROUTE_ITEM + " set itemid=" + itemid + " where routeid=" + routeid + " and xuhao=" + xuhao);
			preference.setSummary(itemmap.get(newValue.toString()));
			Cursor cursor = md.rawQuery("select * from " + DBer.T_ITEM + " where itemid=" + itemid);
			if (cursor.moveToFirst()) {
				if (cursor.getInt(cursor.getColumnIndex("timeout")) == 0) {
					findPreference("ri_timeout").setSummary("");
				} else {
					findPreference("ri_timeout").setSummary(String.valueOf(cursor.getInt(cursor.getColumnIndex("timeout"))));
				}
				if (cursor.getInt(cursor.getColumnIndex("delay")) == 0) {
					findPreference("ri_delay").setSummary("");
				} else {
					findPreference("ri_delay").setSummary(String.valueOf(cursor.getInt(cursor.getColumnIndex("delay"))));
				}
				if (cursor.getInt(cursor.getColumnIndex("range")) == 0) {
					findPreference("ri_range").setSummary("");
				} else {
					findPreference("ri_range").setSummary(String.valueOf(cursor.getInt(cursor.getColumnIndex("range"))));
				}
				md.execSQL("update " + DBer.T_ROUTE_ITEM + " set timeout=0,range=0 where routeid=" + routeid + " and xuhao=" + xuhao);
			}
			cursor.close();
			return true;
		}
		if (preference.getKey().equals("ri_gpsrange")) {
			md.execSQL("update " + DBer.T_ROUTE_ITEM + " set gpsrange=" + newValue + " where routeid=" + routeid + " and xuhao=" + xuhao);
			preference.setSummary(newValue.toString());
			return true;
		}
		if (preference.getKey().equals("ri_timeout")) {
			md.execSQL("update " + DBer.T_ROUTE_ITEM + " set timeout=" + newValue + " where routeid=" + routeid + " and xuhao=" + xuhao);
			preference.setSummary(newValue.toString());
			return true;
		}
		if (preference.getKey().equals("ri_delay")) {
			md.execSQL("update " + DBer.T_ROUTE_ITEM + " set delay=" + newValue + " where routeid=" + routeid + " and xuhao=" + xuhao);
			preference.setSummary(newValue.toString());
			return true;
		}
		if (preference.getKey().equals("ri_range")) {
			md.execSQL("update " + DBer.T_ROUTE_ITEM + " set range=" + newValue + " where routeid=" + routeid + " and xuhao=" + xuhao);
			preference.setSummary(newValue.toString());
			return true;
		}
		return true;
	}

	public boolean onPreferenceClick(Preference preference) {
		if (preference.getKey().equals("ri_ok")) {
			RouteItemEditActivity.this.setResult(RESULT_OK);
			RouteItemEditActivity.this.finish();
		}
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			RouteItemEditActivity.this.setResult(RESULT_OK);
			RouteItemEditActivity.this.finish();
		}
		return false;
	}

}
