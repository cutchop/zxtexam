package net.whzxt.zxtexam;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ExamListAdapter extends ArrayAdapter<HashMap<String, Object>> {

	public ExamListAdapter(Context context, List<HashMap<String, Object>> objects) {
		super(context, 0, objects);
	}

	@Override
	public boolean isEnabled(int position) {
		return super.isEnabled(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		view = LayoutInflater.from(getContext()).inflate(R.layout.listlayout, null);
		TextView txtItem = (TextView) view.findViewById(R.id.textView1);
		TextView txtKoufen = (TextView) view.findViewById(R.id.textView2);
		TextView txtErr = (TextView) view.findViewById(R.id.textView3);
		txtItem.setText(getItem(position).get("itemname").toString());
		txtKoufen.setText("-" + getItem(position).get("fenshu").toString());
		txtErr.setText(getItem(position).get("errname").toString());
		return view;
	}
}