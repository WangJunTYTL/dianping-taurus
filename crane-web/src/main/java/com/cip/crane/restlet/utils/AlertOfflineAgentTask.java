package com.cip.crane.restlet.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.TimerTask;

import com.cip.crane.common.alert.MailHelper;
import com.cip.crane.common.alert.OpsAlarmHelper;
import com.cip.crane.common.alert.WeChatHelper;
import com.cip.crane.common.lion.ConfigHolder;
import com.cip.crane.common.lion.LionKeys;
import com.cip.crane.restlet.resource.IAllHosts;
import com.cip.crane.restlet.resource.IExceptionHosts;
import org.apache.commons.lang.StringUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.restlet.data.MediaType;
import org.restlet.resource.ClientResource;

import com.dianping.cat.Cat;
import com.dianping.lion.EnvZooKeeperConfig;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.LionException;

/**
 * Created by kirinli on 15/1/30.
 */
public class AlertOfflineAgentTask  extends TimerTask {
    
    public void run() {
        
        // 监测异常的Agent 告警
        ClientResource cr = new ClientResource(LionConfigUtil.RESTLET_API_BASE + "exceptionhosts");
        IExceptionHosts hostsResource = cr.wrap(IExceptionHosts.class);
        cr.accept(MediaType.APPLICATION_XML);
        String exceptionHosts = hostsResource.retrieve();
        
        // 检测正常Agent 发现异常 告警
        cr = new ClientResource(LionConfigUtil.RESTLET_API_BASE + "allhosts");
        IAllHosts allOnlineHostsResource = cr.wrap(IAllHosts.class);
        cr.accept(MediaType.APPLICATION_XML);
        String onlineHostsJsonStr = allOnlineHostsResource.retrieve();
        
        String domain = null;
        String reportToOps = null;
        OpsAlarmHelper oaHelper = new OpsAlarmHelper();
        try {
            domain = ConfigCache.getInstance(EnvZooKeeperConfig.getZKAddress()).getProperty("taurus.web.serverName");
            reportToOps = ConfigCache.getInstance(EnvZooKeeperConfig.getZKAddress()).getProperty("taurus.agent.down.ops.report.alarm.post");
        } catch (LionException e) {
            domain="http://taurus.dp";
            reportToOps = "http://pulse.dp/report/alarm/post";
            e.printStackTrace();
        }
        
        
        // 检测异常agent:isOnline = 1 and isConnected = 0
        if (StringUtils.isNotBlank(exceptionHosts)){
            String[] hostLists = exceptionHosts.split(",");

            for (String host: hostLists){

                String context = "您好，taurus-agent的job主机 ["
                        + host
                        + "] 心跳异常请在【"+domain+"/host_center】核实" +
                        "监控连接如下："+domain+"/hosts?hostName="
                        + host
                        +"，谢谢~";
                String exceptContext = "您好，taurus-agent的job主机 ["
                        + host
                        + "] 服务已经挂掉请在【"+domain+"/host_center】核实并重启该Job机器的TOMCAT" +
                        "监控连接如下："+domain+"/hosts?hostName="
                        + host
                        +"，谢谢~";


                try {


                    String url1= "http://"+host+":8080/agentrest.do?action=isnew";
                    String url2 = "http://"+host+":8088/agentrest.do?action=isnew";
                    String isAlive1 = get_data(url1);
                    String isAlive2 = get_data(url2);

                    if ((isAlive1!= null && isAlive1.equals("true"))||(isAlive2!= null && isAlive2.equals("true"))){
                    	String admin = ConfigHolder.get(LionKeys.ADMIN_USER);
                    	MailHelper.sendMail(admin + "@dianping.com", context, "Taurus-Agent主机心跳异常告警服务");
                    	WeChatHelper.sendWeChat(admin, context, "Taurus-Agent主机心跳异常告警服务", ConfigHolder.get(LionKeys.ADMIN_WECHAT_AGENTID));
                        
                        oaHelper.buildTypeObject("Taurus")
								.buildTypeItem("Service")
								.buildTypeAttribute("Status")
								.buildSource("taurus")
								.buildDomain(host)
								.buildTitle("Taurus-Agent主机心跳异常告警服务")
								.buildContent(context)
								.buildUrl(domain + "/hosts?hostName=" + host)
								.buildReceiver("dpop@dianping.com")
								.sendAlarmPost(reportToOps);
                    }else
                    {
                    	String admin = ConfigHolder.get(LionKeys.ADMIN_USER);
                        WeChatHelper.sendWeChat(admin,exceptContext, "Taurus-Agent主机失联系告警服务", ConfigHolder.get(LionKeys.ADMIN_WECHAT_AGENTID));
                        String toMails = ConfigHolder.get(LionKeys.AGENT_DOWN_MAIL_TO);
                        String [] toLists = toMails.split(",");
                        for (String to:toLists){
                            MailHelper.sendMail(to, exceptContext, "Taurus-Agent主机失联系告警服务");
                        }

                        oaHelper.buildTypeObject("Taurus")
								.buildTypeItem("Service")
								.buildTypeAttribute("Status")
								.buildSource("taurus")
								.buildDomain(host)
								.buildTitle("Taurus-Agent主机失联系告警服务")
								.buildContent(exceptContext)
								.buildUrl(domain + "/hosts?hostName=" + host)
								.buildReceiver("monitor@dianping.com")
								.sendAlarmPost(reportToOps);
                    }

                } catch (Exception e) {
                    Cat.logError(e);
                }
            }
        }
        
        // 检测其他agent:isOnline = 1 and isConnected = 1
        JSONObject jsonObj = JSONObject.fromObject(onlineHostsJsonStr);
        Object obj = jsonObj.get("hosts");
        
        if(StringUtils.isNotBlank(obj.toString())){
        	JSONArray jsonArr = jsonObj.getJSONArray("hosts");
			Object[] hostListsObj = jsonArr.toArray();
			
            for (Object hostObj: hostListsObj){
            	String host = hostObj.toString();
                String exceptContext = "您好，taurus-agent的job主机 ["
                        + host
                        + "] 服务已经挂掉请在【"+domain+"/host_center】核实并重启该Job机器的TOMCAT" +
                        "监控连接如下："+domain+"/hosts?hostName="
                        + host
                        +"，谢谢~";

                try {

                    String url1= "http://"+host+":8080/agentrest.do?action=isnew";
                    String url2 = "http://"+host+":8088/agentrest.do?action=isnew";
                    String isAlive1 = get_data(url1);
                    String isAlive2 = get_data(url2);

                    if(isAlive1 == null && isAlive2 == null) {//agent服务器无响应
                    	String admin = ConfigHolder.get(LionKeys.ADMIN_USER);
                    	WeChatHelper.sendWeChat(admin,exceptContext, "Taurus-Agent主机失联系告警服务", ConfigHolder.get(LionKeys.ADMIN_WECHAT_AGENTID));
                        String toMails = ConfigHolder.get(LionKeys.AGENT_DOWN_MAIL_TO);
                        String [] toLists = toMails.split(",");
                        for (String to:toLists){
                            MailHelper.sendMail(to, exceptContext, "Taurus-Agent主机失联系告警服务");
                        }
                        
                        oaHelper.buildTypeObject("Taurus")
								.buildTypeItem("Service")
								.buildTypeAttribute("Status")
								.buildSource("taurus")
								.buildDomain(host)
								.buildTitle("Taurus-Agent主机失联系告警服务")
								.buildContent(exceptContext)
								.buildUrl(domain + "/hosts?hostName=" + host)
								.buildReceiver("monitor@dianping.com")
								.sendAlarmPost(reportToOps);
                    }

                } catch (Exception e) {
                    Cat.logError(e);
                }
            }
        }
        
        
        
    }
    
	public static String get_data(String url) {
	    try {
	        URL httpUrl = new URL(url);
	        HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
	        conn.setDoOutput(true);
	        conn.setConnectTimeout(1000);
	        conn.connect();
	
	        //返回
	        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	        String result = reader.readLine();
	        return result.trim();
	    } catch (Exception e) {
	
	        return null;
	    }
	}
	
}
