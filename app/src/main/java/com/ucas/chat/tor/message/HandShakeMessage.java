package com.ucas.chat.tor.message;

import android.content.Context;
import android.util.Log;

import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.jni.JniEntryUtils;
import com.ucas.chat.tor.util.AESCrypto;
import com.ucas.chat.tor.util.Constant;
import com.ucas.chat.tor.util.RSACrypto;
import com.ucas.chat.tor.util.RecordXOR;
import com.ucas.chat.tor.util.XORutil;
import com.ucas.chat.utils.RandomUtil;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apaches.commons.codec.digest.DigestUtils;



public class HandShakeMessage extends Message {
	private static final String TAG = ConstantValue.TAG_CHAT + "HandShakeMessage";
	private String remoteOnion;
	private String localPrivateKey;
	private String remotePublickey;

	
	public String getRemoteOnion() {
		return remoteOnion;
	}

	public String getLocalPrivateKey() {
		return localPrivateKey;
	}

	public String getRemotePublickey() {
		return remotePublickey;
	}

	public HandShakeMessage(String onion, String localPrivateKey, String remotePublickey) {
		super();
		this.remoteOnion = onion;
		this.localPrivateKey = localPrivateKey;
		this.remotePublickey = remotePublickey;
	}

	public byte[] createSessionRequestMessage(Context context) {//创建会话请求消息的握手包，第一次握手
		byte[] payload = null;
		int interanlPayloadLength = 0;
		byte messageType = 1;//类型1

		String messageID = RandomUtil.randomChar(); // TODO: 2021/8/24 更新消息id标记，用这个来唯一标记当前次的发送情况
		byte[] externalPayload = Message.createMessageHeader(interanlPayloadLength, messageType, messageID);//组合成application-id(byte)	时间戳(byte)	message-number(byte)	message-type(byte)	payload-length(byte)

		byte[] externalPayloadHash = AESCrypto.digest_fast(externalPayload);
//		System.out.println(externalPayload.length);
//		System.out.println(externalPayloadHash.length);
		String localOnion = Constant.MY_ONION_HOSTNAME;
		byte[] header = Message.byteMerger(externalPayload, externalPayloadHash);//组合成application-id(byte)	时间戳(byte)	message-number(byte)	message-type(byte)	payload-length(byte)	external-hash(byte)
		byte[] myOnionHash = DigestUtils.sha256(localOnion.getBytes());
//		System.out.println(new String(myOnionHash));
//		System.out.println("HandShakeMessage.createSessionRequestMessage.myOnionHash :\n" + RSACrypto.bytesToHex(myOnionHash));

		payload = AESCrypto.paddingToLength(header, Constant.EXTERNAL_HANDSHAKE_LENGTH);//扩到1790字节，需要的剩余字节
//		byte[] tail = Message.subBytes(payload, 32, 1725);//从第32位开始取，因为上面的header未组合myOnionHash
		byte[] tail = Message.subBytes(payload, 38, 1719);// TODO: 2021/10/4  从第38位开始取，因为上面的header未组合myOnionHash，startFileNameAndIndex

		payload = Message.byteMerger(header, myOnionHash);//application-id(byte)	时间戳(byte)	message-number(byte)	message-type(byte)	payload-length(byte)	external-hash(byte)		onion_hash(byte)

		RecordXOR recordStartXOR = XORutil.getStartXOR(context);// TODO: 2021/10/3  开始要用的xor文件信息

		int fileIndex = JniEntryUtils.getKeyIndex();
		fileIndex++;
		Log.d(TAG, " 测试 createSessionRequestMessage:: fileIndex = " + fileIndex);
		byte[] startFileNameAndIndex = XORutil.xorFile2Byte(1, fileIndex);//按设计的大小合并文件名和位置

		payload = Message.byteMerger(payload,startFileNameAndIndex);// TODO: 2021/10/4 加上startFileNameAndIndex

		payload = Message.byteMerger(payload, tail);//上面的+任意填充

//		System.out.println(payload.length);
		System.out.println(TAG + " createSessionRequestMessage:: payload :\n" + RSACrypto.bytesToHex(payload));
		byte[] finalPayload = null;
		try {
			finalPayload = Message.build(payload, this.localPrivateKey, this.remotePublickey);//获取加密后的握手包
		} catch (Exception e) {
			e.printStackTrace();
		}
		return finalPayload;
	}

	private byte[] parseSessionRequestPaylod(byte[] interalPayload) {
		byte[] payload = null;

		return payload;
	}

	public byte[] createSessionAuthMessage(String password,byte[] startFileNameAndIndex ) {// TODO: 2021/10/4 增加xor文件的字段信息 //创建会话身份验证消息
		ByteBuffer data = ByteBuffer.allocate(30);
		byte[] passwd = new byte[0];
		byte[] passwordLength = new byte[1];
		try {
			passwd = password.getBytes("utf-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(TAG + " createSessionAuthMessage:: passwd:" + Arrays.toString(passwd));
		data.putInt(passwd.length);
		data.position(3);
		data.get(passwordLength);
		data.clear();
		byte[] session_auth_internal_bytes = Message.byteMerger(passwordLength, passwd);
		System.out.println(TAG + " createSessionAuthMessage::  session_auth_internal_bytes:" + Arrays.toString(session_auth_internal_bytes));
		byte messageType = 2;//类型2

		String messageID = RandomUtil.randomChar(); // TODO: 2021/8/24 更新消息id标记，用这个来唯一标记当前次的发送情况
		byte[] externalPayload = Message.createMessageHeader(session_auth_internal_bytes.length, messageType, messageID);//组合成application-id(byte)	时间戳(byte)	message-number(byte)	message-type(byte)	payload-length(byte)

		externalPayload = Message.byteMerger(externalPayload, session_auth_internal_bytes);//组合成application-id(byte)	时间戳(byte)	message-number(byte)	message-type(byte)	payload-length(byte)	internal-payload
		byte[] externalPayloadHash = AESCrypto.digest_fast(externalPayload);
//		System.out.println(externalPayload.length);
//		System.out.println(externalPayloadHash.length);
		byte[] header = Message.byteMerger(externalPayload, externalPayloadHash);//组合成application-id(byte)	时间戳(byte)	message-number(byte)	message-type(byte)	payload-length(byte)	internal-payload		external-hash(byte)

		header = Message.byteMerger(header,startFileNameAndIndex);// TODO: 2021/10/4 增加xor的字段信息 ，字段

		byte[] payload = AESCrypto.paddingToLength(header, Constant.EXTERNAL_HANDSHAKE_LENGTH);//扩到1790字节，需要的剩余字节
		payload = Message.byteMerger(header, payload);//application-id(byte)	时间戳(byte)	message-number(byte)	message-type(byte)	payload-length(byte)	internal-payload		external-hash(byte)			任意填充

//		System.out.println(payload.length);
		System.out.println(TAG + " createSessionAuthMessage::  payload :\n" + RSACrypto.bytesToHex(payload));
		byte[] finalPayload = null;
		try {
			finalPayload = Message.build(payload, this.localPrivateKey, this.remotePublickey);//获取加密后的握手包
		} catch (Exception e) {
			e.printStackTrace();
		}
		return finalPayload;

	}

	/**
	 * 
	 * @param  internalPayload
	 * @return
	 */
	public static boolean parseSessionAuthPaylod(byte[] internalPayload) {//解析会话身份验证Paylod
		byte[] bytePasswordLength = Message.subBytes(internalPayload, 0, 1);
		int passwordLength = Integer.parseInt(AESCrypto.bytesToHex(bytePasswordLength), 16);
		System.out.println(TAG + " parseSessionAuthPaylod:: passwordLength:" + passwordLength);
		String password = "";
		try {
			password = new String(Message.subBytes(internalPayload, 1, passwordLength), "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(TAG + " parseSessionAuthPaylod:: password:" + password);
		if (password.equals(Constant.PASSWORD))
			return true;
		else
			return false;

	}

	public byte[] createSessionExchangeMessage(String myPassword, byte[] sharedKey) {//创建会话交换消息

		byte[] payload = null;

		ByteBuffer data = ByteBuffer.allocate(30);
		byte[] passwd = new byte[0];
		byte[] passwordLength = new byte[1];
		try {
			passwd = myPassword.getBytes("utf-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(TAG + " createSessionExchangeMessage:: passwd:" + Arrays.toString(passwd));
		data.putInt(passwd.length);
		data.position(3);
		data.get(passwordLength);
		data.clear();
		byte[] temp = Message.byteMerger(passwordLength, passwd);
		byte[] session_exchange_internal_bytes = Message.byteMerger(temp, sharedKey);

		System.out.println(TAG + " createSessionExchangeMessage:: session_exchange_internal_bytes:"
				+ Arrays.toString(session_exchange_internal_bytes));

		int interanlPayloadLength = session_exchange_internal_bytes.length;
		byte messageType = 3;//类型3

		String messageID = RandomUtil.randomChar(); // TODO: 2021/8/24 更新消息id标记，用这个来唯一标记当前次的发送情况
		byte[] externalPayload = Message.createMessageHeader(interanlPayloadLength, messageType, messageID);

		externalPayload = Message.byteMerger(externalPayload, session_exchange_internal_bytes);
		byte[] externalPayloadHash = AESCrypto.digest_fast(externalPayload);
//		System.out.println(externalPayload.length);
//		System.out.println(externalPayloadHash.length);

		byte[] header = Message.byteMerger(externalPayload, externalPayloadHash);
//		System.out.println(header.length);
//		System.out.println("header :\n" + RSACrypto.bytesToHex(header));
		payload = AESCrypto.paddingToLength(header, Constant.EXTERNAL_HANDSHAKE_LENGTH);
		payload = Message.byteMerger(header, payload);
//		System.out.println(payload.length);
		System.out.println(TAG + " createSessionExchangeMessage:: payload :\n" + RSACrypto.bytesToHex(payload));
		byte[] finalPayload = null;
		try {
			finalPayload = Message.build(payload, this.localPrivateKey, this.remotePublickey);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return finalPayload;
	}

	public static byte[] parseSessionExchangePaylod(byte[] internalPayload) {//解析会话交换Paylod
		byte[] payload = null;
		byte[] bytePasswordLength = Message.subBytes(internalPayload, 0, 1);
		int passwordLength = Integer.parseInt(AESCrypto.bytesToHex(bytePasswordLength), 16);
		String password="";
		try {
			password = new String(Message.subBytes(internalPayload, 1, passwordLength), "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(TAG + " parseSessionExchangePaylod:: password:" + password);
		byte[] sharedKey = Message.subBytes(internalPayload, passwordLength + 1, Constant.SHAREDKEY_LENGTH);
		return sharedKey;

	}

	public byte[] createSessionDoneMessage() {//创建会话完成消息
		byte[] payload = null;
		int interanlPayloadLength = 0;
		byte messageType = 4;//类型4

		String messageID = RandomUtil.randomChar(); // TODO: 2021/8/24 更新消息id标记，用这个来唯一标记当前次的发送情况
		byte[] externalPayload = Message.createMessageHeader(interanlPayloadLength, messageType, messageID);

		byte[] externalPayloadHash = AESCrypto.digest_fast(externalPayload);
//		System.out.println(externalPayload.length);
//		System.out.println(externalPayloadHash.length);

		byte[] header = Message.byteMerger(externalPayload, externalPayloadHash);
		payload = AESCrypto.paddingToLength(header, Constant.EXTERNAL_HANDSHAKE_LENGTH);
		payload = Message.byteMerger(header, payload);
		
//		System.out.println(payload.length);
		System.out.println(TAG + " createSessionDoneMessage:: payload :\n" + RSACrypto.bytesToHex(payload));
		byte[] finalPayload = null;
		try {
			finalPayload = Message.build(payload, this.localPrivateKey, this.remotePublickey);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return finalPayload;
	}

	private byte[] parseSessionDonePaylod() {
		byte[] payload = null;

		return payload;
	}

}
