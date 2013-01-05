package net.whzxt.zxtexam;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android_serialport_api.SerialPort;

public abstract class SerialPortActivity extends Activity {

	protected Metadata md;
	protected SerialPort mSerialPort;
	protected OutputStream mOutputStream;
	private InputStream mInputStream;
	private ReadThread mReadThread;

	private class ReadThread extends Thread {

		@Override
		public void run() {
			super.run();
			while (!isInterrupted()) {
				int size;
				try {
					if (mInputStream == null)
						return;
					byte[] buffer = new byte[32];
					size = mInputStream.read(buffer);
					if (size >= 17) {
						onDataReceived(buffer, size);
					}
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}

	private void DisplayError(int resourceId) {
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setTitle("Error");
		b.setMessage(resourceId);
		b.setPositiveButton("OK", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				SerialPortActivity.this.finish();
			}
		});
		b.show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		md = (Metadata) getApplication();
		if (md.getDataResourceType() == 0) {
			// 串口
			try {
				mSerialPort = md.getSerialPort();
				mOutputStream = mSerialPort.getOutputStream();
				mInputStream = mSerialPort.getInputStream();

				/* Create a receiving thread */
				mReadThread = new ReadThread();
				mReadThread.start();
			} catch (SecurityException e) {
				DisplayError(R.string.error_security);
				e.printStackTrace();
			} catch (IOException e) {
				DisplayError(R.string.error_unknown);
				e.printStackTrace();
			} catch (InvalidParameterException e) {
				DisplayError(R.string.error_configuration);
				e.printStackTrace();
			}
		} else {
			// 蓝牙
			BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
			if (!mAdapter.isEnabled()) {
				mAdapter.enable();
			}
			// 搜索设备
			
		}
	}

	protected abstract void onDataReceived(final byte[] buffer, final int size);

	@Override
	protected void onDestroy() {
		if (mReadThread != null)
			mReadThread.interrupt();
		md.closeSerialPort();
		mSerialPort = null;
		super.onDestroy();
	}
}
