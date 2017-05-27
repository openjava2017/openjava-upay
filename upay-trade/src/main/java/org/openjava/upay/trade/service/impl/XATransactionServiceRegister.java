package org.openjava.upay.trade.service.impl;

import org.openjava.upay.trade.service.XATransactionService;
import org.openjava.upay.trade.service.XATransactionServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service("transactionServiceRegister")
public class XATransactionServiceRegister implements BeanPostProcessor
{
    private static Logger LOG = LoggerFactory.getLogger(XATransactionServiceRegister.class);

    @Resource
    private XATransactionServiceFactory xaTransactionServiceFactory;

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
            xaTransactionServiceFactory.registerTransactionService(xaTransactionService);
        }
        return object;
    }
}
