package com.ucas.chat.tor.message;

import java.net.Socket;
import java.util.Arrays;

public class MessageItem {

	private String onionName;
	/**
	 * recieve data from this socket
	 */
	private Socket socket;
	/**
	 * external pay load
	 */
	private byte[] data;


	private String rawMessage;

	/**
	 * handshake message 0 or data message 1
	 */
	private int type;
	/**
	 * socket purpose :file 1 or text 0
	 */
	private int purpose=0;

	private HandShakeMessage handShakeMessage=null;

	private int pieceID=0;

	public int getPieceID() {
		return pieceID;
	}

	public void setPieceID(int pieceID) {
		this.pieceID = pieceID;
	}

	public MessageItem(String onionName, Socket socket, byte[] data, int type, int purpose,
					   HandShakeMessage handShakeMessage) {
		super();
		this.onionName = onionName;
		this.socket = socket;
		this.data = data;
		this.type = type;
		this.purpose = purpose;
		this.handShakeMessage = handShakeMessage;
	}
	public Socket getSocket() {
		return socket;
	}
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}

	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getOnionName() {
		return onionName;
	}
	public void setOnionName(String onionName) {
		this.onionName = onionName;
	}

	public int getPurpose() {
		return purpose;
	}
	public void setPurpose(int purpose) {
		this.purpose = purpose;
	}
	@Override
	public String toString() {
		return "MessageItem [onionName=" + onionName + ", socket=" + socket + ", data=" + Arrays.toString(data)
				+ ", type=" + type + ", purpose=" + purpose + "]";
	}
	public HandShakeMessage getHandShakeMessage() {
		return handShakeMessage;
	}
	public void setHandShakeMessage(HandShakeMessage handShakeMessage) {
		this.handShakeMessage = handShakeMessage;
	}

	public String getRawMessage() {
		return rawMessage;
	}

	public void setRawMessage(String rawMessage) {
		this.rawMessage = rawMessage;
	}
}
