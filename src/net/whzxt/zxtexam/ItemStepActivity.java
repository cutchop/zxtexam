package net.whzxt.zxtexam;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.text.InputType;
import android.view.KeyEvent;

public class ItemStepActivity extends PreferenceActivity implements OnPreferenceClickListener, OnPreferenceChangeListener {

	private Metadata md;
	private int itemid;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.itemstep);
		md = (Metadata) getApplication();

		Bundle bundle = this.getIntent().getExtras();
		int id = bundle.getInt("itemid");
		itemid = id;

		Preference preference = null;
		Cursor cursor = md.rawQuery("select * from " + DBer.T_ITEM + " where itemid=" + id);
		if (cursor.moveToFirst()) {
			preference = findPreference("ie_name");
			preference.setSummary(cursor.getString(cursor.getColumnIndex("name")));
			preference.setOnPreferenceChangeListener(this);
			preference = findPreference("ie_tts");
			preference.setSummary(cursor.getString(cursor.getColumnIndex("tts")));
			preference.setOnPreferenceChangeListener(this);
			preference = findPreference("ie_timeout");
			((EditTextPreference) preference).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
			preference.setSummary(String.valueOf(cursor.getInt(cursor.getColumnIndex("timeout")) + "秒"));
			preference.setOnPreferenceChangeListener(this);
		}
		cursor.close();

		for (int i = 1; i < 8; i++) {
			findPreference("step" + i).setOnPreferenceClickListener(this);
		}
		cursor = md.rawQuery("select * from " + DBer.T_ITEM_ACTION + " where itemid=" + id + " order by dataid");
		if (cursor.moveToFirst()) {
			do {
				findPreference("step" + cursor.getInt(cursor.getColumnIndex("step"))).setSummary("已设置评判条件");
			} while (cursor.moveToNext());
		}
		cursor.close();
	}

	public boolean onPreferenceChange(Preference arg0, Object arg1) {
		String key = arg0.getKey();
		if (key.equals("ie_name")) {
			if (arg1.toString().trim().equals("")) {
				return false;
			}
			md.execSQL("update " + DBer.T_ITEM + " set name='" + arg1 + "' where itemid=" + itemid);
			arg0.setSummary(arg1.toString());
			return true;
		}
		if (key.equals("ie_tts")) {
			if (arg1.toString().trim().equals("")) {
				return false;
			}
			md.execSQL("update " + DBer.T_ITEM + " set tts='" + arg1 + "' where itemid=" + itemid);
			arg0.setSummary(arg1.toString());
			return true;
		}
		if (key.equals("ie_timeout")) {
			if (arg1.toString().trim().equals("")) {
				return false;
			}
			md.execSQL("update " + DBer.T_ITEM + " set timeout=" + arg1 + " where itemid=" + itemid);
			arg0.setSummary(arg1.toString() + "秒");
			return true;
		}
		return true;
	}

	public boolean onPreferenceClick(Preference preference) {
		if (preference.getKey().startsWith("step")) {
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putInt("itemid", itemid);
			bundle.putInt("step",Integer.parseInt(preference.getKey().replace("step", "")));			
			intent.putExtras(bundle);
			intent.setClass(ItemStepActivity.this, ItemEditActivity.class);
			startActivityForResult(intent, 0);
		}
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case RESULT_OK:
			Cursor cursor = md.rawQuery("select * from " + DBer.T_ITEM_ACTION + " where itemid=" + itemid + " order by dataid");
			if (cursor.moveToFirst()) {
				do {
					findPreference("step" + cursor.getInt(cursor.getColumnIndex("step"))).setSummary("已设置评判条件");
				} while (cursor.moveToNext());
			}
			cursor.close();
			break;
		default:
			break;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = settings.edit();
			editor.remove("ie_name");
			editor.remove("ie_tts");
			editor.remove("ie_timeout");
			for (int i = 1; i < 8; i++) {
				editor.remove("step" + i);
			}
			editor.commit();
			this.finish();
		}
		return false;
	}

}
