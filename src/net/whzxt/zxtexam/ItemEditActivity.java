package net.whzxt.zxtexam;

import android.R.id;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.DialogPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.KeyEvent;

public class ItemEditActivity extends PreferenceActivity implements
		OnPreferenceClickListener, OnPreferenceChangeListener {

	private DBer sqlHelper;
	private SQLiteDatabase db;
	private PreferenceCategory itemcategory;
	private Metadata md;
	private int itemid;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.itemedit);
		md = (Metadata) getApplication();
		itemcategory = (PreferenceCategory) findPreference("ie_actions");
		
        Bundle bundle = this.getIntent().getExtras();
        int id = bundle.getInt("itemid");
        itemid = id;
        
		sqlHelper = new DBer(this, Metadata.DBNAME, null, Metadata.DBVERSION);
		db = sqlHelper.getWritableDatabase();

		Preference preference = null;		
		Cursor cursor = db.rawQuery("select * from " + DBer.T_ITEM + " where itemid=" + id, null);
		if (cursor.moveToFirst()) {
			preference = findPreference("ie_name");
			preference.setSummary(cursor.getString(cursor.getColumnIndex("name")));
			preference.setOnPreferenceChangeListener(this);
			preference = findPreference("ie_tts");
			preference.setSummary(cursor.getString(cursor.getColumnIndex("tts")));
			preference.setOnPreferenceChangeListener(this);
			preference = findPreference("ie_timeout");
			preference.setSummary(String.valueOf(cursor.getInt(cursor.getColumnIndex("timeout"))+"秒"));
			preference.setOnPreferenceChangeListener(this);
		}
		cursor.close();

		for (int i = 0; i < 16; i++) {
			preference = findPreference("ie_action_" + i);
			preference.setTitle(md.getName(i));
			preference.setOnPreferenceChangeListener(this);
			preference = findPreference("ie_action_" + i + "_times");
			preference.setOnPreferenceChangeListener(this);
			preference = findPreference("ie_action_" + i + "_err");
			preference.setOnPreferenceChangeListener(this);
		}

		preference = findPreference("ie_action_20_min");
		preference.setTitle(md.getName(20) + "必须大于");
		preference.setOnPreferenceChangeListener(this);

		preference = findPreference("ie_action_20_max");
		preference.setTitle(md.getName(20) + "必须小于");
		preference.setOnPreferenceChangeListener(this);
		
		preference = findPreference("ie_action_21_min");
		preference.setTitle(md.getName(21) + "必须大于");
		preference.setOnPreferenceChangeListener(this);

		preference = findPreference("ie_action_21_max");
		preference.setTitle(md.getName(21) + "必须小于");
		preference.setOnPreferenceChangeListener(this);

		preference = findPreference("ie_action_30_min");
		preference.setTitle("GPS速度必须大于");
		preference.setOnPreferenceChangeListener(this);

		preference = findPreference("ie_action_30_max");
		preference.setTitle("GPS速度必须小于");
		preference.setOnPreferenceChangeListener(this);
		
		preference = findPreference("ie_action_31");
		preference.setTitle("GPS角度偏差必须大于");
		preference.setOnPreferenceChangeListener(this);
		
		itemcategory = (PreferenceCategory) findPreference("ie_notallows");
		
		for (int i = 0; i < 16; i++) {
			preference = findPreference("ie_notallow_" + i);
			preference.setTitle(md.getName(i));
			preference.setOnPreferenceChangeListener(this);
			preference = findPreference("ie_notallow_" + i + "_err");
			preference.setOnPreferenceChangeListener(this);
		}
		preference = findPreference("ie_notallow_20_min");
		preference.setTitle(md.getName(20) + "不能大于");
		preference.setOnPreferenceChangeListener(this);

		preference = findPreference("ie_notallow_20_max");
		preference.setTitle(md.getName(20) + "不能小于");
		preference.setOnPreferenceChangeListener(this);
		
		preference = findPreference("ie_notallow_21_min");
		preference.setTitle(md.getName(21) + "不能大于");
		preference.setOnPreferenceChangeListener(this);

		preference = findPreference("ie_notallow_21_max");
		preference.setTitle(md.getName(21) + "不能小于");
		preference.setOnPreferenceChangeListener(this);

		preference = findPreference("ie_notallow_30_min");
		preference.setTitle("GPS速度不能大于");
		preference.setOnPreferenceChangeListener(this);

		preference = findPreference("ie_notallow_30_max");
		preference.setTitle("GPS速度不能小于");
		preference.setOnPreferenceChangeListener(this);
		
		preference = findPreference("ie_notallow_31");
		preference.setTitle("GPS角度偏差不能大于");
		preference.setOnPreferenceChangeListener(this);
		
		int times = 1;
		cursor = db.rawQuery("select a.*,b.name as errname from " + DBer.T_ITEM_ACTION + " a left join "+DBer.T_ITEM_ERR+" b on a.errid=b.errid where a.itemid=" + id + " order by dataid", null);
		if (cursor.moveToFirst()) {
			do {
				id = cursor.getInt(cursor.getColumnIndex("dataid")); 
				times = cursor.getInt(cursor.getColumnIndex("times"));
				if (cursor.getInt(cursor.getColumnIndex("dataid"))<20) {
					if (times>0) {
						preference = findPreference("ie_action_"+id);
						((CheckBoxPreference)preference).setChecked(true);
						preference = findPreference("ie_action_"+id+"_times");
						preference.setSummary(String.valueOf(times));
						preference = findPreference("ie_action_"+id+"_err");
						preference.setSummary(cursor.getString(cursor.getColumnIndex("errname")));
					}else {
						preference = findPreference("ie_notallow_"+id);
						((CheckBoxPreference)preference).setChecked(true);
						preference = findPreference("ie_notallow_"+id+"_err");
						preference.setSummary(cursor.getString(cursor.getColumnIndex("errname")));
					}
					continue;
				}
				if (cursor.getInt(cursor.getColumnIndex("dataid"))<31) {
					if (times>0) {
						preference = findPreference("ie_action_"+id+"_min");
						preference.setSummary(String.valueOf(cursor.getInt(cursor.getColumnIndex("min"))));
						preference = findPreference("ie_action_"+id+"_max");
						preference.setSummary(String.valueOf(cursor.getInt(cursor.getColumnIndex("max"))));
					} else{
						preference = findPreference("ie_notallow_"+id+"_min");
						preference.setSummary(String.valueOf(cursor.getInt(cursor.getColumnIndex("min"))));
						preference = findPreference("ie_notallow_"+id+"_max");
						preference.setSummary(String.valueOf(cursor.getInt(cursor.getColumnIndex("max"))));
					}
					continue;
				}
				if (times>0) {
					preference = findPreference("ie_action_31_range");
				}else {
					preference = findPreference("ie_notallow_31_range");
				}
				preference.setSummary(String.valueOf(cursor.getInt(cursor.getColumnIndex("max"))) + "度");
			} while (cursor.moveToNext());
		}
		cursor.close();
	}

	public boolean onPreferenceChange(Preference arg0, Object arg1) {
		String key = arg0.getKey();
		if (key.startsWith("ie_action_")) {
			if (key.endsWith("_times")) {
				String id = key.replace("ie_action_", "").replace("_times", "");
				db.execSQL("update "+DBer.T_ITEM_ACTION+" set times="+arg1+" where dataid="+id);
			} else if(key.endsWith("_err")) {
				String id = key.replace("ie_action_", "").replace("_err", "");
				db.execSQL("update "+DBer.T_ITEM_ACTION+" set errid="+arg1+" where dataid="+id);
			} else if(key.endsWith("_min")) {
				String id = key.replace("ie_action_", "").replace("_min", "");
				db.execSQL("update "+DBer.T_ITEM_ACTION+" set min="+arg1+" where dataid="+id);
			} else if(key.endsWith("_max")) {
				String id = key.replace("ie_action_", "").replace("_max", "");
				db.execSQL("update "+DBer.T_ITEM_ACTION+" set max="+arg1+" where dataid="+id);
			} else if(key.endsWith("_range")) {
				String id = key.replace("ie_action_", "").replace("_range", "");
				db.execSQL("update "+DBer.T_ITEM_ACTION+" set max="+arg1+" where dataid="+id);
			} else {
				String id = key.replace("ie_action_", "");
				if ((Boolean)arg1) {
					db.execSQL("INSERT INTO " + DBer.T_ITEM_ACTION + " (itemid, dataid, times, min, max, errid) VALUES ("+itemid+","+id+",1,0,0,999)");
				}else {
					db.execSQL("delete from "+ DBer.T_ITEM_ACTION +" where dataid="+id);
				}
			}
			return true;
		}

		if (key.startsWith("ie_notallow_")) {
			if (key.endsWith("_times")) {
				String id = key.replace("ie_notallow_", "").replace("_times", "");
				db.execSQL("update "+DBer.T_ITEM_ACTION+" set times="+arg1+" where dataid="+id);
			} else if(key.endsWith("_err")) {
				String id = key.replace("ie_notallow_", "").replace("_err", "");
				db.execSQL("update "+DBer.T_ITEM_ACTION+" set errid="+arg1+" where dataid="+id);
			} else if(key.endsWith("_min")) {
				String id = key.replace("ie_notallow_", "").replace("_min", "");
				db.execSQL("update "+DBer.T_ITEM_ACTION+" set min="+arg1+" where dataid="+id);
			} else if(key.endsWith("_max")) {
				String id = key.replace("ie_notallow_", "").replace("_max", "");
				db.execSQL("update "+DBer.T_ITEM_ACTION+" set max="+arg1+" where dataid="+id);
			} else if(key.endsWith("_range")) {
				String id = key.replace("ie_notallow_", "").replace("_range", "");
				db.execSQL("update "+DBer.T_ITEM_ACTION+" set max="+arg1+" where dataid="+id);
			} else {
				String id = key.replace("ie_notallow_", "");
				if ((Boolean)arg1) {
					db.execSQL("INSERT INTO " + DBer.T_ITEM_ACTION + " (itemid, dataid, times, min, max, errid) VALUES ("+itemid+","+id+",0,0,0,999)");
				}else {
					db.execSQL("delete from "+ DBer.T_ITEM_ACTION +" where dataid="+id);
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
		 SharedPreferences	settings = PreferenceManager.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = settings.edit();
			for (int i = 0; i < 16; i++) {
				editor.remove("ie_action_"+i);
				editor.remove("ie_action_"+i+"_times");
				editor.remove("ie_action_"+i+"_err");
				editor.remove("ie_notallow_"+i);
				editor.remove("ie_notallow_"+i+"_err");
			}
			editor.remove("ie_action_20_min");
			editor.remove("ie_action_20_max");
			editor.remove("ie_action_21_min");
			editor.remove("ie_action_21_max");
			editor.remove("ie_action_30_min");
			editor.remove("ie_action_30_max");
			editor.remove("ie_action_31_range");
			editor.remove("ie_notallow_20_min");
			editor.remove("ie_notallow_20_max");
			editor.remove("ie_notallow_21_min");
			editor.remove("ie_notallow_21_max");
			editor.remove("ie_notallow_30_min");
			editor.remove("ie_notallow_30_max");
			editor.remove("ie_notallow_31_range");
			editor.commit();
		}
		return false;
	}

}
