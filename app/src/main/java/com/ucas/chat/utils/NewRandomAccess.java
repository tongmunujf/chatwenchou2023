package com.ucas.chat.utils;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Date;

public class NewRandomAccess {

	public  static void resetFile(String filePath,long taregetLength) {
		
		try {
		File f = new File(filePath);
		RandomAccessFile file = new RandomAccessFile(f, "rwd");
		
		long totalSize = file.length();
		System.out.println("置totalSize "+totalSize);
			System.out.println("置taregetLength "+taregetLength);
		if(taregetLength>totalSize)
			taregetLength=totalSize;
		int pieceSize=2048;
		int targetPieceNumber = (int) Math.floor(taregetLength / pieceSize);
		if (taregetLength % pieceSize != 0) {
			targetPieceNumber = targetPieceNumber + 1;
		}
		long offSet =0L;
		byte[] target = new byte[pieceSize];
		for (int i = 0; i < targetPieceNumber - 1; i++) {
			long begin = offSet;
			file.seek(begin);//���ļ��α��ƶ����ļ���beginλ��,
			file.write(target);
			
			System.out.println("the "+i+" piece");
			offSet = file.getFilePointer();
		}
		
		if (taregetLength - offSet > 0) {//����ʣ��!
			int left=(int)(taregetLength - offSet);
			byte[] b = new byte[left];
//			int n = file.read(b);
//			System.out.println(n);
//			System.out.println(b.length);
//			byte[] t = new byte[n];
//			System.arraycopy(b, 0, t, 0, n);
			System.out.println("the last piece");
//			byte[] x= new byte[n];
			
			file.write(b);
//			offSet = file.getFilePointer();
			System.out.println("offset "+offSet);
		}
		System.out.println((file.length()));
		file.close();
		
	}catch (Exception e) {
		e.printStackTrace();
		System.out.println("cut file error " + e.getMessage());
	}
	}
	
	public static void main(String[] args) {
		System.out.println(new Date());
		String filePath="C:\\Users\\happywindy\\Desktop\\auth.log.1";
		filePath="C:\\Download\\sogou_pinyin_112b.exe";
		long taregetLength=76895056;
		NewRandomAccess.resetFile(filePath, taregetLength);
		System.out.println(new Date());
		
	}
	
}
