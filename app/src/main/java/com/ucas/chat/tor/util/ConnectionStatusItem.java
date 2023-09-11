package com.ucas.chat.tor.util;

import java.net.Socket;

public class ConnectionStatusItem {
	/**
	 * remote onion host name
	 */
	private String onionName;
	/**
	 * which remote onion
	 */
	private byte[] onionHash;
	/**
	 * socket handler
	 */
	private Socket socket;
	/**
	 * handshake or data phrase
	 */
	private int status;
	/**
	 * connection shared key
	 */
	private byte[] shareKey;
	
	private int purpose;
	
	public byte[] getOnionHash() {
		return onionHash;
	}
	public void setOnionHash(byte[] onionHash) {
		this.onionHash = onionHash;
	}
	public Socket getSocket() {
		return socket;
	}
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public byte[] getShareKey() {
		return shareKey;
	}
	public void setShareKey(byte[] shareKey) {
		this.shareKey = shareKey;
	}
	
	
	public int getPurpose() {
		return purpose;
	}
	public void setPurpose(int purpose) {
		this.purpose = purpose;
	}
	public String getOnionName() {
		return onionName;
	}
	public void setOnionName(String onionName) {
		this.onionName = onionName;
	}
	public ConnectionStatusItem(String onionName,byte[] onionHash, Socket socket, int status, byte[] shareKey) {
		super();
		this.onionName=onionName;
		this.onionHash = onionHash;
		this.socket = socket;
		this.status = status;
		this.shareKey = shareKey;
		this.purpose=Constant.SOCKET_PURPOSE_TEXT;
	}
	@Override
	public String toString() {
		if(this.shareKey!=null)
		return "ConnectionStatusItem  "+", socket=" + socket + ", status=" + status + ", shareKey=" + AESCrypto.bytesToHex(shareKey) + ", purpose="
				+ purpose + "]";
		else
			return "ConnectionStatusItem  "+ ", socket=" + socket + ", status=" + status + ", shareKey=" + null + ", purpose="
			+ purpose + "]";
	}
	
	
	

}
