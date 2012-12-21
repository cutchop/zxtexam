package net.whzxt.zxtexam;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ChooseActivity extends Activity {

	private ListView listView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose);
		listView = (ListView) findViewById(R.id.listView1);
		List<String> data = new ArrayList<String>();
		data.add("灯光");
		data.add("路线1");
		data.add("路线2");
		data.add("路线3");
		listView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_expandable_list_item_1, data));
		listView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				startActivity(new Intent().setClass(ChooseActivity.this, ExamActivity.class));
			}
			
		});
	}

}
