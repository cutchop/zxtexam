package net.whzxt.zxtexam;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.WindowManager;
import android.widget.TextView;
import android.app.Activity;
import android.content.Intent;

public class WelcomeActivity extends Activity implements OnInitListener {

	private TextToSpeech mTts;
	private static final int REQ_TTS_STATUS_CHECK = 0;
	private TextView txtServiceInfo;

	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			if (mTts != null) {
				try {					
					mTts.speak("欢迎使用练车宝", TextToSpeech.QUEUE_FLUSH, null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					runOnUiThread(new Runnable() {
						public void run() {
							if (mTts != null) {
								try {
									mTts.shutdown();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							startActivity(new Intent().setClass(WelcomeActivity.this, MainActivity.class));
							WelcomeActivity.this.finish();
						}
					});
				}
			}, 2000);
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQ_TTS_STATUS_CHECK) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				mTts = new TextToSpeech(this, this);
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		txtServiceInfo = (TextView)findViewById(R.id.txtServiceInfo);
		txtServiceInfo.setText(((Metadata) getApplicationContext()).getServiceInfo());
		if (mTts == null) {
			Intent checkIntent = new Intent();
			checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
			startActivityForResult(checkIntent, REQ_TTS_STATUS_CHECK);
		}
	}
	
	@Override
	public void onAttachedToWindow() {
		this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
		super.onAttachedToWindow();
	}
}
