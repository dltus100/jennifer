package com.mzc.snc.jennifer5;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONObject;

import com.aries.extension.data.EventData;
import com.aries.extension.handler.EventHandler;
import com.aries.extension.util.PropertyUtil;
//import com.aries.view.extension.data.InstanceInfo;

public class ServiceNowEventAdapter implements EventHandler{
    private String tokenFile;
    private String logPath;
    private String conUrl;
    public ServiceNowEventAdapter(){
        tokenFile = PropertyUtil.getValue("sms", "tokenFile","error");
        logPath = PropertyUtil.getValue("sms", "logFile","error");
        conUrl = PropertyUtil.getValue("sms", "conUrl","error");

    }

    public void on(EventData[] events){
        try{
            JSONObject job = new JSONObject();

            String code = "";
            String log = "";
            for(int i=0; i<events.length;i++){
                log = "";
                EventData data = (EventData) events[i];
                job = makeMsg(data);
                code = sendSms(job);
                log = makeLog(job, code);
                writeLogFile(log, logPath);
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }


    public String sendSms(JSONObject jo){
        String returnMsg = "";
        BufferedWriter bw = null;
        HttpURLConnection hCon = null;

        try{
            String cToken = getToken(tokenFile);

            //String cmpURL = "http://"+ conUrl + "/api/v1/log-proc/log";
            String cmpURL = "http://"+ conUrl + "/api/mid/em/jsonv2";
            URL smsUrl = new URL(cmpURL);
            hCon = (HttpURLConnection)smsUrl.openConnection();
            hCon.setRequestMethod("POST");
            hCon.setRequestProperty("accept", "*/*");
            hCon.setRequestProperty("Authorization", cToken);
            hCon.setRequestProperty("Content-Type", "application/json");
            hCon.setDoOutput(true);

            bw = new BufferedWriter(new OutputStreamWriter(hCon.getOutputStream()));
            bw.write(jo.toString());
            bw.flush();

            BufferedReader in = new BufferedReader(new InputStreamReader(hCon.getInputStream()));
            String msg = in.readLine();

            int responseCode = hCon.getResponseCode();
            returnMsg = String.valueOf(responseCode) + " : " + msg;
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                bw.close();
                hCon.disconnect();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return returnMsg;
    }

    public JSONObject makeMsg(EventData ev){
        JSONObject jsob = new JSONObject();
        try{
            String apmMessage = "";
            if(ev.errorType.equals("Metrics EVENT")){
                apmMessage = new StringBuffer().append("[")
                        .append(ev.eventLevel)
                        .append("] ")
                        .append(ev.metricsName)
                        .append(" : ")
                        .append(ev.domainName)
                        .append(", ")
                        .append(ev.instanceName).toString();

            }else{
                apmMessage = new StringBuffer().append(" [")
                        .append(ev.eventLevel)
                        .append("] ")
                        .append(ev.errorType)
                        .append(" : ")
                        .append(ev.domainName)
                        .append(", ")
                        .append(ev.instanceName).toString();
            }
            jsob.put("systemType", "APM");
            jsob.put("severityLevel", changeCode(ev.eventLevel));
            jsob.put("eventDt",getEventTime(ev.time));
            jsob.put("messages", apmMessage);
            jsob.put("targetName",ev.instanceName);
            jsob.put("targetId",String.valueOf(ev.instanceId));
            jsob.put("credentialId","5370eb15-0e4c-4e62-a14b-5f2a48162f64");
        }catch(Exception e){
            e.printStackTrace();
        }
        return jsob;

    }

    public String makeLog(JSONObject jo, String code){
        String logText = "";
        try{
            String st = (String)jo.get("systemType");
            String sl = (String)jo.get("severityLevel");
            String ed = (String)jo.get("eventDt");
            String ms = (String)jo.get("messages");
            String tn = (String)jo.get("targetName");
            String ti = (String)jo.get("targetId");
            String ci = (String)jo.get("credentialId");
            logText = new StringBuffer().append(ed)
                    .append(" | ")
                    .append(st)
                    .append(" | ")
                    .append(sl)
                    .append(" | ")
                    .append(ed)
                    .append(ms)
                    .append(" | ")
                    .append(tn)
                    .append(" | ")
                    .append(ti)
                    .append(" | ")
                    .append(ci)
                    .append(" | ")
                    .append(code).toString();
        }catch(Exception e){
            e.printStackTrace();
        }
        return logText;
    }


    public String getEventTime(long date) {
        SimpleDateFormat sdfTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdfTime.format(new Date(date));
        return time;
    }

    public String getToken(String path){
        String t = "";
        BufferedReader br = null;
        try{
            br = new BufferedReader(new FileReader(path));
            if(br != null)
                t = br.readLine();

        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                br.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return t;
    }

    public String changeCode(String eventLevel){
        String eventCode = "";
        switch (eventLevel){
            case "FATAL" : eventCode = "1";
                break;
            case "WARNING" : eventCode = "4";
                break;
            case "NORMAL" : eventCode = "0";
                break;
        }
        return eventCode;
    }

    public void writeLogFile(String alertMsg, String filePath){
        try{
            SimpleDateFormat sdfTime = new SimpleDateFormat("yyyyMMdd");
            String lTime = sdfTime.format(new Date().getTime());
            String path = filePath + "/jennifer_sms_"+lTime+".log";
            String linesp = System.getProperty("line.separator");
            FileWriter fw = new FileWriter(path,true);
            fw.write(alertMsg);
            fw.write(linesp);
            fw.close();
        }catch(Exception e){
            e.printStackTrace();
        }

    }

}
