package com.mzc;


import com.aries.extension.data.EventData;
import com.aries.extension.handler.EventHandler;
import com.aries.extension.util.PropertyUtil;

public class EventAdapter implements EventHandler {
    @Override
    public void on(EventData[] events) {
        System.out.println("[EventAdapter] - " +
                PropertyUtil.getValue("event_adapter", "subject", "Unknown subject"));

        for(EventData data : events) {
            System.out.println("Domain ID : " + data.domainId);
            System.out.println("Instance Name : " + data.instanceName);
            System.out.println("Transaction ID : " + data.txid);
            System.out.println("Service Name : " + data.serviceName);
            System.out.println("Error Type : " + data.errorType);
            System.out.println("Event Level : " + data.eventLevel + "\n");
        }
    }
}
