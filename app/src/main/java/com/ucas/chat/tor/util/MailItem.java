package com.ucas.chat.tor.util;

import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.utils.LogUtils;

import org.apaches.commons.codec.digest.DigestUtils;

/**
 * contact item
 * @author happywindy
 *
 */
public class MailItem {
	public static final String TAG = ConstantValue.TAG_CHAT + "MailItem";
	private String onionName;
	private byte[] onionHash;
	private int port;
	private String publicKey;
	public MailItem(String onionName, int port, String publicKey) {
		super();
		this.onionName = onionName;
		LogUtils.d(TAG, " this.onionName: " + onionName);
		this.onionHash = DigestUtils.sha256(this.onionName.getBytes());
		this.port = port;
		this.publicKey = publicKey;
	}
	public String getOnionName() {
		return onionName;
	}
	public byte[] getOnionHash() {
		return onionHash;
	}
	public int getPort() {
		return port;
	}
	public String getPublicKey() {
		return publicKey;
	}
	@Override
	public String toString() {
		return "MailItem [onionName=" + onionName +  ", port=" + port+ "]";
	}
	
	
	
	

}
