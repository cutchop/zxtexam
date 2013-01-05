package net.whzxt.zxtexam;

import java.util.HashMap;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.Toast;

public class ItemEditActivity extends PreferenceActivity implements OnPreferenceClickListener, OnPreferenceChangeListener {

	private Metadata md;
	private int itemid;
	private String[] errnames;
	private String[] errvalues;
	private HashMap<String, String> errmap;
	private String[] timesnames = { "信号等于1就扣分", "信号等于0就扣分", "信号等于1就通过", "信号等于0就通过", "信号等于1并且保持1秒以上就通过", "信号等于0并且保持1秒以上就通过", "信号等于1并且保持3秒以上就通过", "信号等于0并且保持3秒以上就通过" };
	private String[] timesvalues = { "1", "-1", "2", "-2", "3", "-3", "4", "-4" };
	private HashMap<String, String> timesmap;
	private int step;
	private int fanx = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.itemedit);
		md = (Metadata) getApplication();
		timesmap = new HashMap<String, String>();
		for (int i = 0; i < timesnames.length; i++) {
			timesmap.put(timesvalues[i], timesnames[i]);
		}

		Bundle bundle = this.getIntent().getExtras();
		int id = bundle.getInt("itemid");
		itemid = id;
		step = bundle.getInt("step");

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

		cursor = md.rawQuery("select * from " + DBer.T_ITEM_ERR + " where itemid=" + id);
		if (cursor.getCount() > 0) {
			errnames = new String[cursor.getCount()];
			errvalues = new String[cursor.getCount()];
			if (cursor.moveToFirst()) {
				errnames[0] = cursor.getString(cursor.getColumnIndex("name"));
				errvalues[0] = cursor.getString(cursor.getColumnIndex("errid"));
				for (int i = 1; cursor.moveToNext(); i++) {
					errnames[i] = cursor.getString(cursor.getColumnIndex("name"));
					errvalues[i] = cursor.getString(cursor.getColumnIndex("errid"));
				}
			}
		} else {
			errnames = new String[] { "请先在系统设置里为该项目添加扣分项" };
			errvalues = new String[] { "999" };
		}
		errmap = new HashMap<String, String>();
		for (int i = 0; i < errnames.length; i++) {
			errmap.put(errvalues[i], errnames[i]);
		}
		cursor.close();
		for (int i = 0; i < 16; i++) {
			preference = findPreference("ie_action_" + i);
			preference.setTitle(md.getName(i));
			preference.setOnPreferenceChangeListener(this);
			preference = findPreference("ie_action_" + i + "_times");
			((ListPreference) preference).setEntries(timesnames);
			((ListPreference) preference).setEntryValues(timesvalues);
			preference.setOnPreferenceChangeListener(this);
			preference = findPreference("ie_action_" + i + "_err");
			((ListPreference) preference).setEntries(errnames);
			((ListPreference) preference).setEntryValues(errvalues);
			preference.setOnPreferenceChangeListener(this);
		}
		findPreference("ie_action_20").setOnPreferenceChangeListener(this);
		((EditTextPreference) findPreference("ie_action_20_min")).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		findPreference("ie_action_20_min").setOnPreferenceChangeListener(this);
		((EditTextPreference) findPreference("ie_action_20_max")).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		findPreference("ie_action_20_max").setOnPreferenceChangeListener(this);
		findPreference("ie_action_21").setOnPreferenceChangeListener(this);
		((EditTextPreference) findPreference("ie_action_21_min")).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		findPreference("ie_action_21_min").setOnPreferenceChangeListener(this);
		((EditTextPreference) findPreference("ie_action_21_max")).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		findPreference("ie_action_21_max").setOnPreferenceChangeListener(this);
		findPreference("ie_action_30").setOnPreferenceChangeListener(this);
		((EditTextPreference) findPreference("ie_action_30_min")).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		findPreference("ie_action_30_min").setOnPreferenceChangeListener(this);
		((EditTextPreference) findPreference("ie_action_30_max")).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		findPreference("ie_action_30_max").setOnPreferenceChangeListener(this);
		findPreference("ie_action_31").setOnPreferenceChangeListener(this);
		((EditTextPreference) findPreference("ie_action_31_min")).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		findPreference("ie_action_31_min").setOnPreferenceChangeListener(this);
		((EditTextPreference) findPreference("ie_action_31_max")).getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		findPreference("ie_action_31_max").setOnPreferenceChangeListener(this);

		preference = findPreference("ie_action_20_err");
		((ListPreference) preference).setEntries(errnames);
		((ListPreference) preference).setEntryValues(errvalues);
		preference.setOnPreferenceChangeListener(this);
		preference = findPreference("ie_action_21_err");
		((ListPreference) preference).setEntries(errnames);
		((ListPreference) preference).setEntryValues(errvalues);
		preference.setOnPreferenceChangeListener(this);
		preference = findPreference("ie_action_30_err");
		((ListPreference) preference).setEntries(errnames);
		((ListPreference) preference).setEntryValues(errvalues);
		preference.setOnPreferenceChangeListener(this);
		preference = findPreference("ie_action_31_err");
		((ListPreference) preference).setEntries(errnames);
		((ListPreference) preference).setEntryValues(errvalues);
		preference.setOnPreferenceChangeListener(this);
		preference = findPreference("ie_action_31_fanx");
		((ListPreference) preference).setEntries(new String[] { "向左", "向右" });
		((ListPreference) preference).setEntryValues(new String[] { "-1", "1" });
		preference.setSummary("向左");
		preference.setOnPreferenceChangeListener(this);

		cursor = md.rawQuery("select * from " + DBer.T_ITEM_ACTION + " where step=" + step + " and itemid=" + id + " order by dataid");
		if (cursor.moveToFirst()) {
			do {
				id = cursor.getInt(cursor.getColumnIndex("dataid"));
				((CheckBoxPreference) findPreference("ie_action_" + id)).setChecked(true);
				findPreference("ie_action_" + id + "_err").setSummary(errmap.get(cursor.getString(cursor.getColumnIndex("errid"))));
				if (id < 20) {
					findPreference("ie_action_" + id + "_times").setSummary(timesmap.get(String.valueOf(cursor.getInt(cursor.getColumnIndex("times")))));
				} else {
					if (id == 31) {
						findPreference("ie_action_" + id + "_min").setSummary(String.valueOf(Math.abs(cursor.getInt(cursor.getColumnIndex("min")))));
						findPreference("ie_action_" + id + "_max").setSummary(String.valueOf(Math.abs(cursor.getInt(cursor.getColumnIndex("max")))));
						if (cursor.getInt(cursor.getColumnIndex("min")) > 0 || cursor.getInt(cursor.getColumnIndex("max")) > 0) {
							findPreference("ie_action_31_fanx").setSummary("向右");
							fanx = 1;
						}
					} else {
						findPreference("ie_action_" + id + "_min").setSummary(String.valueOf(cursor.getInt(cursor.getColumnIndex("min"))));
						findPreference("ie_action_" + id + "_max").setSummary(String.valueOf(cursor.getInt(cursor.getColumnIndex("max"))));
					}
				}
			} while (cursor.moveToNext());
		}
		cursor.close();
	}

	public boolean onPreferenceChange(final Preference arg0, final Object arg1) {
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
		if (key.startsWith("ie_action_")) {
			if (key.endsWith("_times")) {
				String id = key.replace("ie_action_", "").replace("_times", "");
				md.execSQL("update " + DBer.T_ITEM_ACTION + " set times=" + arg1 + " where itemid=" + itemid + " and dataid=" + id + " and step=" + step);
				arg0.setSummary(timesmap.get(arg1));
			} else if (key.endsWith("_err")) {
				final String id = key.replace("ie_action_", "").replace("_err", "");
				md.execSQL("update " + DBer.T_ITEM_ACTION + " set errid=" + arg1 + " where itemid=" + itemid + " and dataid=" + id + " and step=" + step);
				arg0.setSummary(errmap.get(arg1));
				final EditText txtAppend = new EditText(ItemEditActivity.this);
				AlertDialog alertDialog = new AlertDialog.Builder(ItemEditActivity.this).setTitle("如果要附加说明，请输入然后点击确定，否则点击取消").setIcon(android.R.drawable.ic_menu_add).setView(txtAppend)
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								if (txtAppend.getText().toString().trim().equals("")) {
									return;
								}
								Cursor cur = md.rawQuery("select max(errid)+1 as id from " + DBer.T_ITEM_ERR);
								if (cur.moveToFirst()) {
									md.execSQL("insert into " + DBer.T_ITEM_ERR + "(errid, itemid, name, fenshu) select " + cur.getInt(0) + " as errid,itemid,name||'(" + txtAppend.getText()
											+ ")' as name,fenshu from " + DBer.T_ITEM_ERR + " where errid=" + arg1);
									md.execSQL("update " + DBer.T_ITEM_ACTION + " set errid=" + cur.getInt(0) + " where itemid=" + itemid + " and dataid=" + id + " and step=" + step);
									errmap.put(String.valueOf(cur.getInt(0)), errmap.get(arg1).toString() + "(" + txtAppend.getText().toString() + ")");
									Log.i("exam", errmap.get(arg1).toString() + "(" + txtAppend.getText().toString() + ")");
									arg0.setSummary(errmap.get(String.valueOf(cur.getInt(0))));
								} else {
									Toast.makeText(ItemEditActivity.this, "出现错误,请重试", Toast.LENGTH_SHORT).show();
								}
								cur.close();
							}
						}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								return;
							}
						}).create();
				alertDialog.show();
			} else if (key.endsWith("_min")) {
				if (arg1.toString().equals("")) {
					return false;
				}
				String id = key.replace("ie_action_", "").replace("_min", "");
				if (id.equals("31")) {
					md.execSQL("update " + DBer.T_ITEM_ACTION + " set min=" + (Integer.parseInt(arg1.toString()) * fanx) + " where itemid=" + itemid + " and dataid=" + id + " and step=" + step);
				} else {
					md.execSQL("update " + DBer.T_ITEM_ACTION + " set min=" + arg1 + " where itemid=" + itemid + " and dataid=" + id + " and step=" + step);
				}
				arg0.setSummary(arg1.toString());
			} else if (key.endsWith("_max")) {
				if (arg1.toString().equals("")) {
					return false;
				}
				String id = key.replace("ie_action_", "").replace("_max", "");
				if (id.equals("31")) {
					md.execSQL("update " + DBer.T_ITEM_ACTION + " set max=" + (Integer.parseInt(arg1.toString()) * fanx) + " where itemid=" + itemid + " and dataid=" + id + " and step=" + step);
				} else {
					md.execSQL("update " + DBer.T_ITEM_ACTION + " set max=" + arg1 + " where itemid=" + itemid + " and dataid=" + id + " and step=" + step);
				}
				arg0.setSummary(arg1.toString());
			} else if (key.endsWith("_fanx")) {
				if (arg1.toString().equals("")) {
					return false;
				}
				if (Integer.parseInt(arg1.toString()) == 1) {
					fanx = 1;
					md.execSQL("update " + DBer.T_ITEM_ACTION + " set max=max*-1 where itemid=" + itemid + " and dataid=31 and max < 0 and step=" + step);
					md.execSQL("update " + DBer.T_ITEM_ACTION + " set min=min*-1 where itemid=" + itemid + " and dataid=31 and min < 0 and step=" + step);
					arg0.setSummary("向右");
				} else {
					fanx = -1;
					md.execSQL("update " + DBer.T_ITEM_ACTION + " set max=max*-1 where itemid=" + itemid + " and dataid=31 and max > 0 and step=" + step);
					md.execSQL("update " + DBer.T_ITEM_ACTION + " set min=min*-1 where itemid=" + itemid + " and dataid=31 and min > 0 and step=" + step);
					arg0.setSummary("向左");
				}
			} else {
				String id = key.replace("ie_action_", "");
				if ((Boolean) arg1) {
					md.execSQL("INSERT INTO " + DBer.T_ITEM_ACTION + " (itemid, dataid, times, min, max, errid, step) VALUES (" + itemid + "," + id + ",0,0,0,999," + step + ")");
				} else {
					md.execSQL("delete from " + DBer.T_ITEM_ACTION + " where itemid=" + itemid + " and dataid=" + id + " and step=" + step);
					findPreference(key + "_err").setSummary("");
					if (findPreference(key + "_times") != null) {
						findPreference(key + "_times").setSummary("");
					} else {
						findPreference(key + "_min").setSummary("");
						findPreference(key + "_max").setSummary("");
					}
				}
			}
			return true;
		}
		return true;
	}

	public boolean onPreferenceClick(Preference preference) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = settings.edit();
			editor.remove("ie_name");
			editor.remove("ie_tts");
			editor.remove("ie_timeout");
			for (int i = 0; i < 16; i++) {
				editor.remove("ie_action_" + i);
				editor.remove("ie_action_" + i + "_times");
				editor.remove("ie_action_" + i + "_err");
			}
			editor.remove("ie_action_20");
			editor.remove("ie_action_21");
			editor.remove("ie_action_30");
			editor.remove("ie_action_31");

			editor.remove("ie_action_20_min");
			editor.remove("ie_action_21_min");
			editor.remove("ie_action_30_min");
			editor.remove("ie_action_31_min");

			editor.remove("ie_action_20_max");
			editor.remove("ie_action_21_max");
			editor.remove("ie_action_30_max");
			editor.remove("ie_action_31_max");

			editor.remove("ie_action_20_err");
			editor.remove("ie_action_21_err");
			editor.remove("ie_action_30_err");
			editor.remove("ie_action_31_err");

			editor.remove("ie_action_31_fanx");
			editor.commit();
			setResult(RESULT_OK);
			this.finish();
		}
		return false;
	}

}
