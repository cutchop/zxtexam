package net.whzxt.zxtexam;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;

public class ItemsActivity extends PreferenceActivity implements OnPreferenceClickListener {

	private PreferenceCategory itemcategory;
	private Metadata md;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.itemsettings);
		itemcategory = (PreferenceCategory) findPreference("itemcategory");
		md = (Metadata)getApplication();
		Cursor cursor = md.rawQuery("select * from " + DBer.T_ITEM + " order by itemid");
		if (cursor.moveToFirst()) {
			do {
				Preference preference = new Preference(this);
				preference.setKey("item" + cursor.getInt(cursor.getColumnIndex("itemid")));
				preference.setTitle(cursor.getString(cursor.getColumnIndex("name")));
				preference.setOnPreferenceClickListener(this);
				itemcategory.addPreference(preference);
			} while (cursor.moveToNext());
		}
		cursor.close();
	}

	public boolean onPreferenceClick(Preference preference) {
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putInt("itemid", Integer.parseInt(preference.getKey().substring(4)));
		intent.putExtras(bundle);
		intent.setClass(ItemsActivity.this, ItemStepActivity.class);
		startActivity(intent);
		return false;
	}

}
