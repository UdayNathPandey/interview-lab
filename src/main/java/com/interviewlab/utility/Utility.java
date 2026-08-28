package com.interviewlab.utility;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class Utility {

    public void printTransactionInfo(String point) {

        System.out.println("\n========== " + point + " ==========");

        System.out.println(
                "Thread = "
                        + Thread.currentThread().getName()
        );

        System.out.println(
                "Transaction Active = "
                        + TransactionSynchronizationManager
                        .isActualTransactionActive()
        );

        System.out.println(
                "Synchronization Active = "
                        + TransactionSynchronizationManager
                        .isSynchronizationActive()
        );

        System.out.println(
                "Transaction Name = "
                        + TransactionSynchronizationManager
                        .getCurrentTransactionName()
        );

        System.out.println(
                "Resources = "
                        + TransactionSynchronizationManager
                        .getResourceMap()
        );
    }
}
