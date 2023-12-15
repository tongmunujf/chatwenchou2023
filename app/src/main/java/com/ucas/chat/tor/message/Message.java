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


	/*needFileSize:本次需要合并的大小
	* offSet:上一次循环结束的地方。从这里开始用才对
	* */

	public static byte[] xMerger2(int needFileSize,long offSet,List< File> allXORFileList,int[] fileStartIndex,RecordXOR recordXOR ) throws IOException {// TODO: 2021/10/25 增加该分片的头尾指针的记录 // TODO: 2021/8/31  //从各个小的xor文件中组装出x数组

		byte[] x= new byte[needFileSize];//XOR组合后的总长度


		//firstFileStartIndex为本次加密第一个文件真正开始的位置
//		int firstFileStartIndex = findStartFile(allXORFileList,offSet);//获取本轮使用剩余的文件列表。因为在发送中，不删除用过的。只有发送成功收到ack才真正删除文件


		int needSize =needFileSize;
		boolean firstRead = true;//是第一次组合
		int indexX = 0;//当前写到x数组的哪个位置了，指针位置

		List<File> deleteFilesList = new ArrayList<>();//记录已用完的文件，用来删除allXORFileList用完的

		for (File xorfile:allXORFileList ){//从剩余的可用文件中

			if (needSize<=0) {

				allXORFileList.removeAll(deleteFilesList);//删除allXORFileList用完的。不是删除文件。只是内存中保存的全局
				break;
			}

			System.out.println("该文件："+xorfile);
			RandomAccessFile randomAccessFile = new RandomAccessFile(xorfile,"r");
			int xorfileSize = (int)randomAccessFile.length();//当前文件大小,这里可能有bug，要是xor文件超出int长度可能会报错

			int surplusfileSize = xorfileSize - fileStartIndex[0];//该文件能再读的剩余长度


			if (needSize-surplusfileSize>=0){//需要的比xor文件的还大


				if(firstRead) {//第一次组合x

					recordXOR.setStartFileName(Integer.parseInt(allXORFileList.get(0).getName()));// TODO: 2021/10/25 存储当前分片的开始文件名
					recordXOR.setStartFileIndex(surplusfileSize);// TODO: 2021/10/25 存储当前分片文件的开始指针

					randomAccessFile.seek(fileStartIndex[0]);//移动到该位置开始读

					byte[] temp = new byte[surplusfileSize] ;
					randomAccessFile.read(temp);//firstFileStartIndex为本次加密第一个文件真正开始的位置

					x = temp;

					firstRead = false;


					needSize=needSize-surplusfileSize;//剩余的长度

				}else {
					byte[] temp = new byte[xorfileSize] ;
					randomAccessFile.read(temp);
					x = byteMerger(x,temp);


					needSize=needSize-xorfileSize;//剩余的长度
				}

				fileStartIndex[0]=0;//这个文件用完了，下次从下一个文件的0位置开始
				deleteFilesList.add(xorfile);//记录

				recordXOR.setEndFileName(Integer.parseInt(xorfile.getName()));// TODO: 2021/10/25 存储当前分片的结束文件名
				recordXOR.setEndFileIndex(fileStartIndex[0]);// TODO: 2021/10/25 存储当前分片文件的结束指针


				randomAccessFile.close();//在这里关闭流，免得后面要删除文件删除不了

			}else {//需要的小于xor文件大小


				if(firstRead) {

					recordXOR.setStartFileName(Integer.parseInt(allXORFileList.get(0).getName()));// TODO: 2021/10/25 存储当前分片的开始文件名
					recordXOR.setStartFileIndex(surplusfileSize);// TODO: 2021/10/25 存储当前分片文件的开始指针

					randomAccessFile.seek(fileStartIndex[0]);//移动到该位置开始读
//					int surplusfileSize = xorfileSize - firstFileStartIndex;//该文件能再读的剩余长度

					byte[] temp = new byte[needSize] ;//解决剩下需要的
					randomAccessFile.read(temp);//firstFileStartIndex为本次加密第一个文件真正开始的位置

					x = temp;
					indexX = xorfileSize - 1;
					firstRead = false;


				}else {
					byte[] temp = new byte[needSize] ;
					randomAccessFile.read(temp);//不需要-1，最后一个不读
					x = byteMerger(x,temp);

				}

				fileStartIndex[0]=fileStartIndex[0]+needSize;

				recordXOR.setEndFileName(Integer.parseInt(xorfile.getName()));// TODO: 2021/10/25 存储当前分片的结束文件名
				recordXOR.setEndFileIndex(surplusfileSize-needSize);// TODO: 2021/10/25 存储当前分片文件的结束指针

				needSize=needSize-needSize;//剩余的长度
				randomAccessFile.close();//在这里关闭流，免得后面要删除文件删除不了
			}



		}


		return x;
	}


	public static byte[] xMerger(int needFileSize,long offSet) throws IOException {// TODO: 2021/8/31  //从各个小的xor文件中组装出x数组

		byte[] x= new byte[needFileSize];//XOR组合后的总长度

		File allXORFolder = new File(XORutil.XOR_PATH);//文件夹，内包含多个拆分的XOR文件
		File[] allXORFiles = allXORFolder.listFiles();//多个拆分的XOR文件

		List< File> allXORFileList = sortFile(Arrays.asList(allXORFiles));//文件的顺序有问题的，要按数字大小排

		//firstFileStartIndex为本次加密第一个文件真正开始的位置
		int firstFileStartIndex = findStartFile(allXORFileList,offSet);//获取本轮使用剩余的文件列表。因为在发送中，不删除用过的。只有发送成功收到ack才真正删除文件


		int needSize =needFileSize;
		boolean firstRead = true;//是第一次组合
		int indexX = 0;//当前写到x数组的哪个位置了，指针位置


		for (File xorfile:allXORFileList ){//从剩余的可用文件中

			if (needSize<=0)
				break;


			System.out.println("该文件："+xorfile);
			RandomAccessFile randomAccessFile = new RandomAccessFile(xorfile,"r");
			int xorfileSize = (int)randomAccessFile.length();//当前文件大小,这里可能有bug，要是xor文件超出int长度可能会报错

			int surplusfileSize = xorfileSize - firstFileStartIndex;//该文件能再读的剩余长度


			if (needSize-surplusfileSize>=0){//需要的比文件的还大


				if(firstRead) {//第一次组合x

					randomAccessFile.seek(firstFileStartIndex);//移动到该位置开始读

					byte[] temp = new byte[surplusfileSize] ;
					randomAccessFile.read(temp);//firstFileStartIndex为本次加密第一个文件真正开始的位置

					x = temp;

					firstRead = false;
					firstFileStartIndex=0;

					needSize=needSize-surplusfileSize;//剩余的长度

				}else {
					byte[] temp = new byte[xorfileSize] ;
					randomAccessFile.read(temp);
					x = byteMerger(x,temp);
					needSize=needSize-xorfileSize;//剩余的长度
				}



				randomAccessFile.close();//在这里关闭流，免得后面要删除文件删除不了

			}else {//需要的小于文件大小


				if(firstRead) {
					randomAccessFile.seek(firstFileStartIndex);//移动到该位置开始读
//					int surplusfileSize = xorfileSize - firstFileStartIndex;//该文件能再读的剩余长度

					byte[] temp = new byte[needSize] ;//解决剩下需要的
					randomAccessFile.read(temp);//firstFileStartIndex为本次加密第一个文件真正开始的位置

					x = temp;
					indexX = xorfileSize - 1;
					firstRead = false;
					firstFileStartIndex=0;

				}else {
					byte[] temp = new byte[needSize] ;
					randomAccessFile.read(temp);//不需要-1，最后一个不读
					x = byteMerger(x,temp);

				}

				needSize=needSize-needSize;//剩余的长度
				randomAccessFile.close();//在这里关闭流，免得后面要删除文件删除不了
			}



		}


		return x;
	}

	public static List<File> sortFile(List< File> allXORFileList){//给文件按数字大小排序

		if (allXORFileList ==null)
			return null;

		Collections.sort(allXORFileList, new Comparator< File>() {//https://www.cnblogs.com/dgwblog/p/9222144.html
			@Override
			public int compare(File o1, File o2) {
				if (o1.isDirectory() && o2.isFile())
					return -1;
				if (o1.isFile() && o2.isDirectory())
					return 1;
				Integer f = Integer.parseInt(o1.getName());
				Integer f2 =Integer.parseInt(o2.getName());
				return Integer.compare(f, f2);
			}
		});

		List arrayList = new ArrayList(allXORFileList);//https://blog.csdn.net/chenxiaoning87/article/details/10518623Arrays.asLisvt()。 Arrays.asLisvt()后调用add，remove这些method时出错，asLisvt返回的是java.util.Arrays$ArrayList， 而不是ArrayList

		return arrayList;
	}



	public static int findStartFile(List< File> allXORFileList,long offSet ) throws IOException {//给出长度，从全部文件中找出起始位置的文件

//		int useNumber = (int) offSet+1;//offSet最初是从0开始算的,不是1！useNumber指前面用了几个了
//		if(offSet ==0L)
		int	useNumber = (int) offSet;//发文件的第一轮加密，这里传入的是0L.还没读呢。offSet就是下一个要读的位置了


		List<File> filesList = new ArrayList<>();

		for(File xorfile:allXORFileList ){//移动到上次结束的地方

			FileInputStream fileInputStream = new FileInputStream(xorfile);
			int xorfileSize = fileInputStream.available();
			fileInputStream.close();

			if (useNumber >= xorfileSize){

				useNumber = useNumber - xorfileSize;//xorfileSize最初是从0开始算的,不是1！还剩几个useNumber
				System.out.println("读到了："+allXORFileList.indexOf(xorfile));
//                allXORFileList.remove(allXORFileList.indexOf(xorfile));//删除这个文件在数组中的。不需要再读它了。不能这样用代码

				filesList.add(xorfile);//先暂时保存，结束后再删除


			}else {//到这里的useNumber即是要从文件这里的useNumber位置开始读了

				System.out.println("找到了："+xorfile.getName());//判断文件是否够用，是否满足需要，



				break;
			}


		}

		allXORFileList.removeAll(filesList);

		return useNumber;//要从文件这里的useNumber位置开始读了

	}



	public static List<File> delectAllUsedFiles(List< File> allXORFileList,int startFileName ) {// TODO: 2021/10/4 删除 allXORFileList内存中startFileName这个文件之前用过的其他文件，只是内存的删除，不是物理删除掉

		String startFileNameString = String.valueOf(startFileName);

//		List< File> newallXORFileList = allXORFileList;
		List<File> deleteFilesList = new ArrayList<>();//记录已用完的文件，用来删除allXORFileList用完的

		for (int i=0;i<allXORFileList.size();i++){
			String ss =allXORFileList.get(i).getName();
			if(!ss.equals(startFileNameString)){
				deleteFilesList.add(allXORFileList.get(i));
			}else
				break;
		}

		allXORFileList.removeAll(deleteFilesList);//删除allXORFileList用完的。不是删除文件。只是内存中保存的全局

//		allXORFileList = newallXORFileList;

		return allXORFileList;
	}





	/**
	 * 2+4+4+1+2=13bytes for both handshake message and data message
	 * 
	 * @param payloadlength internalPayload length
	 * @param messageType
	 * @return
	 */
	public static byte[] createMessageHeader(int payloadlength, byte messageType,String messageID) {// TODO: 2021/8/24 更新消息id标记，用这个来唯一标记当前次的发送情况
//		byte[] externalPayload = new byte[30];
//		ByteBuffer data = ByteBuffer.allocate(30);
//		data.put(Constant.APPLICATION_ID);
//		Date time = new Date(System.currentTimeMillis());
//		System.out.println("message.ExternalPayload.pack.utc_timestamp:" + time.getSeconds());
//		data.order(ByteOrder.BIG_ENDIAN);
//		data.putFloat(time.getSeconds());
//		int messageNumber = 1;
//		data.putInt(messageNumber);
//		char messageType = 1;
//		data.putChar(messageType);
//		data.put(payloadlength, 0, 2);
//		data.clear();
//		data.get(externalPayload);
//		System.out.println(externalPayload.length);
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
		String stringB3 = new String(b3);

//	        message_no
		data.clear();
//		int messageNo = 1;// TODO: 2021/8/24 更新消息id标记，用动态获取的，不用这个写死的
//		data.putInt(messageNo);

//		byte[] dd = messageID.getBytes();
//		data.put(dd);// TODO: 2021/8/24 更新消息id标记，用动态获取的，这里就只能放4个字节的数据
//		data.flip();
//	        System.out.println(data.position());
//	        System.out.println(data.limit());
//	        System.out.println(data.capacity());
//		data.get(byteMessageNo);

		int intmessageID = Integer.parseInt(messageID);
//		data.order(ByteOrder.BIG_ENDIAN);//字节序(Byte Order)之大端 https://blog.csdn.net/Batac_Lee/article/details/106458515
		data.putInt(intmessageID);
//		data.position(4);
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
//		System.out.println("ԭ��:\n" + RSACrypto.bytesToHex(rawData));
//		System.out.println("ԭ�ĳ���:" + rawData.length);

		// ʹ�ÿͻ���˽Կ��rsa��Կǩ��
		byte[] signature = RSACrypto.sign(localPrivateKey, rawData);
//		System.out.println("ǩ��:\n" + RSACrypto.bytesToHex(signature));
//		System.out.println("ǩ�����ȣ�" + signature.length);

		// �������16λaes��Կ
		byte[] aesKey = SecureRandomUtil.getRandom(16).getBytes();
//		System.out.println("���ɵ�aes��Կ:\n" + RSACrypto.bytesToHex(aesKey));
//		System.out.println("���ɵ�aes��Կ����:" + aesKey.length);

		// ʹ��aes��Կ�����ݽ��м���
		byte[] all_data = byteMerger(rawData, signature);
		byte[] encrypt_text = AESCrypto.encrypt(aesKey, all_data);
//		System.out.println("����aes���ܺ������:\n" + RSACrypto.bytesToHex(encrypt_text));
//		System.out.println("����aes���ܺ�����ݳ���:" + encrypt_text.length);

		// ʹ�÷���˹�Կ����aes��Կ
		byte[] encrypt_key = RSACrypto.encrypt(remotePulicKey, aesKey);
//		System.out.println("���ܺ��aes��Կ:\n" + RSACrypto.bytesToHex(encrypt_key));
//		System.out.println("���ܺ��aes��Կ�����ǣ�" + encrypt_key.length);
		// �������

		byte[] finally_package = byteMerger(encrypt_key, encrypt_text);
//	        String temp_a = "2c796a41596bca8736a06de3fc4454771853d53390536ee54c66732bf99dd4edc08da68f38c5000ab23fc381cc0c2f713db02e8e90bc93381a236e2b5296c88ea90b48298e428fbc98ca051257d3b2f0089ba6816b043cc773401f3995617b31311a61ca411c39ce37a04919ca008332ade0289751b88d79b17ab06452b77b728c01ed5c88967acf19a3450ae4fc780ac5b7277d9da17ee2bb91858c5865c6ca3522438b33c862c590b0ca3afd00cfa5f9a5ab805107f4f7b5c14269787cd67b48dc05aa38274009b5ce7f4c2111685fb69fdbfb3eef898160f3c323f5113fa3fa8b0b27425c46f612360e6c7f4c19f336216bb4ce33f83754ae48fb490276a17839488ec2e1f3a7a76e03a7d4a4041deb1d5f2272ee85a50bbd076a16aa2eeffb2b981dfb817ff51e9fa677c2107001724cebc95cc71b1ab9f92d725e3863fc8565e9ee3c5c8f3b7437702fc45aa9eb05887bdfcee025bb5a9db6092428694930998fd81bbe92bce619695329ad4025cb0155b0a12d49e2a29c76907159048757646ab12e22864e5b3c842f0268d1a14ccbcee7805c75393f064671de0c1cd442bd6ee02bf4207dd6fa667b3e49ec49b6fd8a3ddd08d03ade570d170f9e7125f04ba20b92dbcb45906a7b1bb1c738a3fcb988bcae456904ad8fe51abd839ed68125f1269f3e185da6ad963806c452a03674444e750d032178319f23fe5c330303e22ef56a8bd8566fae5d326134d96854dc255de14afbe8153448af059bae9faa1df8565cb61f1b414097fb6eb15e076ec8bea5919f35031f94ce718e8af34980b7e4811d40dbf137f939d328eb33e2717eaec652cd1a088bb638b371f11778f6b5c2802d532399c1470ee041b7e5add117d22702ea2fabba15233bdc8760eaf0c93d20f1251e45b5f10b87146f3e1c3f414ad7c942faa3ae86cbd2bccaf265b1fe0e54081890812f2c49aa7809d7784cef8a51e5a8a414a06838a142c80a9336a6eab79cba9d5479581a90e7e4d7f8369869c2ecaff63f33a6c0e2acbb2bde7bad9f25c4956437d5b6435383846c71d77c2869aaa68d0a6d1b2a5209e3489179452efb9270a86d582d2ea83099bad28aa3a3b8b4af5db573496891623271599d9f54db88b91f8deb94a97d06fcf169acf630cc26761c3b0362fac8bbddd581faadbe867adc1e72d2be38b19d2bf3be78ef352ca5982dff8d8a73a824bd472862c255a25bdf20aecab10120441b5f8f4872a4570a10019def589cdba01a0ca17ce2e742d813ae997d5b4fae095aa7e85c82d3f941f50a0cf23891ce5649521be301e803bd404a5d4d0c99197aa6f261a614e8042342a950f9a815cb3ce44a295e96480e05566791ed462d2722b2fa525171936706ac234faf51596b6b19794e5e674eb69f6c12e00e8eda7bce27eb007965bdf605574f5f21323346ba48c389c6ee6781ed4046d2b9eaba13e32abfa5b6ef40ba023be338827a0caa1855452bc03e1ea70701883cbc39c6e5b8cacccccbfe71a8c7cc4814d2b74c2b82a0802ea52e664e2c2fc0b51adb8800968d8c5b63c15e480132a0a86e501013fced9e13323608212035a4ee523933f15e55f1060c1c41190acb0ff2de00f1955e3b88c23652eb7c6d32829dc1c76e8b477cbc763ccdf8295a819c4d1f58b1d8ad0dda5a7ecbf2dd22be64dcb7f628dda1f185a0a67247e4bf53fad20479a0b72635c1b037421133b35dab23648aa9e4b8d1b2f778c7334b5fd2909f3a8626f0b993c8e6b4002f44f340c0ba1ec490798bfa59cd5d38c6dc53eadcb14a2b36f39d17b5bffffc6bbc895371f680651b32ecf76475406b233e2dd80c0d8a6bc60ef5e2bd61f0012b88a4e7f39312b2af21cb473f993437c479502348de7a3fff7044aa196759f76d61ce92523e41192b3797add38409ef6ab81ae0010fda50f557ab50df78aacc44c4100762ada8edb1b05ac32c2f68dca21c2783079a469411ce909aec1a742cea17049e7433e1472a72b72b9ee5e7acd57133c3f050af932647af80ea04c58117a4e60c2753c0a93229a38e8fa15e52497b299e6a9f87a0d7e2eabf6ca2adccaf8aa6b82375af369b85065b027b4563c396971329c161a41081758d25fdc6d5612e9b4dc41f83c1649a074abdb05a76fe1e6efba0837ae0add6bdf1f3113830e5779f10de52be642e64134fc6198327f247cfeb8310c7590c2f451708be17f685eaa2ec0229b06cdc14df1affed3a7657757c392014f95b90f294525f3ccc8eca68ba50aa4fd65612fb2de90184f03678b45c66893ff9bbc2b9fc34b05c03bae98f594075fef93f3e1ecc006141e659fcaa0ff6dfa750f576da7f4ac74781c44cfa529160db0b721750f7c073ba4f1879e94052f15a1cd224d02fdd01c3fc664b6c41a430aeddf6ee83969442b6d7029bdc7bfa5801d78fe4e233fc4fa731bc60198c8f27de3029d7e0fed7303d4f92f74f2997a15ecc35dfce34291c97ba350e2016481b428cfe8a655182fcae6723db822101d206020d6a063d8cbaab872d7bfd2de1cea15f9bdaaae0acc37467ff77a92b662859731f837d958b9d46c0f8278f5e196138fff1aed8acec47d97b516df7cd1db6feedef690f2aaabbfd7c1f5a5b2e31ac634ae0bfc13d19ef5c1e9d0aa9e4e0caff9963214e7a94f90d02dd1c9f67b09ea33171847171dacf542254bcd5de838417fc09e0e89c2a3e5448085541ece678397dfa6014605550426331579ff470d1c4b4d98266398207c0ec59a830cdfd098b6b27171dc65f17dfaa870dd443348ba45aaac931d08e78ad5184898ec118bcd34bbe89f93d48f2c139540822014eaac36fda362b7b88014590eff60f40f8d91455eecd028eeadfc07102cf49c8358af734c682ad3df9d4f6";
//		finalMessage = RSACrypto.bytesToHex(finally_package).getBytes();
//		System.out.println("���յİ�Ϊ:\n" + RSACrypto.bytesToHex(finally_package));
//		System.out.println("���հ��ĳ�����:" + finally_package.length);

		// �ͻ��˷������ġ�ǩ���ͼ��ܺ��aes��Կ
//		System.out.println("\n************************�ָ���************************\n");
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
//		System.out.println("����aes���ܺ������:\n" + RSACrypto.bytesToHex(encrypt_text1));
//		System.out.println("����aes���ܺ�����ݳ���:" + encrypt_text1.length);
		byte[] signature1 = subBytes(encrypt_text1, 1790, 128);
//		System.out.println("���ܺ��ǩ��:\n" + RSACrypto.bytesToHex(signature1));
//		System.out.println("���ܺ��ǩ�����ȣ�" + signature1.length);

		byte[] text1 = subBytes(encrypt_text1, 0, 1790);
		boolean result = RSACrypto.checkSign(remotePulicKey, text1, signature1);
//		System.out.println("Message.parse ��ǩ:" + result);

//      System.out.println("���ܺ��ԭ��:\n" + test1.bytesToHex(text1));
//		System.out.println("���ܺ��ԭ��:\n" + new String(text1));
//		System.out.println("���ܺ��ԭ�ĳ���:" + text1.length);
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
//		System.out.println("message.Message.pack.raw_bytes:" + Arrays.toString(rawData));
//		System.out.println("Message.packDataPayload raw_bytes.length:" + rawData.length);
//        �õ�32λAES��Կ
		byte[] aesKey = sharedKey;
//		System.out.println("shared��aes��Կ:" + Arrays.toString(aesKey));
//		System.out.println("shared��aes��Կ����:" + aesKey.length);
//        ��AES��Կ���м���ԭʼ���ݺ�ǩ��
		byte[] packCell = null;
		try {
			packCell = AESCrypto.encrypt(aesKey, rawData);
//			System.out.println("����aes���ܺ������:\n" + Arrays.toString(packCell));
//			System.out.println("Message.packDataPayload ����aes���ܺ�����ݳ���:" + packCell.length);
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
//		System.out.println(
//				"================��ʼ�ⳤ��Ϊ2048�İ�=========================================================================");
//		System.out.println("message.Message.parse_message begin working...");
		byte[] cell1520 = null;
		try {
			cell1520 = AESCrypto.decrypt(sharedKey, rawData);//在线接收的解密
		} catch (Exception e) {
			System.out.println("Message.parseDataPayload �޷�ʹ��aes��Կ����ԭ��");
//			e.printStackTrace();
		}
//		System.out.println("����aes���ܺ������:\n" + Arrays.toString(cell1520));
//		System.out.println("����aes���ܺ�����ݳ���:" + cell1520.length);
		return cell1520;

	}


}
