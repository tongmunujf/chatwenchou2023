package com.ucas.chat.tor.util;

import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;

import com.google.android.material.tabs.TabLayout;
import com.ucas.chat.bean.contact.ConstantValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.apaches.commons.codec.digest.DigestUtils;

public class FileUtil {
	private static String TAG = ConstantValue.TAG_CHAT + "FileUtil";

	/**
	 * splite file into pieces according to the specific piecesize
	 * @param file
	 * @param pieceSize
	 * @param filePieceContentMap
	 */
	public static void cutFile(RandomAccessFile file, int pieceSize,
			ConcurrentHashMap<Integer, byte[]> filePieceContentMap,ConcurrentHashMap<Integer, byte[]> filePieceHashMap) {
		try {
			long totalSize = file.length();
			int totalPieceNumber = (int) Math.ceil(totalSize / pieceSize);

			long offSet = 0L;

			for (int i = 0; i < totalPieceNumber - 1; i++) {
				long begin = offSet;
				file.seek(begin);
				byte[] b = new byte[pieceSize];
				file.read(b);
				filePieceContentMap.put(i, b);
				filePieceHashMap.put(i, DigestUtils.sha1(b));
				offSet = file.getFilePointer();
			}
			if (totalSize - offSet > 0) {
				byte[] b = new byte[pieceSize];
				int n = file.read(b);
				System.out.println(n);
				System.out.println(b.length);
				byte[] t = new byte[n];
				System.arraycopy(b, 0, t, 0, n);
				filePieceContentMap.put(totalPieceNumber - 1, t);
			}
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("cut file error " + e.getMessage());
		}

	}
	
    /**
     * 
     *
     * @param file
     * @return
     */
    public static byte[] getFileSHA1(File file) {
    	 FileInputStream in = null;
        try {
        	in = new FileInputStream(file);
            MessageDigest md = MessageDigest.getInstance("SHA-1");//MessageDigest 类为应用程序提供信息摘要算法的功能https://www.cnblogs.com/lxnlxn/p/9993969.html

            byte[] buffer = new byte[1024];
            int length = -1;
            while ((length = in.read(buffer, 0, 1024)) != -1) {
                md.update(buffer, 0, length);//处理数据
            }
            in.close();
            return md.digest();//获得密文完成哈希计算,产生160 位的长整数？,相当于文件加密了
        } catch (Exception e) {
//            e.printStackTrace();
            System.out.println(e.getMessage());
            return null;
        }
    }


	public static byte[] getFileSHA1_2(byte[] bitmapBytes) {
//		FileInputStream in = null;
		try {
//			in = new FileInputStream(file);
			MessageDigest md = MessageDigest.getInstance("SHA-1");//MessageDigest 类为应用程序提供信息摘要算法的功能https://www.cnblogs.com/lxnlxn/p/9993969.html

			byte[] buffer = new byte[1024];
			int length = -1;

//			for (int i=0;i<bitmapBytes.length;i=i+1024 ){
//				buffer = Arrays.copyOfRange(bitmapBytes,i,i+1024);
//				md.update(buffer, 0, length);//处理数据
//			}

			md.update(bitmapBytes, 0, bitmapBytes.length);//处理数据

//			while ((length = in.read(buffer, 0, 1024)) != -1) {
//				md.update(buffer, 0, length);//处理数据
//			}
//			in.close();
			return md.digest();//获得密文完成哈希计算,产生160 位的长整数？,相当于文件加密了
		} catch (Exception e) {
//            e.printStackTrace();
			System.out.println(e.getMessage());
			return null;
		}
	}


	/**
	 *
	 * @param binName jpg_1.bin
	 * @return
	 */
	public static String getTargetFilePath(String binName){
		Log.d(TAG, " getTargetFilePath:: binName = " + binName);
		String[] binNameArr = binName.split("_");
		String fileExtension = binNameArr[0];
		Log.d(TAG, " getTargetFilePath:: fileExtension = " + fileExtension);

		String targetFilePath = generateTargetFile(fileExtension);
		Log.d(TAG, " getTargetFilePath:: targetFilePath = " + targetFilePath);
		return targetFilePath;
	}

	/**
	 * 获取文件扩展名
	 * @param sourceFilePath
	 * @return
	 */
	public static String getFileExtension(String sourceFilePath){
		String fileExtension = null;
		fileExtension = sourceFilePath.substring(sourceFilePath.lastIndexOf("."));
		return fileExtension.substring(1);
	}

	/**
	 * 创建 /data/data/com.ucas.chat/files/binFile
	 */
	private static void createBinFileFolder(){
		//  /data/data/com.ucas.chat/files/binFile
		File binFilePath = new File(FilePathUtils.BIN_FILE_PATH);
		if (!binFilePath.exists()){
			binFilePath.mkdirs();
			Log.d(TAG," createBinFileFolder:: " + " 新建 /files/binFiles 成功");
		}else {
			Log.d(TAG," createBinFileFolder:: " + "/files/binFiles 已存在");
		}
	}

	/**
	 * 创建扩展名文件夹
	 * 例如：
	 * /data/data/com.ucas.chat/files/binFile/MP4
	 * /data/data/com.ucas.chat/files/binFile/txt
	 *
	 * @param fileExtension 文件扩展名 如xxxx.MP4
	 */
	private static File createExtensionFolder(String basePath, String fileExtension){
		Log.d(TAG, " createExtensionFolder:: basePath = " + basePath);
		Log.d(TAG, " createExtensionFolder:: fileExtension = " + fileExtension);
		String extensionPath = basePath + "/" + fileExtension;
		File extensionPathFile = new File(extensionPath);
		if (!extensionPathFile.exists()){
			extensionPathFile.mkdirs();
		}
		return extensionPathFile;
	}

	/**
	 *  获取folder目录下的所有文件夹
	 *  找出与 fileExtension 名字相同的文件夹
	 * @param folderPath
	 * @param fileExtension 文件扩展名，如xxxx.MP4
	 * @return
	 */
	public static String traverseBinFileFolder(String folderPath, String fileExtension){
		String folderName = null;
		File binFolder = new File(folderPath);
		Log.d(TAG, " traverseBinFileFolder:: binFolder = " + binFolder.getAbsolutePath());
		File[] files = binFolder.listFiles();
		for (File folder: files){
			if (folder.isDirectory()){
				if (folder.getName().equals(fileExtension)){
					folderName = folder.getName();
					break;
				}
			}
		}
		Log.d(TAG, " traverseBinFileFolder:: folderName = " + folderName);
		return folderName;
	}

	/**
	 * 创建相应文件的bin文件
	 * 比如MP4_1.bin
	 *
	 * @param sourceFilePath  源文件如sdcard/DCIM/Camera/xxxxxxxx.MP4
	 * @throws IOException
	 */
   public static String createBinFile(String sourceFilePath){
		Log.d(TAG, " createBinFile:: sourceFilePath = " + sourceFilePath);

		//获取xxxxxxxx.MP4 文件的扩展名 .MP4
		String fileExtension = getFileExtension(sourceFilePath);

		//  创建/data/data/com.ucas.chat/files/binFile
	    createBinFileFolder();

		//根据源文件sourceFilePath扩展名 创建相应的文件夹，如 /data/data/com.ucas.chat/files/binFile/.MAP4
	   File extensionPathFile = createExtensionFolder( FilePathUtils.BIN_FILE_PATH, fileExtension);

       //遍历 /files/binFiles 下的所有扩展名文件夹，如 /files/binFiles/MP4  /files/binFiles/txt
	   String targetFolderName = traverseBinFileFolder(FilePathUtils.BIN_FILE_PATH, fileExtension);

		// 如果targetFolderName = MP4， 那么targetFolder = /files/binFiles/MP4
		String targetFolder = FilePathUtils.BIN_FILE_PATH + "/" + targetFolderName;
		Log.d(TAG, " createBinFile:: " + " 新生成的.bin文件 存放的目录targetFolder = " + targetFolder);

		//生成新的.bin文件，用于存储加密文件信息
	   String binName = generateBinFile(fileExtension, extensionPathFile, targetFolder);
	   return binName;

   }

   private static void createNewFileName(String binFilePath){
	   File binFile = new File(binFilePath);
	   try {
		   binFile.createNewFile();
	   } catch (IOException e) {
		   throw new RuntimeException(e);
	   }
   }

	public static String getCurrentNum(List<Integer> list) {
		int max = list.get(0);
		for (int i = 1; i < list.size(); i++) {
			if (list.get(i) > max) {
				max = list.get(i);
			}
		}
		int num = max+1;
		String currentNum = num + "";
		Log.d(TAG, " getCurrentNum:: currentNum = " + currentNum);
		return currentNum;
	}

	/**
	 *
	 * @param fileExtension  扩展名
	 * @param extensionPathFile 扩展名路径，如/data/data/com.ucas.chat/files/binFile/MP4
	 * @param targetFolder 即将生产的.bin 存放的路径， 如MP4
	 */
	private static String generateBinFile(String fileExtension, File extensionPathFile, String targetFolder){
		ArrayList<Integer> numList = new ArrayList<>();
		//MP4_5.bin
		String newBinFileName = fileExtension + "_1" + ".bin";

		File targetFile = new File(targetFolder);
		File[] files = targetFile.listFiles();
		if (files.length == 0){
			String binFilePath = extensionPathFile + "/" + newBinFileName;

			createNewFileName(binFilePath);
		}else {
			for (File fileName: files){
				//fileName举例，MP4_1.bin
				String split[] = fileName.getName().split("_");
				//1.bin
				String lastSplit = split[1];
				String[] num = lastSplit.split("\\.");
				numList.add(Integer.parseInt(num[0]));
				Log.d(TAG, " generateBinFile:: " + " 已存在的num = " + num[0]);
			}
			String currentNum = getCurrentNum(numList);
			newBinFileName = fileExtension + "_" + currentNum + ".bin";

			String binFilePath = extensionPathFile + "/" + newBinFileName;
			createNewFileName(binFilePath);
		}

		return newBinFileName;
	}

	private static String generateTargetFile(String fileExtension){
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HH_mm_ss");
		String time = simpleDateFormat.format(date);
		Log.d(TAG, " generateTargetFile:: time = " + time);
		String newTargetFileName = fileExtension + "_" + time + "." + fileExtension;

		Log.d(TAG, " generateTargetFile:: newTargetFileName " + newTargetFileName);
		return newTargetFileName;
	}


	/**
	 * 创建 /data/data/com.ucas.chat/files/ReceiveFile
	 */
	public static void createReceiveFileFolder(){
		File file = new File(FilePathUtils.RECIEVE_FILE_PATH);
		if (!file.exists()){
			file.mkdirs();
		}
	}


	public static void createUserInfoFile(){
		File sdcardChatFile = new File(FilePathUtils.SDCARD_CHAT);
		if (!sdcardChatFile.exists()){
			sdcardChatFile.mkdirs();
		}
		File userFile = new File(FilePathUtils.USER_INFO_FILE);
		if (!userFile.exists()){
			userFile.mkdirs();
		}
	}

	public static String readFileFromSdcardChatUser(String fileName){
		String content="";
		String file = FilePathUtils.USER_INFO_FILE + "/" + fileName;
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);
			byte[] buf = new byte[inputStream.available()];
			inputStream.read(buf);
			content = new String(buf,"utf-8");// TODO: 2021/7/13 utf-8格式
			inputStream.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Log.d(TAG, " readFileFromSdcardChatUser content = " + content);
		return content;
	}
}
