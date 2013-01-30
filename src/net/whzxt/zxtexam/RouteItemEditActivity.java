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
		preference.setSummary("使用默认值");
		((EditTextPreference) preference).getEditText().setText(String.valueOf(md.getRange()));
		((EditTextPreference) preference).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		preference.setOnPreferenceChangeListener(this);
		preference = findPreference("ri_timeout");
		preference.setSummary("使用默认值");
		((EditTextPreference) preference).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		preference.setOnPreferenceChangeListener(this);
		preference = findPreference("ri_delay");
		preference.setSummary("使用默认值");
		((EditTextPreference) preference).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		preference.setOnPreferenceChangeListener(this);
		preference = findPreference("ri_delaymeter");
		preference.setSummary("使用默认值");
		((EditTextPreference) preference).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		preference.setOnPreferenceChangeListener(this);
		preference = findPreference("ri_range");
		preference.setSummary("使用默认值");
		((EditTextPreference) preference).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		preference.setOnPreferenceChangeListener(this);
		preference = findPreference("ri_ok");
		preference.setOnPreferenceClickListener(this);
		cursor = md.rawQuery("select * from " + DBer.T_ROUTE_ITEM + " where routeid=" + routeid + " and itemid=" + itemid + " and xuhao=" + xuhao);
		if (cursor.moveToFirst()) {
			if(cursor.getInt(cursor.getColumnIndex("gpsrange"))>0){
				findPreference("ri_gpsrange").setSummary(String.valueOf(cursor.getInt(cursor.getColumnIndex("gpsrange"))));
			}
			if(cursor.getInt(cursor.getColumnIndex("timeout"))>0){
				findPreference("ri_timeout").setSummary(String.valueOf(cursor.getInt(cursor.getColumnIndex("timeout"))));
			}
			if(cursor.getInt(cursor.getColumnIndex("delay"))>0){
				findPreference("ri_delay").setSummary(String.valueOf(cursor.getInt(cursor.getColumnIndex("delay"))));
			}
			if(cursor.getInt(cursor.getColumnIndex("delaymeter"))>0){
				findPreference("ri_delaymeter").setSummary(String.valueOf(cursor.getInt(cursor.getColumnIndex("delaymeter"))));
			}
			if(cursor.getInt(cursor.getColumnIndex("range"))>0){
				findPreference("ri_range").setSummary(String.valueOf(cursor.getInt(cursor.getColumnIndex("range"))));
			}
		}
		cursor.close();
	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference.getKey().equals("ri_item")) {
			itemid = Integer.parseInt(newValue.toString());
			md.execSQL("update " + DBer.T_ROUTE_ITEM + " set itemid=" + itemid + ",delay=0,delaymeter=0,timeout=0,range=0 where routeid=" + routeid + " and xuhao=" + xuhao);
			preference.setSummary(itemmap.get(newValue.toString()));
			findPreference("ri_delay").setSummary("使用默认值");
			findPreference("ri_delaymeter").setSummary("使用默认值");
			findPreference("ri_timeout").setSummary("使用默认值");
			findPreference("ri_range").setSummary("使用默认值");
			return true;
		}
		if (preference.getKey().equals("ri_gpsrange")) {
			if(newValue.toString().equals("")){
				return false;
			}
			md.execSQL("update " + DBer.T_ROUTE_ITEM + " set gpsrange=" + newValue + " where routeid=" + routeid + " and xuhao=" + xuhao);
			if(newValue.toString().equals("0")){
				preference.setSummary("使用默认值");
			}else{
				preference.setSummary(newValue.toString());
			}
			return true;
		}
		if (preference.getKey().equals("ri_timeout")) {
			if(newValue.toString().equals("")){
				return false;
			}
			md.execSQL("update " + DBer.T_ROUTE_ITEM + " set timeout=" + newValue + " where routeid=" + routeid + " and xuhao=" + xuhao);
			if(newValue.toString().equals("0")){
				preference.setSummary("使用默认值");
			}else{
				preference.setSummary(newValue.toString());
			}
			return true;
		}
		if (preference.getKey().equals("ri_delay")) {
			if(newValue.toString().equals("")){
				return false;
			}
			md.execSQL("update " + DBer.T_ROUTE_ITEM + " set delay=" + newValue + " where routeid=" + routeid + " and xuhao=" + xuhao);
			if(newValue.toString().equals("0")){
				preference.setSummary("使用默认值");
			}else{
				preference.setSummary(newValue.toString());
			}
			return true;
		}
		if (preference.getKey().equals("ri_delaymeter")) {
			if(newValue.toString().equals("")){
				return false;
			}
			md.execSQL("update " + DBer.T_ROUTE_ITEM + " set delaymeter=" + newValue + " where routeid=" + routeid + " and xuhao=" + xuhao);
			if(newValue.toString().equals("0")){
				preference.setSummary("使用默认值");
			}else{
				preference.setSummary(newValue.toString());
			}
			return true;
		}
		if (preference.getKey().equals("ri_range")) {
			if(newValue.toString().equals("")){
				return false;
			}
			md.execSQL("update " + DBer.T_ROUTE_ITEM + " set range=" + newValue + " where routeid=" + routeid + " and xuhao=" + xuhao);
			if(newValue.toString().equals("0")){
				preference.setSummary("使用默认值");
			}else{
				preference.setSummary(newValue.toString());
			}
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
