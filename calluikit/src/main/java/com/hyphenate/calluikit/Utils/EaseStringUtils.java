package com.hyphenate.calluikit.Utils;

import java.util.Random;

public class EaseStringUtils {

    /**
     * length用户要求产生字符串的长度，随机生成会议密码
     * @param length
     * @return
     */
    static public String getRandomString(int length){
        String str="abcdefghijklmnopqrstuvwxyz";
        Random random=new Random();
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<length;i++){
            int number=random.nextInt(26);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }
}
