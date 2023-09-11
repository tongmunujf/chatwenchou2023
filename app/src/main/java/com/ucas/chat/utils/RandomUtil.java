package com.ucas.chat.utils;

import java.util.Random;

/**
 * @auther :haoyunlai
 * date         :2021/8/8 19:21
 * e-mail       :2931945387@qq.com
 * usefulness   :生成(毫秒)时间+随机数的随机数
 */
public class RandomUtil {


    private static final Random random = new Random();
//    private static char[] chars = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

    private static char[] headchars = new char[]{'1', '2', '3', '4', '5', '6', '7', '8', '9'};//先生成最左边的数字
    private static char[] chars = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};//右边的其他数字

    public RandomUtil() {
    }

    public static Random getRandom() {
        return random;
    }

    public static String randomChar(int len) {
//        String nowTime = System.currentTimeMillis()+"";//当前收发的时间

        StringBuilder sb = new StringBuilder();
//        sb.append(nowTime);

        for(int i = 0; i < len; ++i) {
            sb.append(chars[random.nextInt(chars.length)]);
        }

        return sb.toString();
    }

    public static String randomChar() {

        int max=7;
        int min=4;
        Random random = new Random();
        int len = random.nextInt(max)%(max-min+1) + min;//随机数的总长度// 生成一个范围的随机数,如：[min,max]

        String head = String.valueOf(headchars[random.nextInt(headchars.length)]);//生成该随机数的最左边的一位
        String randomNumber = head +randomChar(len-1);//组合成最终的随机数

        return randomNumber;// TODO: 2021/8/24 因为消息id 只能存4个字节，每个字节对应一位字符
    }


    public static String randomChar(String time,int len) {
        StringBuilder sb = new StringBuilder();
        sb.append(time);

        for(int i = 0; i < len; ++i) {
            sb.append(chars[random.nextInt(chars.length)]);
        }

        return sb.toString();//以时间开头的随机数
    }



    public static String randomChar(String time) {
        return randomChar(time,8);
    }



}