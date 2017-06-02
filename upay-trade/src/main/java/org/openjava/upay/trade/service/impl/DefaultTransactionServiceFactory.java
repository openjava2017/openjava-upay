package org.openjava.upay.trade.service.impl;

import org.openjava.upay.core.exception.FundTransactionException;
import org.openjava.upay.shared.type.ErrorCode;
import org.openjava.upay.trade.service.XATransactionServiceFactory;
import org.openjava.upay.trade.service.XATransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service("transactionServiceFactory")
public class DefaultTransactionServiceFactory implements XATransactionServiceFactory, BeanPostProcessor
{
    private static Logger LOG = LoggerFactory.getLogger(DefaultTransactionServiceFactory.class);

    private Map<String, XATransactionService> services = new ConcurrentHashMap();

    @Override
    public void registerTransactionService(XATransactionService service)
    {
        services.put(service.getServiceId(), service);
    }

    @Override
    public XATransactionService getTransactionService(String serviceId)
    {
        if (serviceId == null) {
            LOG.error("Argument missed: service id");
            throw new FundTransactionException(ErrorCode.ARGUMENT_MISSED);
        }

        XATransactionService service = services.get(serviceId);
        if (service == null) {
            LOG.error("Transaction service not registered: " + serviceId);
            throw new FundTransactionException(ErrorCode.SERVICE_UNAVAILABLE);
        }
        return service;
    }

    @Override
    public Object postProcessBeforeInitialization(Object object, String name) throws BeansException
    {
        return object;
    }

    @Override
    public Object postProcessAfterInitialization(Object object, String name) throws BeansException
    {
        if (object instanceof XATransactionService) {
            XATransactionService xaTransactionService = (XATransactionService)object;
            LOG.info("Register transaction service: " + name);
            registerTransactionService(xaTransactionService);
        }
        return object;
    }
}
