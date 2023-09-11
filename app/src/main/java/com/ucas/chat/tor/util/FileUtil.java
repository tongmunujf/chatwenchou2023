package com.ucas.chat.tor.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import org.apaches.commons.codec.digest.DigestUtils;

public class FileUtil {

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
 * @param fileName
 * @param filePieceContentMap
 * @param contentHash
 */
	public static void mergeFile(String fileName, ConcurrentHashMap<Integer, byte[]> filePieceContentMap,
			byte[] contentHash) {
		int tempCount = filePieceContentMap.size();
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(new File(Constant.RECIEVE_FILE_PATH+fileName), "rw");
			for (int i = 0; i < tempCount; i++) {
				byte[] data = filePieceContentMap.get(i);
				raf.write(data, 0, data.length);
			}
			
			raf.close();
		} catch (Exception e) {
			e.printStackTrace();
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

   

}
