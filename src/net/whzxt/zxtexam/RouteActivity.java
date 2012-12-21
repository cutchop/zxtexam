package net.whzxt.zxtexam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class RouteActivity extends Activity {

	private ListView listView;
	private DBer sqlHelper;
	private SQLiteDatabase db;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route);
		listView = (ListView) findViewById(R.id.listView1);
		sqlHelper = new DBer(this, Metadata.DBNAME, null, Metadata.DBVERSION);
		db = sqlHelper.getWritableDatabase();
		Cursor cursor = db.rawQuery("select routeid,name from " + DBer.T_ROUTE + " order by routeid", null);
		List<String> data = new ArrayList<String>();
		if (cursor.moveToFirst()) {
			do {
				data.add(cursor.getString(cursor.getColumnIndex("name")));
			} while (cursor.moveToNext());
		}
		cursor.close();
		data.add("添加路线");
		listView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_expandable_list_item_1, data));
		listView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				/*
				Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putInt("routeid", arg2);
                intent.putExtras(bundle);
                intent.setClass(RouteActivity.this, RouteEditActivity.class);
                startActivity(intent);
                */
			}
			
		});
	}

}
