package org.openjava.upay.trade.service.impl;

import org.openjava.upay.core.exception.FundTransactionException;
import org.openjava.upay.core.model.Merchant;
import org.openjava.upay.core.type.Pipeline;
import org.openjava.upay.shared.type.ErrorCode;
import org.openjava.upay.trade.domain.RegisterTransaction;
import org.openjava.upay.trade.domain.TransactionId;
import org.openjava.upay.trade.service.IRegisterTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

public class RegisterTransactionServiceImpl implements IRegisterTransactionService
{
    private static final Logger LOG = LoggerFactory.getLogger(RegisterTransactionServiceImpl.class);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransactionId register(Merchant merchant, RegisterTransaction transaction) throws Exception
    {
        if (transaction.getAmount() != null) {
            if (transaction.getPipeline() == Pipeline.ACCOUNT || transaction.getAmount() <= 0) {
                LOG.error("Invalid transaction pipeline and amount: pipline != ACCOUNT & amount > 0");
                throw new FundTransactionException(ErrorCode.ARGUMENT_MISSED);
            }
        }
        return null;
    }
}
