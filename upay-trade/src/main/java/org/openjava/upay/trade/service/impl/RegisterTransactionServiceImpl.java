package org.openjava.upay.trade.service.impl;

import org.openjava.upay.core.exception.FundTransactionException;
import org.openjava.upay.core.model.Merchant;
import org.openjava.upay.core.type.Pipeline;
import org.openjava.upay.shared.type.ErrorCode;
import org.openjava.upay.trade.domain.RegisterTransaction;
import org.openjava.upay.trade.domain.TransactionId;
import org.openjava.upay.trade.service.IRegisterTransactionService;
import org.openjava.upay.trade.util.TransactionServiceHelper;
import org.openjava.upay.util.AssertUtils;
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
        validRegisterTransaction(transaction);

        if (transaction.getAmount() != null) {
            AssertUtils.isTrue(transaction.getPipeline() == Pipeline.ACCOUNT &&
                transaction.getAmount() > 0, "Invalid transaction pipeline and amount");
        }

        AssertUtils.isTrue(TransactionServiceHelper.validTransactionFees(transaction.getFees()),
            "Invalid fee pipeline or amount");

        return null;
    }

    private void validRegisterTransaction(RegisterTransaction transaction)
    {
        AssertUtils.notNull(transaction.getType(), "Argument missed: type");
        AssertUtils.notNull(transaction.getName(), "Argument missed: name");
        AssertUtils.notNull(transaction.getMobile(), "Argument missed: mobile");
        AssertUtils.notNull(transaction.getPassword(), "Argument missed: password");
    }
}
