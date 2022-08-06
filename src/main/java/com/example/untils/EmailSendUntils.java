package com.example.untils;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

/**
 * 邮箱验证工具类
 */
public class EmailSendUntils {
    //addressees:收件人 subject：主题
    public static void setEmailCode(String emailType,String addressee ,String subject,String msg) {
        try {
            HtmlEmail email=new HtmlEmail();//创建一个HtmlEmail实例对象
            email.setHostName(emailType);//邮箱的SMTP服务器，一般123邮箱的是smtp.123.com,qq邮箱为smtp.qq.com
            email.setCharset("utf-8");//设置发送的字符类型
            email.addTo(addressee);//设置收件人
            email.setFrom("490660221@qq.com","aa");//发送人的邮箱为自己的，用户名可以随便填
            email.setAuthentication("490660221@qq.com","okrveowepqwgbhgg");//设置发送人到的邮箱和用户名和授权码(授权码是自己设置的)
            email.setSubject(subject);//设置发送主题
            email.setMsg(msg);//设置发送内容
            email.send();//进行发送
        } catch (EmailException e) {
            e.printStackTrace();
        }

    }
}
