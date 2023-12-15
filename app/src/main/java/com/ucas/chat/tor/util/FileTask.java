package com.ucas.chat.tor.util;

import android.util.Log;

import com.google.android.material.tabs.TabLayout;
import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.jni.JniEntryUtils;
import com.ucas.chat.jni.ServiceLoaderImpl;
import com.ucas.chat.jni.common.IDecry;
import com.ucas.chat.jni.common.IEntry;
import com.ucas.chat.tor.message.Message;

import org.apaches.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ToDoubleBiFunction;

public class FileTask {
	private static String TAG = ConstantValue.TAG_CHAT + "FileTask";
	private String fileName;
	private byte[] fileNameHash;
	/**
	 * send :0 recieve :1
	 */
	private int direction;

	private String fileFullPath;
	private long totalSize;
	private byte[] contentHash;
	/**
	 * 0:pic;1:file
	 */
	private int fileType;
	/**
	 * 
	 */
	private int pieceSize;
	private int totalPieceNumber;
	/**
	 * 0: stop; 
	 * 1:transferrring;转移中
	 * 2:compelete;
	 * 3:ck send;
	 * 4: not start
	 */
	private int status;

	private Date startTime;
	private Date endTime;

	/**
	 * recieved ack piece count
	 */
	private int count;

	private double speed;
	private double percent;
	private int stepNumber=1;
	private String onionName;

	private String messageID;

	public String getMessageID() {
		return messageID;
	}

	public void setMessageID(String messageID) {
		this.messageID = messageID;
	}

	/**
	 * file transfer status, key :piece number value: 1:recieved ack piece; 0 not
	 */
	private ConcurrentHashMap<Integer, Integer> fileTransferStatusMap = new ConcurrentHashMap<Integer, Integer>();
	/**
	 * cut file into pieces ,piece number start with 0 key: piece number value: file
	 * content
	 */
	private ConcurrentHashMap<Integer, byte[]> filePieceContentMap = new ConcurrentHashMap<Integer, byte[]>();
	/**
	 * cut file into pieces ,piece number start with 0 key: piece number value: file
	 * content hash
	 */
	private ConcurrentHashMap<Integer, byte[]> filePieceHashMap = new ConcurrentHashMap<Integer, byte[]>();
	private DecimalFormat df = new DecimalFormat("######0.00");

	private ConcurrentHashMap<Integer, RecordXOR> filePieceRecordXORMap = new ConcurrentHashMap<>();// TODO: 2021/10/25 记录每一个文件分片的xor指针

	private RecordXOR fileTaskRecordXOR = new RecordXOR();// TODO: 2021/10/25  本次发送文件的总开始指针和总结束指针


	/**
	 * send file
	 * 
	 * @param filePath
	 * @param direction
	 */
	public FileTask(String onionName,String filePath, int direction,RecordXOR recordXOR )  {// TODO: 2021/10/25 给发送文件增加文件xor指针
		this.fileFullPath = filePath;
		this.onionName = onionName;
		this.direction = direction;
		this.pieceSize = Constant.FILE_PIECE_SIZE;
		this.contentHash = FileUtil.getFileSHA1(new File(this.fileFullPath));// TODO: 2021/10/27 用于文件完整性校验 ！
		Log.d(TAG, " FileTask:: 异或前this.contentHash数据：" + AESCrypto.bytesToHex(this.contentHash));

		this.fileTaskRecordXOR = recordXOR;// TODO: 2021/10/25

		cutFile();// TODO: 2021/9/2   新增，组装各个小的xor来加密文件


		try {

			MessageDigest md = MessageDigest.getInstance("SHA-1");//MessageDigest 类为应用程序提供信息摘要算法的功能https://www.cnblogs.com/lxnlxn/p/9993969.html
			for (Map.Entry<Integer, byte[]> entry : filePieceContentMap.entrySet()) {
//			String key = entry.getKey().toString();
				byte[] b = entry.getValue();
				md.update(b, 0, b.length);//处理数据
			}

//		md.digest();//获得密文完成哈希计算,产生160 位的长整数？,相当于文件加密了
			Log.d(TAG, " FileTask:: 异或后this.contentHash数据："+AESCrypto.bytesToHex(md.digest()));


		}catch (Exception e){


		}

		int temp = this.totalPieceNumber/100;
		if (temp != 0){
			this.stepNumber = temp;
		}
	}


	public FileTask(String onionName,String filePath, int direction,byte[] bitmapBytes,RecordXOR recordXOR) {// TODO: 2021/8/16，照片的
		Log.d(TAG, " FileTask:: filePath = " + filePath);
		//Log.d(TAG, " FileTask:: bitmapBytes = " + Arrays.toString(bitmapBytes));
		this.fileFullPath = filePath;
		this.onionName = onionName;
		this.direction = direction;
		this.pieceSize = Constant.FILE_PIECE_SIZE;
		this.contentHash = FileUtil.getFileSHA1_2(bitmapBytes);

		this.fileTaskRecordXOR = recordXOR;// TODO: 2021/10/25

		cutBitmap(bitmapBytes);// TODO: 2021/9/23 用拆分的xor异或 

		int temp = this.totalPieceNumber/100;
		if (temp != 0){
			this.stepNumber = temp;
		}
	}


	/**
	 * recievie file
	 * 
	 * @param fileName
	 * @param fileSize
	 * @param contentHash
	 * @param direction
	 */
	public FileTask(String onionName,String fileName, long fileSize, byte[] contentHash, int direction) {
		this.fileName = fileName;
		this.totalSize = fileSize;
		this.contentHash = contentHash;
		this.direction = direction;
		this.onionName = onionName;
		try {
			this.fileNameHash = AESCrypto.digest_fast(this.fileName.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			System.out.println("FileTask  recievie file constructor filename decode error"+e.getMessage());
			this.fileNameHash = AESCrypto.digest_fast(this.fileName.getBytes());
		}
		this.pieceSize = Constant.FILE_PIECE_SIZE;
		this.totalPieceNumber = (int) Math.floor(this.totalSize / this.pieceSize);
		if (this.totalSize % this.pieceSize != 0) {
			this.totalPieceNumber = this.totalPieceNumber + 1;
		}
		this.startTime = new Date();//当前生成的新时间

		int temp = this.totalPieceNumber/100;
		if (temp != 0){
			this.stepNumber = temp;
		}

	}

	private void cutFile() {// TODO: 2021/9/2   新增，组装各个小的xor来加密文件，优化cutFileNew()，只有2层循环
		try {

			File f = new File(this.fileFullPath);
			this.fileName = f.getName();
			this.fileNameHash = AESCrypto.digest_fast(this.fileName.getBytes("utf-8"));

			RandomAccessFile file = new RandomAccessFile(f, "r");
			this.totalSize = file.length();
			Log.d(TAG, " cutFile:: 发送文件大小 = " + this.totalSize);
			this.totalPieceNumber = (int) Math.floor(this.totalSize / this.pieceSize);
			if (this.totalSize % this.pieceSize != 0) {
				this.totalPieceNumber = this.totalPieceNumber + 1;
			}

			long offSet = 0L;


			File allXORFolder = new File(XORutil.XOR_PATH);//文件夹，内包含多个拆分的XOR文件
			File[] allXORFiles = allXORFolder.listFiles();//多个拆分的XOR文件

			List< File> allXORFileList = Message.sortFile(Arrays.asList(allXORFiles));//文件的顺序有问题的，要按数字大小排

			int startFileName = this.fileTaskRecordXOR.getStartFileName();//从这个文件开始用
			int startFileIndex = this.fileTaskRecordXOR.getStartFileIndex();//从这个文件这里开始用

			allXORFileList = Message.delectAllUsedFiles(allXORFileList,startFileName);// TODO: 2021/10/4 得到以 startFileName开始的allXORFileList

			FileInputStream fileInputStream =new FileInputStream(allXORFileList.get(0));
			int fileStart = fileInputStream.available()-startFileIndex;//endFileIndex是到文件尾的距离，真实用的时候是从头部开始的，这里就是此时文件头往文件尾有fileStart距离的位置开始用
			fileInputStream.close();

			int[] fileStartIndex = {fileStart};// TODO: 2021/10/4 从endFileIndex开始用 //记录每一次发送文件时，里面的每一轮分片进行xor文件的起始点//优化的地方，用数组才能保存和更新数值


//			int[] fileStartIndex = {0};//记录每一次发送文件时，里面的每一轮分片进行xor文件的起始点//优化的地方，用数组才能保存和更新数值

			RecordXOR recordXOR = new RecordXOR();// TODO: 2021/10/25 临时变量，存储每一次分片的指针

			for (int i = 0; i < this.totalPieceNumber - 1; i++) {//这里的循环只处理到倒数第二个
				long begin = offSet;
				file.seek(begin);//将文件游标移动到文件的begin位置,

				byte[] b = new byte[this.pieceSize];
				file.read(b);


				if(i>=675){
					Log.d(TAG, " cutFile:: 该文件for：" + i);
				}

				Log.d(TAG, " cutFile:: 未加密的文件字节片段16进制："+AESCrypto.bytesToHex(b));

				byte[] x= Message.xMerger2(this.pieceSize,offSet,allXORFileList,fileStartIndex,recordXOR);//各个小XOR组合后的总长度，这里需要pieceSize长度，会改变allXORFileList,fileStartIndex的值
//				Log.d(TAG, " cutFile:: 异或材料："+Arrays.toString(x));
//				Log.d(TAG, " cutFile:: 异或材料："+AESCrypto.bytesToHex(x));

				if (x.length !=this.pieceSize)
					Log.d(TAG, " cutFile:: 密钥不够用了！");//这里需要处理不够用的逻辑

//				Log.d(TAG, " cutFile:: 加密的文件字节片段异或xor片段16进制："+AESCrypto.bytesToHex(x));
//				Log.d(TAG, " cutFile:: 异或片段" + i + ": "+recordXOR);

				//byte[] c= this.byteArrayXOR(b, x);//在这个之前完成各个小XOR的组装x
				//byte[] c = ServiceLoaderImpl.load(IEntry.class).entry("++++",FilePathUtils.SECRET_KEY_FILE, b);
				byte[] c = JniEntryUtils.entry(b);
				Log.d(TAG," cutFile:: 加密内容c = " + AESCrypto.bytesToHex(c));

				this.filePieceContentMap.put(i, c);//存放分片
				this.filePieceHashMap.put(i, DigestUtils.sha1(c));

				Log.d(TAG, " cutFile:: 加密的文件字节16进制片段："+AESCrypto.bytesToHex(c));

				this.filePieceRecordXORMap.put(i,recordXOR);// TODO: 2021/10/25 将当前分片序号对应的指针信息保存

				offSet = file.getFilePointer();
//				System.out.println("offset "+offSet);
			}
			if (this.totalSize - offSet > 0) {//还有剩的!
				byte[] b = new byte[this.pieceSize];
				int n = file.read(b);
				System.out.println(n);
				System.out.println(b.length);
				byte[] t = new byte[n];
				System.arraycopy(b, 0, t, 0, n);//拷贝可以各自独立，而不是赋值引用会相互影响

				Log.d(TAG, " cutFile:: 未加密的文件字节片段16进制：" + AESCrypto.bytesToHex(b));

				byte[] x= Message.xMerger2(n,offSet,allXORFileList,fileStartIndex,recordXOR);//各个小XOR组合后的总长度，这里需要pieceSize长度

//				Log.d(TAG, " cutFile:: 加密的文件字节片段异或xor片段16进制："+AESCrypto.bytesToHex(x));
//				Log.d(TAG, " cutFile:: 异或材料："+Arrays.toString(x));
//				Log.d(TAG, " cutFile:: 异或材料："+AESCrypto.bytesToHex(x));

				if (x.length !=n)
					Log.d(TAG, " cutFile:: 密钥不够用了！");//这里需要处理不够用的逻辑

				//Log.d(TAG, " cutFile:: 异或片段"+(this.totalPieceNumber - 1)+": "+recordXOR);

				byte[] c = JniEntryUtils.entry(t);
				//byte[] c = ServiceLoaderImpl.load(IEntry.class).entry("++++",FilePathUtils.SECRET_KEY_FILE, t);
				//byte[] c= this.byteArrayXOR(t, x);
				Log.d(TAG, " cutFile:: 加密后的文件数据：" + AESCrypto.bytesToHex(c));
				Log.d(TAG, " cutFile:: 加密后的文件数据的哈希：" + AESCrypto.digest_fast(c));
				Log.d(TAG, " cutFile:: 加密的文件字节16进制片段：" + AESCrypto.bytesToHex(c));

				this.filePieceContentMap.put(this.totalPieceNumber - 1, c);
				this.filePieceHashMap.put(this.totalPieceNumber, DigestUtils.sha1(c));// TODO: 2021/10/25 是否还需要-1

				this.filePieceRecordXORMap.put(this.totalPieceNumber - 1,recordXOR);// TODO: 2021/10/25 将当前分片序号对应的指针信息保存

			}

			this.fileTaskRecordXOR.setEndFileName(recordXOR.getEndFileName());// TODO: 2021/10/25 将最后一次异或的结束指针给本次发送文件的指针
			this.fileTaskRecordXOR.setEndFileIndex(recordXOR.getEndFileIndex());

			file.close();
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, " cutFile::cut file error " + e.getMessage());
		}
	}

	private void cutBitmap(byte[] bitmapBytes) {// TODO: 2021/9/22 新增，组装各个小的xor来加密文件。发照片用这个来分片，
		try {

			this.fileName = fileFullPath;
			this.fileNameHash = AESCrypto.digest_fast(this.fileName.getBytes("utf-8"));

			this.totalSize = bitmapBytes.length;
			this.totalPieceNumber = (int) Math.floor(this.totalSize / this.pieceSize);
			if (this.totalSize % this.pieceSize != 0) {
				this.totalPieceNumber = this.totalPieceNumber + 1;
			}
			Log.d(TAG, " cutBitmap:: totalsize:" + this.totalSize);
			Log.d(TAG, " cutBitmap:: totalPieceNumber:" + this.totalPieceNumber);

			long offSet = 0L;

			File allXORFolder = new File(XORutil.XOR_PATH);//文件夹，内包含多个拆分的XOR文件
			File[] allXORFiles = allXORFolder.listFiles();//多个拆分的XOR文件

			List< File> allXORFileList = Message.sortFile(Arrays.asList(allXORFiles));//文件的顺序有问题的，要按数字大小排


			int startFileName = this.fileTaskRecordXOR.getStartFileName();//从这个文件开始用
			int startFileIndex = this.fileTaskRecordXOR.getStartFileIndex();//从这个文件这里开始用

			allXORFileList = Message.delectAllUsedFiles(allXORFileList,startFileName);// TODO: 2021/10/4 得到以 startFileName开始的allXORFileList

			FileInputStream fileInputStream =new FileInputStream(allXORFileList.get(0));
			int fileStart = fileInputStream.available()-startFileIndex;//endFileIndex是到文件尾的距离，真实用的时候是从头部开始的，这里就是此时文件头往文件尾有fileStart距离的位置开始用
			fileInputStream.close();

			int[] fileStartIndex = {fileStart};// TODO: 2021/10/4 从endFileIndex开始用 //记录每一次发送文件时，里面的每一轮分片进行xor文件的起始点//优化的地方，用数组才能保存和更新数值

			RecordXOR recordXOR = new RecordXOR();// TODO: 2021/10/25 记录每一次的头尾指针

			for (int i = 0; i < this.totalPieceNumber - 1; i++) {

				byte[] b = new byte[this.pieceSize];
				b = Arrays.copyOfRange(bitmapBytes,i*this.pieceSize,i*this.pieceSize+this.pieceSize);

				if(i>=675){
					Log.d(TAG, " cutBitmap:: 该文件for："+i);
				}
				byte[] x= Message.xMerger2(this.pieceSize,offSet,allXORFileList,fileStartIndex,recordXOR);//各个小XOR组合后的总长度，这里需要pieceSize长度，会改变allXORFileList,fileStartIndex的值
				if (x.length !=this.pieceSize)
					Log.d(TAG, " cutBitmap::密钥不够用了！");//这里需要处理不够用的逻辑

				//byte[] c= this.byteArrayXOR(b, x);
				//byte[] c = ServiceLoaderImpl.load(IEntry.class).entry("++++",FilePathUtils.SECRET_KEY_FILE, b);
				byte[] c = JniEntryUtils.entry(b);
				Log.d(TAG, " cutBitmap:: 加密后的图片数据：" + AESCrypto.bytesToHex(c));
				Log.d(TAG, " cutBitmap:: 加密后的图片数据的哈希：" + AESCrypto.digest_fast(c));
				Log.d(TAG, " cutBitmap:: 加密的图片字节16进制片段：" + AESCrypto.bytesToHex(c));
				this.filePieceContentMap.put(i, c);//存放分片
				this.filePieceHashMap.put(i, DigestUtils.sha1(c));

				this.filePieceRecordXORMap.put(i,recordXOR);// TODO: 2021/10/25 将当前分片序号对应的指针信息保存

				offSet = i*this.pieceSize+this.pieceSize;
//				System.out.println("offset "+offSet);
			}
			if (this.totalSize - offSet > 0) {//还有剩的!
				byte[] b = new byte[this.pieceSize];
				int n = (int)(this.totalSize - offSet );
				System.out.println(n);
				System.out.println(b.length);
//				byte[] t = new byte[n];
//				System.arraycopy(b, 0, t, 0, n);
//				System.out.println("the last piece");

				byte[] t = Arrays.copyOfRange(bitmapBytes,(int)offSet,(int)this.totalSize);
				Log.d(TAG, " cutBitmap:: 加密前图片件数据：" + AESCrypto.bytesToHex(t));
				
//				byte[] x= new byte[n];
//				xorfile.read(x);
				byte[] x= Message.xMerger2(n,offSet,allXORFileList,fileStartIndex,recordXOR);//各个小XOR组合后的总长度，这里需要pieceSize长度
				if (x.length !=n)
					System.out.println("密钥不够用了！");//这里需要处理不够用的逻辑



				//byte[] c= this.byteArrayXOR(t, x);
				//byte[] c = ServiceLoaderImpl.load(IEntry.class).entry("++++",FilePathUtils.SECRET_KEY_FILE, t);
				byte[] c = JniEntryUtils.entry(t);
				Log.d(TAG, " cutBitmap:: 加密后图片件数据：" + AESCrypto.bytesToHex(c));

				this.filePieceContentMap.put(this.totalPieceNumber - 1, c);
				this.filePieceHashMap.put(this.totalPieceNumber, DigestUtils.sha1(c));// TODO: 2021/10/25 是否还需要-1

				this.filePieceRecordXORMap.put(this.totalPieceNumber - 1,recordXOR);// TODO: 2021/10/25 将当前分片序号对应的指针信息保存
//				System.out.println("offset "+offSet);
			}

			this.fileTaskRecordXOR.setEndFileName(recordXOR.getEndFileName());// TODO: 2021/10/25 将最后一次异或的结束指针给本次发送文件的指针
			this.fileTaskRecordXOR.setEndFileIndex(recordXOR.getEndFileIndex());

		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, " cutBitmap:: cut file error " + e.getMessage());
		}
	}

	public int getStepNumber() {
		return stepNumber;
	}
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getOnionName() {
		return onionName;
	}

	public int getDirection() {
		return direction;
	}

	public long getTotalSize() {
		return totalSize;
	}

	public int getTotalPieceNumber() {
		return totalPieceNumber;
	}

	public ConcurrentHashMap<Integer, Integer> getFileTransferStatusMap() {
		return fileTransferStatusMap;
	}

	public ConcurrentHashMap<Integer, byte[]> getFilePieceContentMap() {
		return filePieceContentMap;
	}

	public ConcurrentHashMap<Integer, byte[]> getFilePieceHashMap() {
		return filePieceHashMap;
	}

	public String getFileName() {
		return fileName;
	}

	public byte[] getContentHash() {
		return contentHash;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public byte[] getFileNameHash() {
		return fileNameHash;
	}

	public void setFileTaskRecordXOR(RecordXOR recordXOR) {
		this.fileTaskRecordXOR = recordXOR;
	}

	public RecordXOR getFileTaskRecordXOR() {// TODO: 2021/10/25 新增本次发送文件大指针
		return fileTaskRecordXOR;
	}


	public ConcurrentHashMap<Integer, RecordXOR> getFilePieceRecordXORMap() {// TODO: 2021/10/25 新增本次发送文件大指针
		return filePieceRecordXORMap;
	}

	public void setFilePieceRecordXORMap(ConcurrentHashMap<Integer, RecordXOR> filePieceRecordXORMap) {
		this.filePieceRecordXORMap = filePieceRecordXORMap;
	}

	public void updateTransferStatus(int pieceID) {
//		if(this.startTime==null) {
//			this.startTime=new Date();
//		}
		this.fileTransferStatusMap.put(pieceID, 1);
		System.out.println("currently acked or recieved piece number "+ this.fileTransferStatusMap.size());
		this.percent = (this.fileTransferStatusMap.size() * 100.0 / (double) this.totalPieceNumber);
		
		double timeinterval  = (double) (new Date().getTime() - this.startTime.getTime())/1000;
		System.out.println("timeinterval is "+timeinterval);
		this.speed = (this.fileTransferStatusMap.size() * Constant.FILE_PIECE_SIZE )// TODO: 2021/10/25 改1990为Constant.FILE_PIECE_SIZE 
				/(1024 * timeinterval);

	}
	
	public void recievePieceData(int pieceID,byte[] content) {//分片存储
		this.filePieceContentMap.put(pieceID, content);
		this.filePieceHashMap.put(pieceID, DigestUtils.sha1(content));//20字节
		this.updateTransferStatus(pieceID);
	}
	

	public byte[] getPieceContentByID(int pieceID) {
		return this.filePieceContentMap.get(pieceID);
	}

	public String getSpeed() {
		return this.df.format(speed);
	}

	public String getPercent() {
		return this.df.format(percent);
	}

	public int getTransfteredNumber() {
		return this.fileTransferStatusMap.size();
	}

	private static byte[] byteArrayXOR(byte[] a, byte[] b) {
		byte[] c=new byte[a.length];
		for(int i=0;i<a.length;i++) {
			c[i]=(byte)(a[i]^b[i]);//按位异或
		}

//		System.out.println("a piece "+AESCrypto.bytesToHex(a));
//		System.out.println("b piece "+AESCrypto.bytesToHex(b));
//		System.out.println("c piece "+AESCrypto.bytesToHex(c));

		return c;
	}

	public String mergeFile(String fileName,RecordXOR fileTaskRecordXOR) {// TODO: 2021/10/26 根据文件xor指针开始解异或 //合并文件之前先XOR解密！
		int tempCount = this.filePieceContentMap.size();
		Log.d(TAG, " mergeFile:: fileName = " + fileName);
		Log.d(TAG, " mergeFile:: tempCount = " + tempCount);
		RandomAccessFile raf = null;
		try {
			File receiveFile = new File(FilePathUtils.RECIEVE_FILE_PATH + fileName);//保存的位置
			raf = new RandomAccessFile(receiveFile, "rw");

//			File allXORFolder = new File(Constant.XOR_FILE_PATH);//文件夹，内包含多个拆分的XOR文件
//			File[] allXORFiles = allXORFolder.listFiles();//多个拆分的XOR文件
//
//			List< File> allXORFileList = Message.sortFile(Arrays.asList(allXORFiles));//文件的顺序有问题的，要按数字大小排
//
//			int startFileName = fileTaskRecordXOR.getStartFileName();//从这个文件开始用
//			int startFileIndex = fileTaskRecordXOR.getStartFileIndex();//从这个文件这里开始用
//
//			allXORFileList = Message.delectAllUsedFiles(allXORFileList,startFileName);// TODO: 2021/10/4 得到以 startFileName开始的allXORFileList
//
//			FileInputStream fileInputStream =new FileInputStream(allXORFileList.get(0));
//			int fileStart = fileInputStream.available()-startFileIndex;//endFileIndex是到文件尾的距离，真实用的时候是从头部开始的，这里就是此时文件头往文件尾有fileStart距离的位置开始用
//			fileInputStream.close();
//
//			int[] fileStartIndex = {fileStart};// TODO: 2021/10/4 从endFileIndex开始用 //记录每一次发送文件时，里面的每一轮分片进行xor文件的起始点//优化的地方，用数组才能保存和更新数值
//
//			long offSet = 0L;
//			RecordXOR recordXOR = new RecordXOR();// TODO: 2021/10/25 临时变量，存储每一次分片的指针

			for (int i = 0; i < tempCount; i++) {
				byte[] data = this.filePieceContentMap.get(i);//未解密的

				//data 1976   c 1968
				byte[] c = JniEntryUtils.decry(data);

				raf.write(c, 0, c.length);
			}
			raf.close();
			byte[] contentHash =FileUtil.getFileSHA1(receiveFile);
			Log.d(TAG, " mergeFile:: targetconentHash = " + AESCrypto.bytesToHex(this.contentHash));
			Log.d(TAG, " mergeFile:: merge file content hash = " + AESCrypto.bytesToHex(contentHash));

			if(Arrays.equals(this.contentHash, contentHash)) {
				Log.d(TAG, " mergeFile:: file recieve successfully path = " + receiveFile.getAbsolutePath() );
				return receiveFile.getAbsolutePath();
			}else {
				Log.d(TAG, " mergeFile:: file hash not matched" );
			}

		} catch (Exception e) {
			Log.d(TAG, " mergeFile:: FileTask mergeFile error " + e.getMessage());
		}
		return null;
	}

	public String getFileFullPath() {
		return fileFullPath;
	}

	public void setFileFullPath(String fileFullPath) {
		this.fileFullPath = fileFullPath;
	}

	@Override
	public String toString() {
		return "FileTask{" +
				"fileName='" + fileName + '\'' +
				", fileNameHash=" + Arrays.toString(fileNameHash) +
				", direction=" + direction +
				", fileFullPath='" + fileFullPath + '\'' +
				", totalSize=" + totalSize +
				", contentHash=" + Arrays.toString(contentHash) +
				", fileType=" + fileType +
				", pieceSize=" + pieceSize +
				", totalPieceNumber=" + totalPieceNumber +
				", status=" + status +
				", startTime=" + startTime +
				", endTime=" + endTime +
				", count=" + count +
				", speed=" + speed +
				", percent=" + percent +
				", stepNumber=" + stepNumber +
				", onionName='" + onionName + '\'' +
				", messageID='" + messageID + '\'' +
				", fileTransferStatusMap=" + fileTransferStatusMap +
				", filePieceContentMap=" + filePieceContentMap +
				", filePieceHashMap=" + filePieceHashMap +
				", df=" + df +
				", filePieceRecordXORMap=" + filePieceRecordXORMap +
				", fileTaskRecordXOR=" + fileTaskRecordXOR +
				'}';
	}
}
