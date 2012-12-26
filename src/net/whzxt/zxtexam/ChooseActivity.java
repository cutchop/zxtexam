package net.whzxt.zxtexam;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ChooseActivity extends Activity {

	private ListView listView;
	private Metadata md;
	private List<String> data;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose);
		listView = (ListView) findViewById(R.id.listView1);
		md = (Metadata) getApplication();
		data = new ArrayList<String>();
		Cursor cursor = md.rawQuery("select routeid,name from " + DBer.T_ROUTE + " order by routeid");
		if (cursor.moveToFirst()) {
			do {
				data.add(cursor.getString(cursor.getColumnIndex("name")));
			} while (cursor.moveToNext());
		}
		cursor.close();
		listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, data));
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putInt("routeid", arg2);
				bundle.putString("routename", data.get(arg2));
				intent.putExtras(bundle);
				intent.setClass(ChooseActivity.this, ExamActivity.class);
				startActivityForResult(intent, 0);
			}
		});
	}

}
