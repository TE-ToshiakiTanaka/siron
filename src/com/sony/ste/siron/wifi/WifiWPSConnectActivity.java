package com.sony.ste.siron.wifi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Timer;
import java.util.TimerTask;

import com.sony.ste.siron.R;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class WifiWPSConnectActivity extends Activity {

	public static final String WPSCONNECT = "com.sony.ste.siron.WIFI_WPS_CONNECT";

	private final static String ANDROID_NET_WIFI_WIFIMANAGER_PKG = "android.net.wifi.WifiManager";
	private final static String WPS_LISTENER = "android.net.wifi.WifiManager$WpsListener";
	private final static String WPS_ACTION_LISTENER = "android.net.wifi.WifiManager$ActionListener";
	private final static String START_WPS_METHOD = "startWps";
	private final static String CANCEL_WPS_METHOD = "cancelWps";
	private final static String WPS_METHOD_INT = "wps";
	private final static String BSSID_INT = "bssid";
	private final static String PIN_KEY_PAD_INT = "pinkeypad";
	private final static String TAG = "com.example.wifiwpsconnection.WifiWPSConnectActivity";
	@SuppressLint("SdCardPath")
	private final static String PIN_FILE = "/sdcard/pin.txt";
	private final static int WPS_TIMEOUT_S = 120;

	// Component
	private WifiManager wifiManager;
	private EditText pinEditText;
	private EditText wpsEditText;
	private ProgressBar progressBar;
	private Button cancelButton;
	private Timer timer;
	private TimerTask timerTask;
	private Handler handler = new Handler();
	private File pinFile;
	private FileWriter fw;
    //private BufferedWriter bw;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wpsconnection);
		this.wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if (this.wifiManager == null) {
			Toast.makeText(this, "Fail to initialize WiFi Manager",
					Toast.LENGTH_SHORT).show();
		} else {
			if (!this.wifiManager.isWifiEnabled()) {
				Toast.makeText(this, "Turn on Wi-Fi...", Toast.LENGTH_SHORT)
						.show();
				this.wifiManager.setWifiEnabled(true);
			}
		}
		this.pinEditText = (EditText) findViewById(R.id.pinTextEdit);
		this.wpsEditText = (EditText) findViewById(R.id.wpsMethodTextEdit);
		this.progressBar = (ProgressBar) findViewById(R.id.progressBar);
		this.cancelButton = (Button) findViewById(R.id.cancelButton);
		this.cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				wpsCancel();
				if (timer != null && timerTask != null) {
					Log.i(TAG, "Now Purge Timer.");
					timerTask.cancel();
					timer.purge();
					timer.cancel();
				}
				progressBar.setProgress(0);
				pinEditText.setText("");
				wpsEditText.setText("");
			}
		});
		this.pinFile = new File(PIN_FILE);
		try {
			if(!this.pinFile.exists())
				this.pinFile.createNewFile();

			//this.bw = new BufferedWriter(this.fw);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@SuppressLint("NewApi")
	@Override
	protected void onStart() {
		super.onStart();
		// Get Intent
		Intent intent = getIntent();
		String method = intent.getStringExtra(WPS_METHOD_INT);
		if (method != null) {
			if (method.equals("PIN"))
				wpsConnection("PIN", null, null);
			if (method.equals("PBC"))
				wpsConnection("PBC", null, null);
			if (method.equals("PINKeyPad")) {
				String bssid = intent.getStringExtra(BSSID_INT);
				String pinKeyPad = intent.getStringExtra(PIN_KEY_PAD_INT);
				if (bssid != null && pinKeyPad != null)
					wpsConnection("PINKeyPad", bssid, pinKeyPad);
			}
		} else {
			Toast.makeText(
					this,
					"WPS Method is NOT specified by intent. Please start WPS manually",
					Toast.LENGTH_SHORT).show();
			// wpsConnection("PBC");
		}

	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.i(TAG, "onStop is called.");
		wpsCancel();
	}

	/**
	 * startWps Get PIN Code of Enrollee
	 *
	 * Option: PBC / PIN
	 */
	@SuppressLint("NewApi")
	private void wpsConnection(String option, String bssid, String pin) {
		if (this.wifiManager == null)
			return;
		// Setup WpsInfo
		WpsInfo wpsInfo = new WpsInfo();
		if (option.equals("PBC")) {
			wpsInfo.setup = WpsInfo.PBC;
			this.wpsEditText.setText("");
			this.wpsEditText.setText("PBC");
			this.pinEditText.setText("");
		} else if (option.equals("PIN")) {
			wpsInfo.setup = WpsInfo.DISPLAY;
			this.wpsEditText.setText("");
			this.wpsEditText.setText("PIN Entry");
		} else if (option.equals("PINKeyPad")) {
			Log.i(TAG, "Key Pad Mode.");
			wpsInfo.setup = WpsInfo.KEYPAD;
			wpsInfo.pin = pin;
			Field bssidField;
			try {
				bssidField = wpsInfo.getClass().getField("BSSID");
				bssidField.set(wpsInfo, bssid);
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		} else
			return;
		// Start WPS Connection
		try {
			final Class<?> wifiManagerCls = Class
					.forName(ANDROID_NET_WIFI_WIFIMANAGER_PKG);
			Class<?> wpsListenerCls = Class.forName(WPS_LISTENER);
			final Method onStartSuccess = wpsListenerCls.getMethod(
					"onStartSuccess", String.class);
			final Method onCompletion = wpsListenerCls
					.getMethod("onCompletion");
			final Method onFailure = wpsListenerCls.getMethod("onFailure",
					int.class);
			this.timer = new Timer(false);
			if (this.timerTask != null) {
				this.timerTask.cancel();
			}
			this.timerTask = new TimerTask() {
				@Override
				public void run() {
					handler.post(new Runnable() {
						@Override
						public void run() {
							progressBar.incrementProgressBy(1);
						}
					});
				}
			};
			this.progressBar.setMax(WPS_TIMEOUT_S);
			this.progressBar.setProgress(0);
			this.progressBar.setVisibility(View.VISIBLE);
			this.timer.purge();
			this.timer.schedule(this.timerTask, 1000, 1000);
			Object wpsListener = Proxy.newProxyInstance(
					wpsListenerCls.getClassLoader(),
					new Class<?>[] { wpsListenerCls }, new InvocationHandler() {

						@Override
						public Object invoke(Object arg0, Method method,
								final Object[] arg2) throws Throwable {
							if (method.equals(onStartSuccess)) {
								if (arg2[0] != null) {
									Log.i(TAG, ((String) arg2[0]).toString());
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											pinEditText.setText("");
											pinEditText
													.setText((CharSequence) arg2[0]);
										}

									});
									String pin = (String)arg2[0];
									//bw.write(pin);
									//bw.flush();
									fw = new FileWriter(pinFile, false);
									fw.write(pin);
									fw.flush();
									fw.close();
									Toast.makeText(
											WifiWPSConnectActivity.this,
											"PIN Entry: "
													+ (CharSequence) arg2[0],
											Toast.LENGTH_LONG).show();
								} else {
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											pinEditText.setText("");
											pinEditText.setText("No PIN Code");
										}

									});
									Toast.makeText(WifiWPSConnectActivity.this,
											"WPS Start.", Toast.LENGTH_LONG)
											.show();
									fw = new FileWriter(pinFile, false);
									fw.write("PBC");
									fw.flush();
									fw.close();
								}
							}
							if (method.equals(onCompletion)) {
								timer.cancel();
								progressBar.setProgress(0);
								Log.i(TAG, "WPS Connection Completed.");
								Toast.makeText(WifiWPSConnectActivity.this,
										"WPS Connection Completed.",
										Toast.LENGTH_LONG).show();
							}
							if (method.equals(onFailure)) {
								timer.cancel();
								progressBar.setProgress(0);
								int code = (Integer) arg2[0];
								/* Code 0 */
								Field wpsError = wifiManagerCls
										.getField("ERROR");
								/* Code 1 */
								Field wpsInProgress = wifiManagerCls
										.getField("IN_PROGRESS");
								/* Code 2 */
								Field wpsBusy = wifiManagerCls.getField("BUSY");
								/* Code 3 */
								Field wpsOverlapError = wifiManagerCls
										.getField("WPS_OVERLAP_ERROR");
								/* Code 4 */
								Field wpsWepProhibitError = wifiManagerCls
										.getField("WPS_WEP_PROHIBITED");
								/* Code 5 */
								Field wpsTkipOnlyProhibitError = wifiManagerCls
										.getField("WPS_TKIP_ONLY_PROHIBITED");
								/* Code 6 */
								Field wpsAuthFailError = wifiManagerCls
										.getField("WPS_AUTH_FAILURE");
								/* Code 7 */
								Field wpsTimedOutError = wifiManagerCls
										.getField("WPS_TIMED_OUT");
								if (code == wpsError.getInt(null)) {
									Log.i(TAG, "Code 0: Internal Error.");
									Toast.makeText(WifiWPSConnectActivity.this,
											"Code 0: Internal Error.",
											Toast.LENGTH_LONG).show();
								} else if (code == wpsInProgress.getInt(null)) {
									Log.i(TAG,
											"Code 1: WPS is already in progress.");
									Toast.makeText(
											WifiWPSConnectActivity.this,
											"Code 1: WPS is already in progress.",
											Toast.LENGTH_LONG).show();
								} else if (code == wpsBusy.getInt(null)) {
									Log.i(TAG, "Code 2: WPS Busy.");
									Toast.makeText(WifiWPSConnectActivity.this,
											"Code 2: WPS Busy.",
											Toast.LENGTH_LONG).show();
								} else if (code == wpsOverlapError.getInt(null)) {
									Log.i(TAG, "Code 3: WPS Overlap Error.");
									Toast.makeText(WifiWPSConnectActivity.this,
											"Code 3: WPS Overlap Error.",
											Toast.LENGTH_LONG).show();
								}
								// Error of WPS_WEP_PROHIBITED
								else if (code == wpsWepProhibitError
										.getInt(null)) {
									Log.i(TAG,
											"Code 4: WPS Wep Prohibited Error.");
									Toast.makeText(
											WifiWPSConnectActivity.this,
											"Code 4: WPS Wep Prohibited Error.",
											Toast.LENGTH_LONG).show();
								}
								// Error of WPS_TKIP_ONLY_PROHIBITED
								else if (code == wpsTkipOnlyProhibitError
										.getInt(null)) {
									Log.i(TAG,
											"Code 5: WPS TKIP only prohibited Error.");
									Toast.makeText(
											WifiWPSConnectActivity.this,
											"Code 5: WPS TKIP only prohibited Error.",
											Toast.LENGTH_LONG).show();
								}
								// Error of WPS_AUTH_FAILURE
								else if (code == wpsAuthFailError.getInt(null)) {
									Log.i(TAG,
											"Code 6: WPS Auth Failure Error.");
									Toast.makeText(WifiWPSConnectActivity.this,
											"Code 6: WPS Auth Failure Error.",
											Toast.LENGTH_LONG).show();
								}
								// Error of WPS_TIMED_OUT
								else if (code == wpsTimedOutError.getInt(null)) {
									Log.i(TAG, "Code 7: WPS Time out Error.");
									Toast.makeText(WifiWPSConnectActivity.this,
											"Code 7: WPS Time out Error.",
											Toast.LENGTH_LONG).show();
								} else {
									Toast.makeText(
											WifiWPSConnectActivity.this,
											"WPS Connection Failed: Unknow Error"
													+ code, Toast.LENGTH_SHORT)
											.show();
								}
							}
							return null;
						}

					});
			fw = new FileWriter(pinFile, false);
			fw.write("NONE");
			fw.flush();
			fw.close();
			Method startWpsMethod = wifiManagerCls.getMethod(START_WPS_METHOD,
					android.net.wifi.WpsInfo.class, wpsListenerCls);
			startWpsMethod.invoke(this.wifiManager, wpsInfo, wpsListener);

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * To cancel process of WPS
	 *
	 */
	private void wpsCancel() {
		if (this.wifiManager == null)
			return;
		try {
			final Class<?> wifiManagerCls = Class
					.forName(ANDROID_NET_WIFI_WIFIMANAGER_PKG);
			Class<?> actionListenerCls = Class.forName(WPS_ACTION_LISTENER);
			final Method onSuccess = actionListenerCls.getMethod("onSuccess");
			final Method onFailure = actionListenerCls.getMethod("onFailure",
					int.class);

			Object actionListener = Proxy.newProxyInstance(
					actionListenerCls.getClassLoader(),
					new Class<?>[] { actionListenerCls },
					new InvocationHandler() {

						@Override
						public Object invoke(Object proxy, Method method,
								Object[] args) throws Throwable {
							if (method.equals(onSuccess)) {
								Log.i(TAG, "WPS Connection has been canceled");
								Toast.makeText(WifiWPSConnectActivity.this,
										"WPS Connection has been canceled",
										Toast.LENGTH_SHORT).show();
							}
							if (method.equals(onFailure)) {
								int code = (Integer) args[0];
								/* Code 0 */
								Field wpsError = wifiManagerCls
										.getField("ERROR");
								/* Code 1 */
								Field wpsInProgress = wifiManagerCls
										.getField("IN_PROGRESS");
								/* Code 2 */
								Field wpsBusy = wifiManagerCls.getField("BUSY");
								if (code == wpsError.getInt(null)) {
									Log.i(TAG, "Code 0: WPS Internal Error.");
									Toast.makeText(WifiWPSConnectActivity.this,
											"Code 0: WPS Internal Error.",
											Toast.LENGTH_LONG).show();
								} else if (code == wpsInProgress.getInt(null)) {
									Log.i(TAG,
											"Code 1: WPS is already in progress.");
									Toast.makeText(
											WifiWPSConnectActivity.this,
											"Code 1: WPS is already in progress.",
											Toast.LENGTH_LONG).show();
								} else if (code == wpsBusy.getInt(null)) {
									Log.i(TAG, "Code 2: WPS Busy.");
									Toast.makeText(WifiWPSConnectActivity.this,
											"Code 2: WPS Busy.",
											Toast.LENGTH_LONG).show();
								} else {
									Log.i(TAG, "Unknow Error: Code " + code);
									Toast.makeText(WifiWPSConnectActivity.this,
											"Unknow Error: Code " + code,
											Toast.LENGTH_SHORT).show();
								}
							}
							return null;
						}

					});
			Method wpsCancel = wifiManagerCls.getMethod(CANCEL_WPS_METHOD,
					actionListenerCls);
			wpsCancel.invoke(this.wifiManager, actionListener);
			if (timerTask != null)
				timerTask.cancel();
			if (timer != null)
				timer.cancel();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

		}
		if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.wpsconnection, menu);
		menu.clear();
		menu.add(Menu.NONE, Menu.FIRST + 1, 0, "PBC");
		menu.add(Menu.NONE, Menu.FIRST + 2, 0, "PIN Entry");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case Menu.FIRST + 1: {
			Toast.makeText(this, "WPS(PBC) Started.", Toast.LENGTH_SHORT)
					.show();
			wpsConnection("PBC", null, null);
			break;
		}
		case Menu.FIRST + 2: {
			Toast.makeText(this, "WPS(PIN) Started.", Toast.LENGTH_SHORT)
					.show();
			wpsConnection("PIN", null, null);
			break;
		}
		}
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onResume() {
		super.onResume();

	}
}
