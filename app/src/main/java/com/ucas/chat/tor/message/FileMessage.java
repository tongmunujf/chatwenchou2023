package com.ucas.chat.tor.message;

import com.ucas.chat.tor.util.AESCrypto;
import com.ucas.chat.tor.util.Constant;
import com.ucas.chat.tor.util.FileTask;
import com.ucas.chat.tor.util.RSACrypto;
import com.ucas.chat.tor.util.RecordXOR;
import com.ucas.chat.tor.util.XORutil;
import com.ucas.chat.utils.RandomUtil;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;


public class FileMessage extends Message {
	public static String parseACKDataMessage(byte[] internalPayLoad) {
		byte[] bytePasswordLength = Message.subBytes(internalPayLoad, 0, 1);
		int messageType = Integer.parseInt(AESCrypto.bytesToHex(bytePasswordLength), 16);
		System.out.println("FileMessage parseDataMessage.messageType:" + messageType);
		byte[] ackok = Message.subBytes(internalPayLoad, 4, 2);
		System.out.println("FileMessage parseDataMessage ackok " + new String(ackok));
		String ss = new String(ackok);
		if (ss.equals(Constant.DATA_ACK_CONTENT)) {
			byte[] messageHash = Message.subBytes(internalPayLoad, 6, 20);
			return new String(messageHash);
		} else {
			System.out.println("FileMessage parseDataMessage ackok check failed");
			return null;
		}

	}

	public static byte[] buildACKDataMessage(byte[] sharedKey) {
		System.out.println("DataMessage buildACKDataMessage  sharedKey :\n" + sharedKey);
		ByteBuffer data = ByteBuffer.allocate(10);
		byte[] ttype = new byte[Constant.BYTE_TTYPE];
		byte[] receiveNumber = new byte[Constant.BYTE_RECEIVE_NUMBER];
		byte[] receivePieceNumber = new byte[Constant.BYTE_RECEIVE_PIECE_NUMBER];
		byte[] OK;
		data.clear();
		data.putInt(0);
		data.position(2);
		data.get(ttype);
		byte[] a1 = Message.byteMerger(ttype, receiveNumber);

		OK = Constant.DATA_ACK_CONTENT.getBytes();
		byte[] a2 = Message.byteMerger(a1, receivePieceNumber);
		byte[] dataACK = Message.byteMerger(a2, OK);

		System.out
				.println("FileMessage createSessionAuthPaylod session_auth_internal_bytes:" + Arrays.toString(dataACK));
		byte messageType = 12;

		String messageID = RandomUtil.randomChar(); // TODO: 2021/8/24 更新消息id标记，用这个来唯一标记当前次的发送情况
		byte[] externalPayload = Message.createMessageHeader(dataACK.length, messageType, messageID);

		externalPayload = Message.byteMerger(externalPayload, dataACK);
		byte[] externalPayloadHash = AESCrypto.digest_fast(externalPayload);
		System.out.println(externalPayload.length);
		System.out.println(externalPayloadHash.length);
		byte[] header = Message.byteMerger(externalPayload, externalPayloadHash);
		byte[] payload = AESCrypto.paddingToLength(header, Constant.EXTERNAL_DATA_LEGTH);
		payload = Message.byteMerger(header, payload);
		System.out.println(payload.length);
		System.out.println("FileMessage payload :\n" + RSACrypto.bytesToHex(payload));

		byte[] finalPayload = Message.packDataPayload(payload, sharedKey);

		return finalPayload;

	}

	/**
	 * 
	 * @param sharedKey
	 * @param fileName
	 * @param fileSize
	 * @param contentHash
	 */
	public static byte[] buildFileMetaMessage(byte[] sharedKey, String fileName, long fileSize, byte[] contentHash, String messageID, RecordXOR recordXOR) {// TODO: 2021/10/25 给发送文件增加头尾xor指针
		System.out.println("FileMessage buildFileMetaMessage  sharedKey :\n" + AESCrypto.bytesToHex(sharedKey));
		
		System.out.println("FileMessage buildFileMetaMessage  fileName :\n" +fileName);
		byte[] bfileName=null;
		try {
			bfileName = fileName.getBytes("utf-8");
			System.out.println("FileMessage buildFileMetaMessage  "+AESCrypto.bytesToHex(bfileName));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("FileMessage buildFileMetaMessage  fileName.length :\n" +bfileName.length);
		System.out.println("FileMessage buildFileMetaMessage  fileSize :\n" +fileSize);
		
		System.out.println("FileMessage buildFileMetaMessage  fileNameHash :\n" +AESCrypto.bytesToHex(AESCrypto.digest_fast(bfileName)));
		
		System.out.println("FileMessage buildFileMetaMessage  contentHash :\n" +AESCrypto.bytesToHex(contentHash));
		
		
		ByteBuffer data = ByteBuffer.allocate(20);
		byte[] fileNameLength = new byte[1];
		byte[] fileSizeLength = new byte[8];
		data.clear();
		data.putInt(bfileName.length);
		data.position(3);
//		data.limit(1);
		data.get(fileNameLength);
		System.out.println("FileMessage buildFileMetaMessage  fileNameLength :\n" + AESCrypto.bytesToHex(fileNameLength));
		
		
		byte[] a1 = Message.byteMerger(fileNameLength, bfileName);

		a1 = Message.byteMerger(a1, AESCrypto.digest_fast(bfileName));
		data.clear();
		data.putLong(fileSize);
		data.position(0);
		data.get(fileSizeLength);
		System.out.println("FileMessage buildFileMetaMessage  fileSizeLength :\n" + AESCrypto.bytesToHex(fileSizeLength));
		
		
		a1 = Message.byteMerger(a1, fileSizeLength);
		a1 = Message.byteMerger(a1, contentHash);//得到 internal-payload

		System.out.println("FileMessage buildFileMetaMessage :" + Arrays.toString(a1));
		byte messageType = 6;//文件类型

//		String messageID = RandomUtil.randomChar(); // TODO: 2021/8/24 更新消息id标记，用这个来唯一标记当前次的发送情况
		byte[] externalPayload = Message.createMessageHeader(a1.length, messageType, messageID);//组装报头！ 格式：application-id(byte)	时间戳(byte)	message-number(byte)	message-type(byte)	payload-length(byte)

		externalPayload = Message.byteMerger(externalPayload, a1);//格式：application-id(byte)	时间戳(byte)	message-number(byte)	message-type(byte)	payload-length(byte)	internal-payload

		byte[] externalPayloadHash = AESCrypto.digest_fast(externalPayload);//external-hash(byte)

//		System.out.println(externalPayload.length);
//		System.out.println(externalPayloadHash.length);
		byte[] header = Message.byteMerger(externalPayload, externalPayloadHash);//格式：application-id(byte)	时间戳(byte)	message-number(byte)	message-type(byte)	payload-length(byte)	internal-payload		external-hash(byte)

		// TODO: 2021/10/5 增加xor文件的使用信息
		byte[] startFileNameAndIndex = XORutil.xorFile2Byte(recordXOR.getStartFileName(),recordXOR.getStartFileIndex());//按设计的大小合并文件名和位置
		byte[] endFileNameAndIndex = XORutil.xorFile2Byte(recordXOR.getEndFileName(),recordXOR.getEndFileIndex());//按设计的大小合并文件名和位置

		header = Message.byteMerger(header,startFileNameAndIndex);//application-id(byte)	时间戳(byte)	message-number(byte)	message-type(byte)	payload-length(byte)	internal-payload		external-hash(byte)	startXORFileName(byte)	startXORIndex(byte)
		header = Message.byteMerger(header,endFileNameAndIndex);//message-number(byte)	message-type(byte)	payload-length(byte)	internal-payload		external-hash(byte)	startXORFileName(byte)	startXORIndex(byte)	endXORFileName(byte)	endXORIndex(byte)


		byte[] payload = AESCrypto.paddingToLength(header, Constant.EXTERNAL_DATA_LEGTH);//剩余长度
		payload = Message.byteMerger(header, payload);//完成组装报文，格式：application-id(byte)	时间戳(byte)	message-number(byte)	message-type(byte)	payload-length(byte)	internal-payload		external-hash(byte)			任意填充

		System.out.println(payload.length);
		System.out.println("FileMessage buildFileMetaMessage payload :\n" + RSACrypto.bytesToHex(payload));

		byte[] finalPayload = Message.packDataPayload(payload, sharedKey);//加密
		System.out.println("FileMessage buildFileMetaMessage finalPayload :\n" + finalPayload.length);

		return finalPayload;

	}
	
	public static FileTask parseFileMetaMessage(String onionName, byte[] internalPayload) {//拆解报文真实内容
		byte[] bytefileNameLength = Message.subBytes(internalPayload, 0, 1);
		int fileNameLength = Integer.parseInt(AESCrypto.bytesToHex(bytefileNameLength), 16);
		System.out.println("FileMessage parseFileMetaMessage.fileNameLength:" + fileNameLength);
		byte[] byteFileName = Message.subBytes(internalPayload, 1, fileNameLength);
		System.out.println("FileMessage parseFileMetaMessage fileName " + AESCrypto.bytesToHex(byteFileName));
		String fileName=null;
		try {
			fileName = new String(byteFileName,"utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fileName = new String(byteFileName);
		}
		System.out.println("FileMessage parseFileMetaMessage fileName " + fileName);
		byte[] byteFileNameHash = Message.subBytes(internalPayload, 1+fileNameLength, 20);
		System.out.println("FileMessage parseFileMetaMessage fileNameHash " + AESCrypto.bytesToHex(byteFileNameHash));
		
		byte[] bytefileSize = Message.subBytes(internalPayload, 21+fileNameLength, 8);
		long fileSize = Long.parseLong(AESCrypto.bytesToHex(bytefileSize), 16);
		System.out.println("FileMessage parseFileMetaMessage.fileSize:" + fileSize);
		
		byte[] byteContentHash = Message.subBytes(internalPayload, 29+fileNameLength, 20);
		System.out.println("FileMessage parseFileMetaMessage byteContentHash " + AESCrypto.bytesToHex(byteContentHash));
		
		
		FileTask fileTask = new FileTask(onionName,fileName,fileSize,byteContentHash,1);
		return fileTask;
		
	}
	
	public static byte[] buildFileReadyMessage(byte[] sharedKey, byte[] fileNameHash,String messageID,RecordXOR recordXOR) {// TODO: 2021/10/25 给发送文件增加头尾xor指针 // TODO: 2021/8/25 增加消息id
//		byte[] a1 = AESCrypto.digest_fast(fileName.getBytes());
//		System.out.println("FileMessage buildFileReadyMessage fileName "+fileName);
		byte[] a1=fileNameHash;
		System.out.println("FileMessage buildFileReadyMessage fileNameHash :" + AESCrypto.bytesToHex(a1));
		byte messageType = 7;

//		String messageID = RandomUtil.randomChar(); // TODO: 2021/8/24 更新消息id标记，用这个来唯一标记当前次的发送情况
		byte[] externalPayload = Message.createMessageHeader(a1.length, messageType, messageID);//application-id(byte)	时间戳(byte)	message-number(byte)	message-type(byte)	payload-length(byte)


		externalPayload = Message.byteMerger(externalPayload, a1);//application-id(byte)	时间戳(byte)	message-number(byte)	message-type(byte)	payload-length(byte)	internal-payload

		byte[] externalPayloadHash = AESCrypto.digest_fast(externalPayload);//
//		System.out.println(externalPayload.length);
//		System.out.println(externalPayloadHash.length);
		byte[] header = Message.byteMerger(externalPayload, externalPayloadHash);//application-id(byte)	时间戳(byte)	message-number(byte)	message-type(byte)	payload-length(byte)	internal-payload		external-hash(byte)

		// TODO: 2021/10/5 增加xor文件的使用信息
		byte[] startFileNameAndIndex = XORutil.xorFile2Byte(recordXOR.getStartFileName(),recordXOR.getStartFileIndex());//按设计的大小合并文件名和位置
		byte[] endFileNameAndIndex = XORutil.xorFile2Byte(recordXOR.getEndFileName(),recordXOR.getEndFileIndex());//按设计的大小合并文件名和位置

		header = Message.byteMerger(header,startFileNameAndIndex);//application-id(byte)	时间戳(byte)	message-number(byte)	message-type(byte)	payload-length(byte)	internal-payload		external-hash(byte)	startXORFileName(byte)	startXORIndex(byte)
		header = Message.byteMerger(header,endFileNameAndIndex);//message-number(byte)	message-type(byte)	payload-length(byte)	internal-payload		external-hash(byte)	startXORFileName(byte)	startXORIndex(byte)	endXORFileName(byte)	endXORIndex(byte)



		byte[] payload = AESCrypto.paddingToLength(header, Constant.EXTERNAL_DATA_LEGTH);//剩余长度
		payload = Message.byteMerger(header, payload);
		System.out.println(payload.length);
		System.out.println("FileMessage buildFileReadyMessage payload :\n" + RSACrypto.bytesToHex(payload));

		byte[] finalPayload = Message.packDataPayload(payload, sharedKey);
		return finalPayload;
	}
	
	public static byte[] parseFileReadyMessage(byte[] internalPayload) {
		byte[] byteFileNameHash = Message.subBytes(internalPayload, 0, 20);
		System.out.println("FileMessage parseFileReadyMessage fileNameHash " + AESCrypto.bytesToHex(byteFileNameHash));
		return byteFileNameHash;
	}
	
	public static byte[] buildFileDataMessage(byte[] sharedKey,byte[] fileNameHash,int pieceID,byte[] fileContent,String messageID,RecordXOR recordXOR  ) {// TODO: 2021/8/25 增加消息id
//		System.out.println("FileMessage buildFileDataMessage  sharedKey :\n" + AESCrypto.bytesToHex(sharedKey));
//		System.out.println("FileMessage buildFileDataMessage  sharedKey :\n" + AESCrypto.bytesToHex(fileNameHash));
//		
//		System.out.println("FileMessage buildFileDataMessage  pieceID :\n" +pieceID);
//		System.out.println("FileMessage buildFileDataMessage  contentLength :\n" +fileContent.length);
		
		ByteBuffer data = ByteBuffer.allocate(8);
		byte[] bytePieceID = new byte[4];
		data.clear();
		data.putInt(pieceID);
		data.position(0);
		data.get(bytePieceID);
		byte[] a1 = Message.byteMerger(fileNameHash, bytePieceID);
		a1 = Message.byteMerger(a1, fileContent);
		
		byte messageType = 8;

//		String messageID = RandomUtil.randomChar(); // TODO: 2021/8/24 更新消息id标记，用这个来唯一标记当前次的发送情况
		byte[] externalPayload = Message.createMessageHeader(a1.length, messageType, messageID);

		externalPayload = Message.byteMerger(externalPayload, a1);
		byte[] externalPayloadHash = AESCrypto.digest_fast(externalPayload);
//		System.out.println(externalPayload.length);
//		System.out.println(externalPayloadHash.length);
		byte[] header = Message.byteMerger(externalPayload, externalPayloadHash);

		// TODO: 2021/10/5 增加xor文件的使用信息
		byte[] startFileNameAndIndex = XORutil.xorFile2Byte(recordXOR.getStartFileName(),recordXOR.getStartFileIndex());//按设计的大小合并文件名和位置
		byte[] endFileNameAndIndex = XORutil.xorFile2Byte(recordXOR.getEndFileName(),recordXOR.getEndFileIndex());//按设计的大小合并文件名和位置

		header = Message.byteMerger(header,startFileNameAndIndex);//application-id(byte)	时间戳(byte)	message-number(byte)	message-type(byte)	payload-length(byte)	internal-payload		external-hash(byte)	startXORFileName(byte)	startXORIndex(byte)
		header = Message.byteMerger(header,endFileNameAndIndex);//message-number(byte)	message-type(byte)	payload-length(byte)	internal-payload		external-hash(byte)	startXORFileName(byte)	startXORIndex(byte)	endXORFileName(byte)	endXORIndex(byte)


		byte[] payload = AESCrypto.paddingToLength(header, Constant.EXTERNAL_DATA_LEGTH);
		payload = Message.byteMerger(header, payload);
//		System.out.println(payload.length);
//		System.out.println("FileMessage buildFileDataMessage :\n" + RSACrypto.bytesToHex(payload));
		byte[] finalPayload = Message.packDataPayload(payload, sharedKey);
		return finalPayload;
	}
	
	
	public static void parseFileDataMessage(byte[] internalPayload) {
		byte[] byteFileNameHash = Message.subBytes(internalPayload, 0, 20);
		System.out.println("FileMessage parseFileDataMessage fileNameHash " + AESCrypto.bytesToHex(byteFileNameHash));
		
		byte[] bytePieceID = Message.subBytes(internalPayload, 20, 4);
		int pieceID = Integer.parseInt(AESCrypto.bytesToHex(bytePieceID), 16);
		System.out.println("FileMessage parseFileMetaMessage.pieceID:" + pieceID);
		
		byte[] fileContent = Message.subBytes(internalPayload, 24, internalPayload.length-24);
		
		
		
				
	}
	
	
	
	public static byte[] parseFileDoneMessage(byte[] internalPayload) {
		byte[] byteFileNameHash = Message.subBytes(internalPayload, 0, 20);
		System.out.println("FileMessage parseFileDoneMessage fileNameHash " + AESCrypto.bytesToHex(byteFileNameHash));
		return byteFileNameHash;
	}
	
	public static byte[] buildFileDoneMessage(byte[] sharedKey, byte[] fileNameHash,String messageID,RecordXOR recordXOR  ) {// TODO: 2021/10/25 新增本次发送文件大指针// TODO: 2021/8/25 增加消息id
		byte[] a1 = fileNameHash;
		System.out.println("FileMessage buildFileDoneMessage :" + Arrays.toString(a1));
		byte messageType = 9;

//		String messageID = RandomUtil.randomChar(); // TODO: 2021/8/24 更新消息id标记，用这个来唯一标记当前次的发送情况
		byte[] externalPayload = Message.createMessageHeader(a1.length, messageType, messageID);

		externalPayload = Message.byteMerger(externalPayload, a1);
		byte[] externalPayloadHash = AESCrypto.digest_fast(externalPayload);
//		System.out.println(externalPayload.length);
//		System.out.println(externalPayloadHash.length);
		byte[] header = Message.byteMerger(externalPayload, externalPayloadHash);

		// TODO: 2021/10/5 增加xor文件的使用信息
		byte[] startFileNameAndIndex = XORutil.xorFile2Byte(recordXOR.getStartFileName(),recordXOR.getStartFileIndex());//按设计的大小合并文件名和位置
		byte[] endFileNameAndIndex = XORutil.xorFile2Byte(recordXOR.getEndFileName(),recordXOR.getEndFileIndex());//按设计的大小合并文件名和位置

		header = Message.byteMerger(header,startFileNameAndIndex);//application-id(byte)	时间戳(byte)	message-number(byte)	message-type(byte)	payload-length(byte)	internal-payload		external-hash(byte)	startXORFileName(byte)	startXORIndex(byte)
		header = Message.byteMerger(header,endFileNameAndIndex);//message-number(byte)	message-type(byte)	payload-length(byte)	internal-payload		external-hash(byte)	startXORFileName(byte)	startXORIndex(byte)	endXORFileName(byte)	endXORIndex(byte)



		byte[] payload = AESCrypto.paddingToLength(header, Constant.EXTERNAL_DATA_LEGTH);
		payload = Message.byteMerger(header, payload);
		System.out.println(payload.length);
		System.out.println("FileMessage buildFileDoneMessage payload :\n" + RSACrypto.bytesToHex(payload));

		byte[] finalPayload = Message.packDataPayload(payload, sharedKey);
		return finalPayload;
	}
	
	
	
	

}
