package net.whzxt.zxtexam;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
	private String[] typeStrings = { "路考", "灯光" };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.itemstep);
		md = (Metadata) getApplication();

		Bundle bundle = this.getIntent().getExtras();
		int id = bundle.getInt("itemid");
		if (id == -1) {
			AlertDialog alertDialog = new AlertDialog.Builder(ItemStepActivity.this).setTitle("请选择项目类型").setIcon(android.R.drawable.ic_menu_add).setItems(typeStrings, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					int newid = -1;
					int xuhao = 0;
					Cursor cursor = md.rawQuery("select max(itemid)+1 as id from " + DBer.T_ITEM);
					if (cursor.moveToFirst()) {
						newid = cursor.getInt(0);
					}
					cursor.close();
					cursor = md.rawQuery("select max(xuhao)+1 as id from " + DBer.T_ITEM + " where type=" + which);
					if (cursor.moveToFirst()) {
						xuhao = cursor.getInt(0);
					}
					cursor.close();
					if (newid > -1) {
						md.execSQL("insert into " + DBer.T_ITEM + "(itemid, name, tts, timeout, type, xuhao, endtts, range, delay) VALUES (" + newid + ",'新项目','新项目语音提示',20," + which + "," + xuhao + ",'',0,0)");
						itemid = newid;
						Preference preference = findPreference("ie_name");
						preference.setSummary("新项目");
						preference.setOnPreferenceChangeListener(ItemStepActivity.this);
						preference = findPreference("ie_tts");
						preference.setSummary("新项目语音提示");
						preference.setOnPreferenceChangeListener(ItemStepActivity.this);
						preference = findPreference("ie_endtts");
						preference.setOnPreferenceChangeListener(ItemStepActivity.this);
						preference = findPreference("ie_timeout");
						((EditTextPreference) preference).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
						preference.setOnPreferenceChangeListener(ItemStepActivity.this);
						preference = findPreference("ie_delay");
						((EditTextPreference) preference).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
						preference.setOnPreferenceChangeListener(ItemStepActivity.this);
						preference = findPreference("ie_range");
						((EditTextPreference) preference).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
						preference.setOnPreferenceChangeListener(ItemStepActivity.this);

						for (int i = 1; i < 8; i++) {
							findPreference("step" + i).setOnPreferenceClickListener(ItemStepActivity.this);
						}
					}
				}
			}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					ItemStepActivity.this.finish();
				}
			}).create();
			alertDialog.show();
		} else {
			itemid = id;
			Preference preference = null;
			Cursor cursor = md.rawQuery("select * from " + DBer.T_ITEM + " where itemid=" + itemid);
			if (cursor.moveToFirst()) {
				preference = findPreference("ie_name");
				preference.setSummary(cursor.getString(cursor.getColumnIndex("name")));
				preference.setOnPreferenceChangeListener(this);
				preference = findPreference("ie_tts");
				preference.setSummary(cursor.getString(cursor.getColumnIndex("tts")));
				preference.setOnPreferenceChangeListener(this);
				preference = findPreference("ie_endtts");
				preference.setSummary(cursor.getString(cursor.getColumnIndex("endtts")));
				preference.setOnPreferenceChangeListener(this);
				preference = findPreference("ie_timeout");
				((EditTextPreference) preference).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
				if (cursor.getInt(cursor.getColumnIndex("timeout")) > 0) {
					preference.setSummary(String.valueOf(cursor.getInt(cursor.getColumnIndex("timeout")) + "秒"));
				}
				preference.setOnPreferenceChangeListener(this);
				preference = findPreference("ie_delay");
				((EditTextPreference) preference).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
				if (cursor.getInt(cursor.getColumnIndex("delay")) > 0) {
					preference.setSummary(String.valueOf(cursor.getInt(cursor.getColumnIndex("delay")) + "秒"));
				}
				preference.setOnPreferenceChangeListener(this);
				preference = findPreference("ie_range");
				((EditTextPreference) preference).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
				if (cursor.getInt(cursor.getColumnIndex("range")) > 0) {
					preference.setSummary(String.valueOf(cursor.getInt(cursor.getColumnIndex("range")) + "米"));
				}
				preference.setOnPreferenceChangeListener(this);
			}
			cursor.close();

			for (int i = 1; i < 8; i++) {
				findPreference("step" + i).setOnPreferenceClickListener(this);
			}
			cursor = md.rawQuery("select * from " + DBer.T_ITEM_ACTION + " where itemid=" + itemid + " order by dataid");
			if (cursor.moveToFirst()) {
				do {
					findPreference("step" + cursor.getInt(cursor.getColumnIndex("step"))).setSummary("已设置评判条件");
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
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
		if (key.equals("ie_endtts")) {
			if (arg1.toString().trim().equals("")) {
				return false;
			}
			md.execSQL("update " + DBer.T_ITEM + " set endtts='" + arg1 + "' where itemid=" + itemid);
			arg0.setSummary(arg1.toString());
			return true;
		}
		if (key.equals("ie_timeout")) {
			if (arg1.toString().trim().equals("")) {
				return false;
			}
			md.execSQL("update " + DBer.T_ITEM + " set timeout=" + arg1 + " where itemid=" + itemid);
			if (arg1.toString().equals("0")) {
				arg0.setSummary("");
			} else {
				arg0.setSummary(arg1.toString() + "秒");
			}
			return true;
		}
		if (key.equals("ie_delay")) {
			if (arg1.toString().trim().equals("")) {
				return false;
			}
			md.execSQL("update " + DBer.T_ITEM + " set delay=" + arg1 + " where itemid=" + itemid);
			if (arg1.toString().equals("0")) {
				arg0.setSummary("");
			} else {
				arg0.setSummary(arg1.toString() + "秒");
			}
			return true;
		}
		if (key.equals("ie_range")) {
			if (arg1.toString().trim().equals("")) {
				return false;
			}
			md.execSQL("update " + DBer.T_ITEM + " set range=" + arg1 + " where itemid=" + itemid);
			if (arg1.toString().equals("0")) {
				arg0.setSummary("");
			} else {
				arg0.setSummary(arg1.toString() + "米");
			}
			return true;
		}
		return true;
	}

	public boolean onPreferenceClick(Preference preference) {
		if (preference.getKey().startsWith("step")) {
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putInt("itemid", itemid);
			bundle.putInt("step", Integer.parseInt(preference.getKey().replace("step", "")));
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
			exit();
		}
		return false;
	}

	private void exit() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = settings.edit();
		editor.remove("ie_name");
		editor.remove("ie_tts");
		editor.remove("ie_endtts");
		editor.remove("ie_timeout");
		editor.remove("ie_delay");
		editor.remove("ie_range");
		for (int i = 1; i < 8; i++) {
			editor.remove("step" + i);
		}
		editor.commit();
		setResult(RESULT_OK);
		ItemStepActivity.this.finish();
	}

}
