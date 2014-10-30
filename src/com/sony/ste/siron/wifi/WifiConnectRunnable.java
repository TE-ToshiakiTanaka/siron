package com.sony.ste.siron.wifi;

import static com.sony.ste.siron.SironService.APP_TAG;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import com.sony.ste.siron.Action;
import com.sony.ste.siron.MessageManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiEnterpriseConfig.Eap;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiConnectRunnable implements Runnable {
	public static final String WIFI_CONNECT_OPEN = "com.sony.ste.siron.WIFI_CONNECT_OPEN";
	public static final String WIFI_CONNECT_WEP = "com.sony.ste.siron.WIFI_CONNECT_WEP";
	public static final String WIFI_CONNECT_PERSONAL = "com.sony.ste.siron.WIFI_CONNECT_PERSONAL";
	public static final String WIFI_CONNECT_ENTERPRISE_EAP_TLS = "com.sony.ste.siron.WIFI_CONNECT_ENTERPRISE_EAP_TLS";
	public static final String WIFI_CONNECT_ENTERPRISE_EAP_PEAP = "com.sony.ste.siron.WIFI_CONNECT_ENTERPRISE_EAP_PEAP";
	public static final String WIFI_CONNECT_ENTERPRISE_EAP_TTLS = "com.sony.ste.siron.WIFI_CONNECT_ENTERPRISE_EAP_TTLS";
	
    private final Intent mOrigIntent;
    private final int mActionId;
    private final Context mContext;
    
	@SuppressLint("SdCardPath")
	private static String userCertPath = "/sdcard/client.p12";
	@SuppressLint("SdCardPath")
	private static String caCertPath = "/sdcard/ca.crt";

	//private final static String METHOD = "method";
	private final static String PHASE = "phase2";

	private final static String SSID = "ssid";
	private final static String IDENTITY = "identity";
	private final static String CERT_PWD = "certpwd";
	private final static String IDENT_PWD = "identpwd";
	private final static String SHARE_KEY = "sharekey";
	private final static String WEP_KEY = "wepkey";
	// 1)aes  2)tkip 3)aestkip
	private final static String INDENT_CIPHER = "cipher"; 
	// 1)1=wpa  2)2=wpa2 3)wpa+wpa2
	private final static String INDENT_WPA_VERSION = "wpa";  
	// 1)64   2)128
	private final static String INDENT_WEP_BIT = "wepbit";
	private WifiManager mWifiManager = null;
	/**
	 * Ca Certification Installation Service
	 * 
	 */
	public WifiConnectRunnable(Context context, Intent intent, int id) {
        mOrigIntent = intent;
        mActionId = id;
        mContext = context;
    }
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		mWifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
		if (mWifiManager != null) {
			Log.i(APP_TAG, "Wi-Fi Manager is successfully initialized.");
			if (!mWifiManager.isWifiEnabled()) {
				mWifiManager.setWifiEnabled(true);
			}
			Log.i(APP_TAG, "Intent : " + mOrigIntent.getAction());
			Action acswitch = Action.getActionFromValue(mOrigIntent.getAction());
			String ssid = mOrigIntent.getStringExtra(SSID);
			switch(acswitch){
				case DO_WIFI_CONNECT_OPEN: 
					if (ssid != null) this.wifiConnectionOpen(ssid);
					break;
				case DO_WIFI_CONNECT_WEP:
					String wepkey = mOrigIntent.getStringExtra(WEP_KEY);
					String wepbit = mOrigIntent.getStringExtra(INDENT_WEP_BIT);
					if(ssid != null && wepkey != null)
						this.wifiConnectionWEP(ssid, wepbit, wepkey);
					break;
				case DO_WIFI_CONNECT_PERSONAL:
					String shareKey = mOrigIntent.getStringExtra(SHARE_KEY);
					String wpa = mOrigIntent.getStringExtra(INDENT_WPA_VERSION);
					String cipher = mOrigIntent.getStringExtra(INDENT_CIPHER);
					if (ssid != null && shareKey != null && wpa != null && cipher != null)
						this.wifiConnectionWPAWPA2(ssid, wpa, cipher, shareKey);
					break;
				case DO_WIFI_CONNECT_ENTERPRISE_EAP_TLS:
					String identity = mOrigIntent.getStringExtra(IDENTITY);
					String certPwd = mOrigIntent.getStringExtra(CERT_PWD);
					Log.i(APP_TAG, identity);
					if (ssid != null && identity != null && certPwd != null)
						wifiConnectionEapTls(ssid, userCertPath, identity, certPwd);
					break;
				case DO_WIFI_CONNECT_ENTERPRISE_EAP_PEAP:
					String identity2 = mOrigIntent.getStringExtra(IDENTITY);
					String password = mOrigIntent.getStringExtra(IDENT_PWD);
					String phase2 = mOrigIntent.getStringExtra(PHASE);
					// EAP-PEAP-MSCHAPV2
					if (phase2 != null && phase2.equals("mschapv2")) {
						wifiConnectionEapPeap(ssid, caCertPath, identity2, password, "mschapv2");
					}
					// EAP-PEAP-MSCHAPV
					if (phase2 != null && phase2.equals("mschap")){
						wifiConnectionEapPeap(ssid, caCertPath, identity2, password, "mschap");
					}
					// EAP-PEAP-GTC
					if (phase2 != null && phase2.equals("gtc")){
						wifiConnectionEapPeap(ssid, caCertPath, identity2, password, "gtc");
					}
					// EAP-PEAP-NONE
					if (phase2 != null && phase2.equals("none")){
						wifiConnectionEapPeap(ssid, caCertPath, identity2, password, "none");
					}
					// EAP-PEAP-PAP
					if (phase2 != null && phase2.equals("pap")){
						wifiConnectionEapPeap(ssid, caCertPath, identity2, password, "pap");
					}
					break;
				case DO_WIFI_CONNECT_ENTERPRISE_EAP_TTLS:
					String identity3 = mOrigIntent.getStringExtra(IDENTITY);
					String password2 = mOrigIntent.getStringExtra(IDENT_PWD);
					String phase22 = mOrigIntent.getStringExtra(PHASE);
					// EAP-TTLS-MSCHAPV2
					if (phase22 != null && phase22.equals("mschapv2")) {
						wifiConnectionEapTtls(ssid, caCertPath, identity3, password2, "mschapv2");
					}
					// EAP-TTLS-MSCHAPV
					if (phase22 != null && phase22.equals("mschap")){
						wifiConnectionEapTtls(ssid, caCertPath, identity3, password2, "mschap");
					}
					// EAP-TTLS-GTC
					if (phase22 != null && phase22.equals("gtc")){
						wifiConnectionEapTtls(ssid, caCertPath, identity3, password2, "gtc");
					}
					// EAP-TTLS-NONE
					if (phase22 != null && phase22.equals("none")){
						wifiConnectionEapTtls(ssid, caCertPath, identity3, password2, "none");
					}
					// EAP-TTLS-PAP
					if (phase22 != null && phase22.equals("pap")){
						wifiConnectionEapTtls(ssid, caCertPath, identity3, password2, "pap");
					}
					break;
				default:
					Log.i(APP_TAG, "Not Implemented.");
					break;
			}
		} else Log.e(APP_TAG, "Wi-Fi Manager fail to be initialized.");
		MessageManager.getInstance().sendMessage(mActionId ,true, mOrigIntent);
	}
	/**
	 * Wifi Connection by Open
	 * 
	 * @param ssid
	 */
	private void wifiConnectionOpen(String ssid) {
		WifiConfiguration wc = new WifiConfiguration();
		wc.SSID = "\"" + ssid + "\"";
		wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		int netID = mWifiManager.addNetwork(wc);
		boolean flag = mWifiManager.enableNetwork(netID, true);
		if (flag) Log.i(APP_TAG, "Network " + netID + " is enabled");
		else  Log.i(APP_TAG, "Network " + netID + " is NOT enabled");
		mWifiManager.startScan();
	}
	/**
	 * WEP Connection
	 * 
	 * Only WEP-64 & WEP 128 Supported
	 * 
	 * @param ssid
	 * @param wepbit
	 * @param wepkey
	 */
	private void wifiConnectionWEP(String ssid, String wepbit, String wepkey){
		WifiConfiguration wc = new WifiConfiguration();
		Log.i(APP_TAG, "WEP Key: " + wepkey);
		wc.SSID = "\"" + ssid + "\"";
	    wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
	    wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN); 
	    wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
	    wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
	    wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
	    wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
	    wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
	    wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
	    wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
		wc.wepKeys[0] = wepkey;
		wc.wepTxKeyIndex = 0;
		int netID = mWifiManager.addNetwork(wc);
		boolean flag = mWifiManager.enableNetwork(netID, true);
		if (flag) {
			Log.i(APP_TAG, "Network " + netID + " is enabled");
		} else {
			Log.i(APP_TAG, "Network " + netID + " is NOT enabled");
		}
		mWifiManager.startScan();
	}
	/**
	 * Wifi Connection by EAP-TLS
	 * 
	 * @param ssid
	 * @param certPath
	 * @param userId
	 */
	@SuppressLint("NewApi")
	private void wifiConnectionEapTls(String ssid, String certPath,
			String userId, String certPwd) {

		WifiConfiguration wc = new WifiConfiguration();
		WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();

		wc.allowedAuthAlgorithms.clear();
		wc.allowedGroupCiphers.clear();
		wc.allowedPairwiseCiphers.clear();
		wc.allowedProtocols.clear();
		wc.allowedKeyManagement.clear();

		wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
		wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
		wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
		wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);

		wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
		wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);

		wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN); // For WPA2
		wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA); // For WPA

		wc.SSID = "\"" + ssid + "\"";
		wc.allowedKeyManagement.set(KeyMgmt.WPA_EAP);
		wc.allowedKeyManagement.set(KeyMgmt.IEEE8021X);
		wc.status = WifiConfiguration.Status.ENABLED;

		enterpriseConfig.setEapMethod(Eap.TLS);
		enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.NONE);
		enterpriseConfig.setPassword(null);
		enterpriseConfig.setAnonymousIdentity(null);
		enterpriseConfig.setSubjectMatch(null);

		KeyStore pkcs12ks = null;
		try {
			pkcs12ks = KeyStore.getInstance("pkcs12");
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}
		InputStream in = null;
		try {
			in = new BufferedInputStream(
					new FileInputStream(new File(certPath)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		Enumeration<String> aliases = null;
		try {
			pkcs12ks.load(in, certPwd.toCharArray());
			aliases = pkcs12ks.aliases();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		while (aliases.hasMoreElements()) {
			String alias = aliases.nextElement();
			Log.d(APP_TAG, "Processing alias " + alias);
			X509Certificate cert;
			try {
				cert = (X509Certificate) pkcs12ks.getCertificate(alias);
				Log.d(APP_TAG, cert.toString());
				PrivateKey key = (PrivateKey) pkcs12ks.getKey(alias,
						certPwd.toCharArray());
				Log.d(APP_TAG, key.toString());
				enterpriseConfig.setClientKeyEntry(key, cert);
				enterpriseConfig.setIdentity(userId);
			} catch (KeyStoreException e) {
				e.printStackTrace();
			} catch (UnrecoverableKeyException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}
		wc.enterpriseConfig = enterpriseConfig;

		int netID = mWifiManager.addNetwork(wc);
		// wifiManager.saveConfiguration();
		boolean flag = mWifiManager.enableNetwork(netID, true);
		if (flag) {
			Log.i(APP_TAG, "Network " + netID + " is enabled");
		} else {
			Log.i(APP_TAG, "Network " + netID + " is NOT enabled");
		}
		mWifiManager.startScan();
	}

	@SuppressLint("NewApi")
	private void wifiConnectionEapTtls(String ssid, String caCertPath, String identity, String password, String phase2){
		WifiConfiguration wc = new WifiConfiguration();
		WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();

		wc.allowedAuthAlgorithms.clear();
		wc.allowedGroupCiphers.clear();
		wc.allowedPairwiseCiphers.clear();
		wc.allowedProtocols.clear();
		wc.allowedKeyManagement.clear();

		wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
		wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
		wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
		wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);

		wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
		wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);

		wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN); // For WPA2
		wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA); // For WPA

		wc.SSID = "\"" + ssid + "\"";
		wc.allowedKeyManagement.set(KeyMgmt.WPA_EAP);
		wc.allowedKeyManagement.set(KeyMgmt.IEEE8021X);
		wc.status = WifiConfiguration.Status.ENABLED;

		enterpriseConfig.setEapMethod(Eap.TTLS);
		if(phase2.equals("mschapv2"))
			enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.MSCHAPV2);
		else if(phase2.equals("gtc"))
			enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.GTC);
		else if(phase2.equals("mschap"))
			enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.MSCHAP);
		else if(phase2.equals("none"))
			enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.NONE);
		else if(phase2.equals("pap"))
			enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.PAP);
		else
			enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.NONE);
		enterpriseConfig.setIdentity(identity);
		enterpriseConfig.setPassword(password);
		enterpriseConfig.setAnonymousIdentity(null);
		enterpriseConfig.setSubjectMatch(null);

		File certFile = new File(caCertPath);
		FileInputStream fin;
		X509Certificate x509Cert = null;
		try {
			fin = new FileInputStream(certFile);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			x509Cert = (X509Certificate) cf.generateCertificate(fin);
			Log.i(APP_TAG, x509Cert.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		}
		enterpriseConfig.setCaCertificate(x509Cert);
		wc.enterpriseConfig = enterpriseConfig;

		int netID = mWifiManager.addNetwork(wc);
		// wifiManager.saveConfiguration();
		boolean flag = mWifiManager.enableNetwork(netID, true);
		if (flag) {
			Log.i(APP_TAG, "Network " + netID + " is enabled");
		} else {
			Log.i(APP_TAG, "Network " + netID + " is NOT enabled");
		}
		mWifiManager.startScan();
	}
	
	/**
	 * WiFi Connection by EAP-PEAP
	 * 
	 * @param ssid
	 * @param caCertPath
	 * @param identity
	 * @param password
	 * @param phase2
	 */
	@SuppressLint("NewApi")
	private void wifiConnectionEapPeap(String ssid, String caCertPath,
			String identity, String password, String phase2) {
		WifiConfiguration wc = new WifiConfiguration();
		WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();

		wc.allowedAuthAlgorithms.clear();
		wc.allowedGroupCiphers.clear();
		wc.allowedPairwiseCiphers.clear();
		wc.allowedProtocols.clear();
		wc.allowedKeyManagement.clear();

		wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
		wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
		wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
		wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);

		wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
		wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);

		wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN); // For WPA2
		wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA); // For WPA

		wc.SSID = "\"" + ssid + "\"";
		wc.allowedKeyManagement.set(KeyMgmt.WPA_EAP);
		wc.allowedKeyManagement.set(KeyMgmt.IEEE8021X);
		wc.status = WifiConfiguration.Status.ENABLED;

		enterpriseConfig.setEapMethod(Eap.PEAP);
		if(phase2.equals("mschapv2"))
			enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.MSCHAPV2);
		else if(phase2.equals("gtc"))
			enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.GTC);
		else if(phase2.equals("mschap"))
			enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.MSCHAP);
		else if(phase2.equals("none"))
			enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.NONE);
		else if(phase2.equals("pap"))
			enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.PAP);
		else
			enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.NONE);
		enterpriseConfig.setIdentity(identity);
		enterpriseConfig.setPassword(password);
		enterpriseConfig.setAnonymousIdentity(null);
		enterpriseConfig.setSubjectMatch(null);

		File certFile = new File(caCertPath);
		FileInputStream fin;
		X509Certificate x509Cert = null;
		try {
			fin = new FileInputStream(certFile);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			x509Cert = (X509Certificate) cf.generateCertificate(fin);
			Log.i(APP_TAG, x509Cert.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		}
		enterpriseConfig.setCaCertificate(x509Cert);
		wc.enterpriseConfig = enterpriseConfig;

		int netID = mWifiManager.addNetwork(wc);
		// wifiManager.saveConfiguration();
		boolean flag = mWifiManager.enableNetwork(netID, true);
		if (flag) {
			Log.i(APP_TAG, "Network " + netID + " is enabled");
		} else {
			Log.i(APP_TAG, "Network " + netID + " is NOT enabled");
		}
		mWifiManager.startScan();
	}
	
	/**
	 * 
	 * Wifi Connection by WPA/WPA2
	 * 
	 * @param ssid
	 * @param shareKey
	 */
	private void wifiConnectionWPAWPA2(String ssid, String wpa, String cipher, String shareKey) {

		WifiConfiguration wc = new WifiConfiguration();
		wc.SSID = "\"" + ssid + "\"";
		if(shareKey.length() == 64) wc.preSharedKey = shareKey;
		else wc.preSharedKey = "\"" + shareKey + "\"";
		// Authentication Algorithm
		wc.allowedAuthAlgorithms.clear();
		wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
		// Group Ciphers & Pairwise Ciphers
		wc.allowedGroupCiphers.clear();
		wc.allowedPairwiseCiphers.clear();
		if(cipher.equals("aes")){
			wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
		}
		if(cipher.equals("tkip")){
			wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
		}
		if(cipher.equals("aestkip")){
			wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
			wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
		}
		// Key Management
		wc.allowedKeyManagement.clear();
		wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
		
		// Protocols
		wc.allowedProtocols.clear();
		if(wpa.equals("1")){
			wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
		}
		if(wpa.equals("2")){
			wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
		}
		if(wpa.equals("3")){
			wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
			wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
		}
		int netID = mWifiManager.addNetwork(wc);
		boolean flag = mWifiManager.enableNetwork(netID, true);
		if (flag) {
			Log.i(APP_TAG, "Network " + netID + " is enabled");
		} else {
			Log.i(APP_TAG, "Network " + netID + " is NOT enabled");
		}
		mWifiManager.startScan();
	}
}
