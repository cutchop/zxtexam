package net.whzxt.zxtexam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter;

public class ErrSettingsActivity extends Activity {

	private ListView listView;
	private DBer sqlHelper;
	private SQLiteDatabase db;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_errsettings);
		listView = (ListView) findViewById(R.id.listView1);
		sqlHelper = new DBer(this, Metadata.DBNAME, null, Metadata.DBVERSION);
		db = sqlHelper.getWritableDatabase();
		Cursor cursor = db.rawQuery("select a.errid,a.name,a.fenshu,b.name as itemname from " + DBer.T_ITEM_ERR + " a left join " + DBer.T_ITEM + " b on a.itemid=b.itemid", null);
		List<Map<String, String>> data = new ArrayList<Map<String, String>>();
		if (cursor.moveToFirst()) {
			do {
				Map<String, String> datum = new HashMap<String, String>(2);
				datum.put("name", cursor.getString(cursor.getColumnIndex("name")));
				datum.put("fenshu", cursor.getString(cursor.getColumnIndex("itemname")) + "  " + cursor.getString(cursor.getColumnIndex("fenshu")) + "分");
				data.add(datum);
			} while (cursor.moveToNext());
		}
		cursor.close();
		listView.setAdapter(new SimpleAdapter(this, data, android.R.layout.simple_expandable_list_item_2, new String[] { "name", "fenshu" }, new int[] { android.R.id.text1, android.R.id.text2 }));
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

			}

		});
	}
}
