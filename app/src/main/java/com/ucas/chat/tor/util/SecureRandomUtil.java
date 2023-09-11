package com.ucas.chat.tor.util;

import java.security.SecureRandom;

public class SecureRandomUtil {
	
	public static SecureRandom random = new SecureRandom();

	public static String getRandom(int length) {
		StringBuilder ret = new StringBuilder();
		for (int i = 0; i < length; i++) {
			boolean isChar = (random.nextInt(2) % 2 == 0);// �����ĸ��������
			if (isChar) { // �ַ���
				int choice = random.nextInt(2) % 2 == 0 ? 65 : 97; // ȡ�ô�д��ĸ����Сд��ĸ
				ret.append((char) (choice + random.nextInt(26)));
			} else { // ����
				ret.append(random.nextInt(10));
			}
		}
		return ret.toString();
	}
}
