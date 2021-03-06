package com.cip.crane.common.alert;

import com.cip.crane.common.lion.ConfigHolder;
import com.cip.crane.common.lion.LionKeys;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by kirinli on 14/12/17.
 */
public class WeChatHelper {

    public static void sendWeChat(String user, String content, String agentid) {

        if (StringUtils.isBlank(user) || StringUtils.isBlank(content)) {
            System.out.println("SendWechat error! user or content can't be blank!");
            return;
        }

        String wechat_url = ConfigHolder.get(LionKeys.WECHAT_API.value(), "http://core.dp:8080");

        String wechat_api = wechat_url + "/api";

        for (String admin : user.split(",")) {
            String params = "action=push&sysName=ezc&keyword=" + admin.trim()
                    + "&title=Taurus 微信告警服务&content= " + content.trim() + "&agentid=" + agentid;

            String resp = sendPost(wechat_api, params);

            System.out.println(resp);
        }

    }

    public static void sendWeChat(String user, String content, String title, String agentid) {

        if (StringUtils.isBlank(user) || StringUtils.isBlank(content)) {
            System.out.println("SendWechat error! user or content can't be blank!");
            return;
        }

        String wechat_url = ConfigHolder.get(LionKeys.WECHAT_API.value(), "http://core.dp:8080");

        String wechat_api = wechat_url + "/api";

        for (String admin : user.split(",")) {
            String params = "action=push&sysName=ezc&keyword=" + admin.trim()
                    + "&title=" + title.trim() + "&content=" + content.trim() + "&agentid=" + agentid;

            sendPost(wechat_api, params);
        }


    }

    public static String sendPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！" + e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

}
