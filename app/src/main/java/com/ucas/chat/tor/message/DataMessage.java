package com.ucas.chat.tor.message;

import com.ucas.chat.tor.util.AESCrypto;
import com.ucas.chat.tor.util.Constant;
import com.ucas.chat.tor.util.RecordXOR;
import com.ucas.chat.tor.util.XORutil;
import com.ucas.chat.utils.RandomUtil;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * text message
 * @author happywindy
 *
 */
public class DataMessage extends Message {

	
	public static ACKMessage parseACKDataMessage(byte[] internalPayLoad) {
		byte[] byteACKType = Message.subBytes(internalPayLoad, 0, 1);//ack_type

		int messageType = Integer.parseInt(AESCrypto.bytesToHex(byteACKType), 16);
//		System.out.println("DataMessage parseDataMessage.messageType:" + messageType);
		byte[] bytePieceNumber = Message.subBytes(internalPayLoad, 1, 4);//receive-piece-number

//		System.out.println("DataMessage parseDataMessage.recievePieceNumber in bytes:" + AESCrypto.bytesToHex(bytePieceNumber));
		 int recievePieceNumber = Integer.parseInt(AESCrypto.bytesToHex(bytePieceNumber), 16);
//		System.out.println("DataMessage parseDataMessage.recievePieceNumber:" + recievePieceNumber);
		
		byte[] ackok = Message.subBytes(internalPayLoad, 5, 2);//ok,这是小写

//		System.out.println("DataMessage parseDataMessage ackok " + new String(ackok));
		String ss = new String(ackok);
		
		byte[] messageHash = Message.subBytes(internalPayLoad, 7, 20);
//		System.out.println("DataMessage parseDataMessage ackok "+AESCrypto.bytesToHex(messageHash));
		
		
		ACKMessage message = new ACKMessage(messageType,recievePieceNumber,ss,messageHash);
		
		return message;
//		if (ss.equals(Constant.DATA_ACK_CONTENT)) {
//			return new String(messageHash);
//		} else {
//			System.out.println("DataMessage parseDataMessage ackok check failed");
//			return null;
//		}

	}
/**
 * 
 * @param sharedKey
 * @param ackMessageType
 * @param pieceID
 * @param ack
 * @param messageHash
 * @return
 */
	public static byte[] buildACKDataMessage(byte[] sharedKey, int ackMessageType, int pieceID, String ack, byte[] messageHash, String messageID, RecordXOR recordXOR ) {// TODO: 2021/10/25 在ack增加xor异或头尾指针// TODO: 2021/8/24 加入文本消息的id
//		System.out.println("DataMessage buildACKDataMessage  sharedKey :\n" + sharedKey);
		ByteBuffer data = ByteBuffer.allocate(10);
		byte[] ttype = new byte[Constant.BYTE_TTYPE];//ack_type
		data.clear();
		data.putInt(ackMessageType);
		data.position(3);//int类型是4个字节，所以最后一个是3位置（0开始）
		data.get(ttype);
		
		
		byte[] bytePieceID = new byte[4];
		data.clear();
		data.position(0);
		data.putInt(pieceID);
		data.position(0);
		data.get(bytePieceID);
		byte[] a1 = Message.byteMerger(ttype, bytePieceID);//ack_type		receive-piece-number

		byte[] a2 = Message.byteMerger(a1, ack.getBytes());//ack_type		receive-piece-number		ok,这是小写
		byte[] dataACK = Message.byteMerger(a2, messageHash);//ack_type		receive-piece-number		ok,这是小写	file_id_hash

// TODO: 2021/10/5 增加xor文件的使用信息
		byte[] startFileNameAndIndex = XORutil.xorFile2Byte(recordXOR.getStartFileName(),recordXOR.getStartFileIndex());//按设计的大小合并文件名和位置
		byte[] endFileNameAndIndex = XORutil.xorFile2Byte(recordXOR.getEndFileName(),recordXOR.getEndFileIndex());//按设计的大小合并文件名和位置

		dataACK = Message.byteMerger(dataACK,startFileNameAndIndex);//application-id(byte)	时间戳(byte)	message-number(byte)	message-type(byte)	payload-length(byte)	internal-payload		external-hash(byte)	startXORFileName(byte)	startXORIndex(byte)
		dataACK = Message.byteMerger(dataACK,endFileNameAndIndex);//message-number(byte)	message-type(byte)	payload-length(byte)	internal-payload		external-hash(byte)	startXORFileName(byte)	startXORIndex(byte)	endXORFileName(byte)	endXORIndex(byte)

		System.out.println("dataACK:"+ Arrays.toString(dataACK));


//		System.out.println("DataMessage createSessionAuthPaylod session_auth_internal_bytes:" + Arrays.toString(dataACK));
		byte messageType = 12;

//		String messageID = RandomUtil.randomChar(); // TODO: 2021/8/24 更新消息id标记，用这个来唯一标记当前次的发送情况
		byte[] externalPayload = Message.createMessageHeader(dataACK.length, messageType, messageID);//application-id(byte)	时间戳(byte)	message-number(byte)	message-type(byte)	payload-length(byte)

		externalPayload = Message.byteMerger(externalPayload, dataACK);//application-id(byte)	时间戳(byte)	message-number(byte)	message-type(byte)	payload-length(byte)	internal-payload

		byte[] externalPayloadHash = AESCrypto.digest_fast(externalPayload);
//		System.out.println(externalPayload.length);
//		System.out.println(externalPayloadHash.length);
		byte[] header = Message.byteMerger(externalPayload, externalPayloadHash);//时间戳(byte)	message-number(byte)	message-type(byte)	payload-length(byte)	internal-payload		external-hash(byte)

		byte[] payload = AESCrypto.paddingToLength(header, Constant.EXTERNAL_DATA_LEGTH);//扩充长度
		payload = Message.byteMerger(header, payload);//时间戳(byte)	message-number(byte)	message-type(byte)	payload-length(byte)	internal-payload		external-hash(byte)			任意填充

//		System.out.println(payload.length);
//		System.out.println("DataMessage payload :\n" + RSACrypto.bytesToHex(payload));

		byte[] finalPayload = Message.packDataPayload(payload, sharedKey);//加密

		return finalPayload;

	}

}
