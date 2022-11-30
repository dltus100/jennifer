package com.mzc;

import com.aries.extension.data.TransactionData;
import com.aries.extension.handler.TransactionHandler;
import com.aries.extension.util.PropertyUtil;

public class TransactionAdapter implements TransactionHandler {
    @Override
    public void on(TransactionData[] transactions) {
        System.out.println("[TransactionAdapter] - " +
                PropertyUtil.getValue("transaction_adapter", "subject", "Unknown subject"));

        for(TransactionData data : transactions) {
            System.out.println("Domain ID : " + data.domainId);
            System.out.println("Instance Name : " + data.instanceName);
            System.out.println("Transaction ID : " + data.txid);
            System.out.println("Response Time : " + data.responseTime);
            System.out.println("Application : " + data.applicationName + "\n");
        }
    }
}
