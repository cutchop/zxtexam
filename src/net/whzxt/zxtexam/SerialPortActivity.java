package net.whzxt.zxtexam;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidParameterException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android_serialport_api.SerialPort;

public abstract class SerialPortActivity extends Activity {

	protected Metadata md;
	protected SerialPort mSerialPort;
	protected Socket mSocket;
	protected OutputStream mOutputStream;
	private InputStream mInputStream;
	private ReadThread mReadThread;
	protected Boolean isDeviceOK = false;
	protected TextToSpeech mTts;
	private byte[] mBuffer;
	private boolean writering = false;

	private class ReadThread extends Thread {
		@Override
		public void run() {
			super.run();
			byte[] buffer = new byte[32];
			int tsize = 0;
			while (!isInterrupted()) {
				int size;
				try {
					if (mInputStream == null)
						return;
					byte[] tmp = new byte[32];
					size = mInputStream.read(tmp);
					if (tsize == 0) {
						if (tmp[0] == 0x1A) {
							for (int i = 0; i < size; i++) {
								buffer[i] = tmp[i];
								tsize++;
							}
						}
					} else {
						for (int i = 0; i < size; i++) {
							buffer[tsize] = tmp[i];
							tsize++;
							if (tsize >= 32) {
								break;
							}
						}
					}
					if (tsize >= 17) {
						for (int i = 0; i < buffer.length; i++) {
							if (buffer[i] == 0x1D) {
								tsize = i + 1;
								break;
							}
						}
						if (buffer[tsize - 1] == 0x1D) {
							if (!isDeviceOK) {
								isDeviceOK = true;
							}
							onDataReceived(buffer, tsize);
							tsize = 0;
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}

	protected void writeSerial() {
		if (!writering) {
			writering = true;
			try {
				if (mOutputStream != null) {
					mOutputStream.write(mBuffer);
				}
				writering = false;
			} catch (IOException ex) {
				ex.printStackTrace();
				// 断开重连
				if (mReadThread != null) {
					mReadThread.interrupt();
					mReadThread = null;
				}
				md.closeSerialPort();
				mSerialPort = null;
				if (mInputStream != null) {
					try {
						mInputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					mInputStream = null;
				}
				if (mOutputStream != null) {
					try {
						mOutputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					mOutputStream = null;
				}
				if (mSocket != null) {
					try {
						mSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					mSocket = null;
				}
				link();
			}
		}
	}

	protected void DisplayError(int resourceId) {
		DisplayError(getString(resourceId));
	}

	protected void DisplayError(String msg) {
		if (mTts != null) {
			mTts.speak(msg, TextToSpeech.QUEUE_FLUSH, null);
		}
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setTitle("错误");
		b.setMessage(msg);
		b.setPositiveButton("关闭", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				destroy();
			}
		});
		b.show();
	}

	private void link() {
		if (md.getDataResourceType() == 0) {
			// 串口
			try {
				mSerialPort = md.getSerialPort();
				mOutputStream = mSerialPort.getOutputStream();
				mInputStream = mSerialPort.getInputStream();

				runOnUiThread(new Runnable() {
					public void run() {
						/* Create a receiving thread */
						mReadThread = new ReadThread();
						mReadThread.start();
						writering = false;
					}
				});
			} catch (SecurityException e) {
				runOnUiThread(new Runnable() {
					public void run() {
						DisplayError(R.string.error_security);
					}
				});
			} catch (IOException e) {
				runOnUiThread(new Runnable() {
					public void run() {
						DisplayError(R.string.error_unknown);
					}
				});
			} catch (InvalidParameterException e) {
				runOnUiThread(new Runnable() {
					public void run() {
						DisplayError(R.string.error_configuration);
					}
				});
			}
		} else {
			// WIFI
			try {
				mSocket = new Socket("192.168.11.254", 8000);
				mOutputStream = mSocket.getOutputStream();
				mInputStream = mSocket.getInputStream();

				runOnUiThread(new Runnable() {
					public void run() {
						/* Create a receiving thread */
						mReadThread = new ReadThread();
						mReadThread.start();
						writering = false;
					}
				});
			} catch (UnknownHostException e) {
				runOnUiThread(new Runnable() {
					public void run() {
						DisplayError(R.string.error_wifi);
					}
				});
			} catch (IOException e) {
				runOnUiThread(new Runnable() {
					public void run() {
						DisplayError(R.string.error_wifi);
					}
				});
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBuffer = new byte[7];
		mBuffer[0] = 0x1A;
		mBuffer[1] = 0x01;
		mBuffer[2] = 0x00;
		mBuffer[3] = 0x00;
		mBuffer[4] = 0x00;
		mBuffer[5] = 0x00;
		mBuffer[6] = 0x1D;
		md = (Metadata) getApplication();
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				link();
			}
		}, 1000);
	}

	protected abstract void onDataReceived(final byte[] buffer, final int size);

	protected void destroy() {
		if (mReadThread != null) {
			mReadThread.interrupt();
			mReadThread = null;
		}
		md.closeSerialPort();
		mSerialPort = null;
		if (mInputStream != null) {
			try {
				mInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mInputStream = null;
		}
		if (mOutputStream != null) {
			try {
				mOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mOutputStream = null;
		}
		if (mSocket != null) {
			try {
				mSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mSocket = null;
		}
		// Log.i("exam", "serialportactivity destroy");
	}
}
