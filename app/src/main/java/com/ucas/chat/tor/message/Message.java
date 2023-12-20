package com.ucas.chat.tor.message;

import android.util.Log;

import com.ucas.chat.tor.util.AESCrypto;
import com.ucas.chat.tor.util.Constant;
import com.ucas.chat.tor.util.RSACrypto;
import com.ucas.chat.tor.util.RecordXOR;
import com.ucas.chat.tor.util.SecureRandomUtil;
import com.ucas.chat.tor.util.XORutil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public abstract class Message {

	public static byte[] byteMerger(byte[] bt1, byte[] bt2) {//合并byte数组
		byte[] bt3 = new byte[bt1.length + bt2.length];
		System.arraycopy(bt1, 0, bt3, 0, bt1.length);
		System.arraycopy(bt2, 0, bt3, bt1.length, bt2.length);
		return bt3;
	}
	
	/**
	 * 2+4+4+1+2=13bytes for both handshake message and data message
	 * 
	 * @param payloadlength internalPayload length
	 * @param messageType
	 * @return
	 */
	public static byte[] createMessageHeader(int payloadlength, byte messageType,String messageID) {// TODO: 2021/8/24 更新消息id标记，用这个来唯一标记当前次的发送情况
		ByteBuffer data = ByteBuffer.allocate(30);//https://blog.csdn.net/mrliuzhao/article/details/89453082
		byte[] byteApplicationId = Constant.APPLICATION_ID;
		byte[] byteUtcTimestamp = new byte[Constant.BYTE_UTC_TIMESTAMP];//时间
		byte[] byteMessageNo = new byte[Constant.BYTE_MESSAGE_NO];//消息的id
		byte[] byteMessageType = new byte[Constant.BYTE_MESSAGE_TYPE];
		byte[] bytePayloadLength = new byte[Constant.BYTE_PAYLOAD_LENGTH];

//	        ʱ���
		long utcTimestamp = System.currentTimeMillis() / 1000;
//		System.out.println("message.ExternalPayload.pack.utc_timestamp:" + utcTimestamp);
		data.order(ByteOrder.BIG_ENDIAN);//字节序(Byte Order)之大端 https://blog.csdn.net/Batac_Lee/article/details/106458515
		data.putLong(utcTimestamp);
		data.position(4);
		data.get(byteUtcTimestamp);
		data.flip();//不仅将position复位为0，同时也将limit的位置放置在了position之前所在的位置上

//	        application_id
		byte[] b3 = Message.byteMerger(byteApplicationId, byteUtcTimestamp);
		data.clear();

		int intmessageID = Integer.parseInt(messageID);
//		data.order(ByteOrder.BIG_ENDIAN);//字节序(Byte Order)之大端 https://blog.csdn.net/Batac_Lee/article/details/106458515
		data.putInt(intmessageID);;
		data.flip();//不仅将position复位为0，同时也将limit的位置放置在了position之前所在的位置上
		data.get(byteMessageNo);


		byte[] b4 = Message.byteMerger(b3, byteMessageNo);
		String stringB4 = new String(b4);

//	        messageType
		data.clear();
//		messageType=1;
		data.put(messageType);
		data.flip();
		data.get(byteMessageType);
//		System.out.println(RSACrypto.bytesToHex(byteMessageType));

		byte[] b5 = Message.byteMerger(b4, byteMessageType);

		data.clear();
		data.putInt(payloadlength);
		data.position(2);
		data.get(bytePayloadLength);

		byte[] b6 = Message.byteMerger(b5, bytePayloadLength);

//	        internalPayload

		return b6;
	}

	/**
	 * create data message external payload :file \picture\text message
	 * @param messageType
	 * @param internalPayload
	 * @return
	 * // TODO: 2021/10/5 增加xor文件的使用信息
	 */
	public static byte[] createDataMessageExternalPaylod(byte messageType, byte[] internalPayload,String messageID,RecordXOR recordXOR) {
		byte[] payload = null;
//		System.out.println("Message.createDataMessageExternalPaylod");

		byte[] externalPayload = Message.createMessageHeader(internalPayload.length, messageType, messageID);// TODO: 2021/8/24 //头

		externalPayload = Message.byteMerger(externalPayload, internalPayload);//头+文本
		byte[] externalPayloadHash = AESCrypto.digest_fast(externalPayload);//加密
//		System.out.println(externalPayload.length);
//		System.out.println(externalPayloadHash.length);

		byte[] header = Message.byteMerger(externalPayload, externalPayloadHash);
//		System.out.println(header.length);
//		System.out.println("Message.createDataMessageExternalPaylod.header :\n" + RSACrypto.bytesToHex(header));

// TODO: 2021/10/5 增加xor文件的使用信息
		byte[] startFileNameAndIndex = XORutil.xorFile2Byte(recordXOR.getStartFileName(),recordXOR.getStartFileIndex());//按设计的大小合并文件名和位置
		byte[] endFileNameAndIndex = XORutil.xorFile2Byte(recordXOR.getEndFileName(),recordXOR.getEndFileIndex());//按设计的大小合并文件名和位置

		header = Message.byteMerger(header,startFileNameAndIndex);//application-id(byte)	时间戳(byte)	message-number(byte)	message-type(byte)	payload-length(byte)	internal-payload		external-hash(byte)	startXORFileName(byte)	startXORIndex(byte)
		header = Message.byteMerger(header,endFileNameAndIndex);//message-number(byte)	message-type(byte)	payload-length(byte)	internal-payload		external-hash(byte)	startXORFileName(byte)	startXORIndex(byte)	endXORFileName(byte)	endXORIndex(byte)

		payload = AESCrypto.paddingToLength(header, Constant.EXTERNAL_DATA_LEGTH);//凑够长度的剩余部分
		payload = Message.byteMerger(header, payload);
//		System.out.println("Message.createDataMessageExternalPaylod.payloadlength"+payload.length);
//		System.out.println("Message.createDataMessageExternalPaylod.payload :\n" + RSACrypto.bytesToHex(payload));

		return payload;
	}

	/**
	 * extract external payload for both handshake and data message,return internal payload
	 * @param externalPayload
	 * @return
	 */
	public static byte[] parseExternalPaylod(byte[] externalPayload) {
		int payloadLength = 0;

		byte[] bytePayloadLength = Message.subBytes(externalPayload, Constant.BYTE_PAYLOAD_LENGTH_BEGIN,
				Constant.BYTE_PAYLOAD_LENGTH);
		payloadLength = Integer.parseInt(AESCrypto.bytesToHex(bytePayloadLength), 16);
//		System.out.println("message.ExternalPayload.parse.payloadLength :" + payloadLength);

		//没解密具体内容
		byte[] internalPayload = Message.subBytes(externalPayload, Constant.BYTE_internalPayload_BEGIN, payloadLength);
//		System.out.println("message.ExternalPayload.parse.internalPayload :" + Arrays.toString(internalPayload));

		byte[] cell14 = Message.subBytes(externalPayload, 0, Constant.BYTE_internalPayload_BEGIN + payloadLength);
//		System.out.println("�������ǰ����ֽڣ�" + Arrays.toString(cell14));
		byte[] new_hash = AESCrypto.digest_fast(cell14);
//		System.out.println("�������ǰ����ֽڵĹ�ϣ��" + Arrays.toString(new_hash));
		byte[] primitiveHash = Message.subBytes(externalPayload, Constant.BYTE_internalPayload_BEGIN + payloadLength,
				20);
//		System.out.println("���ﱾ��Ĺ�ϣ��" + Arrays.toString(primitiveHash));
		if (Arrays.equals(new_hash, primitiveHash)) {//判断是否发、收的内容一样
			System.out.println("��ϣ��ͬ");
		} else {
			System.out.println("��ϣ��ͬ����Ϣ���۸�");
		}

		return internalPayload;
	}




	public static byte[] parseFirstHandShakeStartXORFileName(byte[] externalPayload){// TODO: 2021/10/4  对方发来要我确认的第一次握手的开始xor文件名

		int begin = Constant.BYTE_internalPayload_BEGIN + Constant.BYTE_EXTERNAL_HASH + Constant.BYTE_ONION_HASH;
		byte[] startXORFileName = Message.subBytes(externalPayload,begin,Constant.BYTE_STARTXORFILENAME_LENGTH );

		return startXORFileName;
	}



	public static byte[] parseFirstHandShakeStartXORIndex(byte[] externalPayload){// TODO: 2021/10/4  对方发来要我确认的第一次握手的开始xor文件的开始位置

		int begin = Constant.BYTE_internalPayload_BEGIN + Constant.BYTE_EXTERNAL_HASH + Constant.BYTE_ONION_HASH +Constant.BYTE_STARTXORFILENAME_LENGTH;
		Log.i("第一次",""+begin);//好友的文件
		byte[] startXORIndex = Message.subBytes(externalPayload,begin,Constant.BYTE_STARTXORINDEX_LENGTH );

		return startXORIndex;
	}


	public static byte[] parseStartXORFileName(byte[] externalPayload){// TODO: 2021/10/4  第二次握手和发送消息的是一样的字段，可在这里解析出开始xor文件名

		int begin = Constant.BYTE_internalPayload_BEGIN;//application-id(byte)	时间戳(byte)	message-number(byte)	message-type(byte)	payload-length(byte)

		int payloadLength = 0;

		byte[] bytePayloadLength = Message.subBytes(externalPayload, Constant.BYTE_PAYLOAD_LENGTH_BEGIN,
				Constant.BYTE_PAYLOAD_LENGTH);
		payloadLength = Integer.parseInt(AESCrypto.bytesToHex(bytePayloadLength), 16);//Payload真实长度

		begin = begin+payloadLength+Constant.BYTE_EXTERNAL_HASH;//application-id(byte)	时间戳(byte)	message-number(byte)	message-type(byte)	payload-length(byte)	internal-payload		external-hash(byte)

		byte[] startXORFileName = Message.subBytes(externalPayload,begin,Constant.BYTE_STARTXORFILENAME_LENGTH );

		return startXORFileName;
	}


	public static byte[] parseStartXORIndex(byte[] externalPayload){// TODO: 2021/10/4  第二次握手和发送消息的是一样的字段，这里可得开始xor文件的开始位置

		int begin = Constant.BYTE_internalPayload_BEGIN;//application-id(byte)	时间戳(byte)	message-number(byte)	message-type(byte)	payload-length(byte)

		int payloadLength = 0;

		byte[] bytePayloadLength = Message.subBytes(externalPayload, Constant.BYTE_PAYLOAD_LENGTH_BEGIN,
				Constant.BYTE_PAYLOAD_LENGTH);
		payloadLength = Integer.parseInt(AESCrypto.bytesToHex(bytePayloadLength), 16);//Payload真实长度

		begin = begin+payloadLength+Constant.BYTE_EXTERNAL_HASH;//application-id(byte)	时间戳(byte)	message-number(byte)	message-type(byte)	payload-length(byte)	internal-payload		external-hash(byte)

		begin = begin+Constant.BYTE_STARTXORFILENAME_LENGTH;//application-id(byte)	时间戳(byte)	message-number(byte)	message-type(byte)	payload-length(byte)	internal-payload		external-hash(byte)	startXORFileName(byte)
		Log.i("第二次",""+begin);//好友的文件
		byte[] startXORIndex = Message.subBytes(externalPayload,begin,Constant.BYTE_STARTXORINDEX_LENGTH );

		return startXORIndex;
	}



	public static byte[] parseEndXORFileName(byte[] externalPayload){// TODO: 2021/10/4  第二次握手和发送消息的是一样的字段，可在这里解析出结束xor文件名

		int begin = Constant.BYTE_internalPayload_BEGIN;//application-id(byte)	时间戳(byte)	message-number(byte)	message-type(byte)	payload-length(byte)

		int payloadLength = 0;

		byte[] bytePayloadLength = Message.subBytes(externalPayload, Constant.BYTE_PAYLOAD_LENGTH_BEGIN,
				Constant.BYTE_PAYLOAD_LENGTH);
		payloadLength = Integer.parseInt(AESCrypto.bytesToHex(bytePayloadLength), 16);//Payload真实长度

		begin = begin+payloadLength+Constant.BYTE_EXTERNAL_HASH+Constant.BYTE_STARTXORFILENAME_LENGTH+Constant.BYTE_STARTXORINDEX_LENGTH;//application-id(byte)	时间戳(byte)	message-number(byte)	message-type(byte)	payload-length(byte)	internal-payload		external-hash(byte)	startXORFileName(byte)	startXORIndex(byte)

		byte[] endXORFileName = Message.subBytes(externalPayload,begin,Constant.BYTE_ENDXORFILENAME_LENGTH );

		return endXORFileName;
	}



	public static byte[] parseEndXORIndex(byte[] externalPayload){// TODO: 2021/10/4  第二次握手和发送消息的是一样的字段，这里可得结束xor文件的位置

		int begin = Constant.BYTE_internalPayload_BEGIN;//application-id(byte)	时间戳(byte)	message-number(byte)	message-type(byte)	payload-length(byte)

		int payloadLength = 0;

		byte[] bytePayloadLength = Message.subBytes(externalPayload, Constant.BYTE_PAYLOAD_LENGTH_BEGIN,
				Constant.BYTE_PAYLOAD_LENGTH);
		payloadLength = Integer.parseInt(AESCrypto.bytesToHex(bytePayloadLength), 16);//Payload真实长度

		begin = begin+payloadLength+Constant.BYTE_EXTERNAL_HASH;//application-id(byte)	时间戳(byte)	message-number(byte)	message-type(byte)	payload-length(byte)	internal-payload		external-hash(byte)

		begin = begin+Constant.BYTE_STARTXORFILENAME_LENGTH+Constant.BYTE_STARTXORINDEX_LENGTH+Constant.BYTE_ENDXORFILENAME_LENGTH;
		//application-id(byte)	时间戳(byte)	message-number(byte)	message-type(byte)	payload-length(byte)	internal-payload		external-hash(byte)	startXORFileName(byte)	startXORIndex(byte)	endXORFileName(byte)

		byte[] endXORIndex = Message.subBytes(externalPayload,begin,Constant.BYTE_ENDXORINDEX_LENGTH );

		return endXORIndex;
	}


	public static byte[] subBytes(byte[] src, int begin, int count) {
		byte[] bs = new byte[count];
		System.arraycopy(src, begin, bs, 0, count);
		return bs;
	}

	/**
	 * 
	 * @param rawData         handshake message build function
	 * @param localPrivateKey
	 * @param remotePulicKey
	 * @return
	 * @throws Exception
	 */
	public static byte[] build(byte[] rawData, String localPrivateKey, String remotePulicKey) throws Exception {//加密握手包
		byte[] signature = RSACrypto.sign(localPrivateKey, rawData);

		byte[] aesKey = SecureRandomUtil.getRandom(16).getBytes();;

		byte[] all_data = byteMerger(rawData, signature);
		byte[] encrypt_text = AESCrypto.encrypt(aesKey, all_data);
		byte[] encrypt_key = RSACrypto.encrypt(remotePulicKey, aesKey);

		byte[] finally_package = byteMerger(encrypt_key, encrypt_text);
		return finally_package;
	}

	/**
	 * handshake message parse function
	 * 
	 * @param rawData
	 * @param localPrivateKey
	 * @param remotePulicKey
	 * @return
	 * @throws Exception
	 */
	public static byte[] parse(byte[] rawData, String localPrivateKey, String remotePulicKey) throws Exception {
		byte[] encrypt_key_server = subBytes(rawData, 0, 128);

		byte[] encrypt_text_server = subBytes(rawData, 128, 1920);
		byte[] aesKey1 = RSACrypto.decrypt(localPrivateKey, encrypt_key_server);
		byte[] encrypt_text1 = AESCrypto.decrypt(aesKey1, encrypt_text_server);
		byte[] text1 = subBytes(encrypt_text1, 0, 1790);

		return text1;
	}
	
	
	/**
	 * handshake message parse function
	 * 
	 * @param rawData
	 * @param localPrivateKey
	 * @return
	 * @throws Exception
	 */
	public static byte[] parse(byte[] rawData, String localPrivateKey) throws Exception {
		// ����˴���
//      //ʹ�÷����˽Կ�Լ��ܺ��aes��Կ����
		byte[] encrypt_key_server = subBytes(rawData, 0, 128);
//		System.out.println("���ܺ��aes_server��Կ:\n" + RSACrypto.bytesToHex(encrypt_key_server));
//		System.out.println("���ܺ��aes_server��Կ�����ǣ�" + encrypt_key_server.length);

		byte[] encrypt_text_server = subBytes(rawData, 128, 1920);
//		System.out.println("����aes���ܺ������:\n" + RSACrypto.bytesToHex(encrypt_text_server));
//		System.out.println("����aes���ܺ�����ݳ���:" + encrypt_text_server.length);

		// ����AES��Կ
		byte[] aesKey1 = RSACrypto.decrypt(localPrivateKey, encrypt_key_server);
//		System.out.println("���ܺ��aes��Կ:\n" + RSACrypto.bytesToHex(aesKey1));
//		System.out.println("���ܺ��aes��Կ����:" + aesKey1.length);

		// ��������+ǩ��
		byte[] encrypt_text1 = AESCrypto.decrypt(aesKey1, encrypt_text_server);
		byte[] text1 = subBytes(encrypt_text1, 0, 1790);
//		System.out.println("����aes���ܺ������:\n" + RSACrypto.bytesToHex(encrypt_text1));
//		System.out.println("����aes���ܺ�����ݳ���:" + encrypt_text1.length);
		
//		System.out.println("���ܺ��ǩ��:\n" + RSACrypto.bytesToHex(signature1));
//		System.out.println("���ܺ��ǩ�����ȣ�" + signature1.length);

		
		
//      System.out.println("���ܺ��ԭ��:\n" + test1.bytesToHex(text1));
//		System.out.println("���ܺ��ԭ��:\n" + new String(text1));
//		System.out.println("���ܺ��ԭ�ĳ���:" + text1.length);
		return text1;
	}

	/**
	 * data message build function
	 * 
	 * @param rawData
	 * @param sharedKey
	 * @return
	 */
	public static byte[] packDataPayload(byte[] rawData, byte[] sharedKey) {
		byte[] aesKey = sharedKey;
		byte[] packCell = null;
		try {
			packCell = AESCrypto.encrypt(aesKey, rawData);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return packCell;
	}

	/**
	 * data message parse function
	 * @param rawData
	 * @param sharedKey
	 * @return
	 */
	public static byte[] parseDataPayload(byte[] rawData, byte[] sharedKey) {
		byte[] cell1520 = null;
		try {
			cell1520 = AESCrypto.decrypt(sharedKey, rawData);//在线接收的解密
		} catch (Exception e) {
			System.out.println("Message.parseDataPayload �޷�ʹ��aes��Կ����ԭ��");
		}
		return cell1520;
	}
}
