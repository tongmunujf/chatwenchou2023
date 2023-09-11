package com.ucas.chat.tor.message;

public class ACKMessage {
	
	private int messageType;
	private int recievePieceNumber;
	private String ackok;
	private byte[] messageHash;
	public ACKMessage(int messageType, int recievePieceNumber, String ackok, byte[] messageHash) {
		super();
		this.messageType = messageType;
		this.recievePieceNumber = recievePieceNumber;
		this.ackok = ackok;
		this.messageHash = messageHash;
	}
	public int getMessageType() {
		return messageType;
	}
	public int getRecievePieceNumber() {
		return recievePieceNumber;
	}
	public String getAckok() {
		return ackok;
	}
	public byte[] getMessageHash() {
		return messageHash;
	}
	
	
	
	
	

}
