package org.openjava.upay.trade.service.impl;

import org.openjava.upay.core.exception.FundTransactionException;
import org.openjava.upay.shared.type.ErrorCode;
import org.openjava.upay.trade.domain.Transaction;
import org.openjava.upay.trade.service.XATransactionServiceFactory;
import org.openjava.upay.trade.service.XATransactionService;
import org.openjava.upay.trade.type.Phase;
import org.openjava.upay.trade.type.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service("transactionServiceFactory")
public class DefaultXATransactionFactory implements XATransactionServiceFactory
{
    private static Logger LOG = LoggerFactory.getLogger(DefaultXATransactionFactory.class);

    private Map<TransactionType, XATransactionService> services = new ConcurrentHashMap();

    @Override
    public Transaction submit(Phase phase, Transaction transaction) throws Exception
    {
        if (phase == null) {
            LOG.error("Argument missed: phase");
            throw new FundTransactionException(ErrorCode.ARGUMENT_MISSED);
        }

        if (phase == Phase.PHASE_FIRST) {
            return getTransactionService(transaction).beginTransaction(transaction);
        } else if (phase == Phase.PHASE_SECOND) {
            return getTransactionService(transaction).commitTransaction(transaction);
        } else {
            LOG.error("Invalid transaction phase");
        }

        return transaction;
    }

    @Override
    public void registerTransactionService(XATransactionService service)
    {
        services.put(service.getTransactionType(), service);
    }

    @Override
    public XATransactionService getTransactionService(Transaction transaction)
    {
        if (transaction.getType() == null) {
            LOG.error("Argument missed: transaction type");
            throw new FundTransactionException(ErrorCode.ARGUMENT_MISSED);
        }

        XATransactionService service = services.get(transaction.getType());
        if (service == null) {
            LOG.error("Transaction service not registered: " + transaction.getType());
            throw new FundTransactionException(ErrorCode.UNKNOWN_EXCEPTION);
        }
        return service;
    }
}
