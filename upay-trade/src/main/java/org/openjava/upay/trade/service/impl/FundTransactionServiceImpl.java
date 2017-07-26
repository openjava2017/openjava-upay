package org.openjava.upay.trade.service.impl;

import org.openjava.upay.core.dao.IFundAccountDao;
import org.openjava.upay.core.exception.FundTransactionException;
import org.openjava.upay.core.model.FundAccount;
import org.openjava.upay.core.model.Merchant;
import org.openjava.upay.core.service.IFundStreamEngine;
import org.openjava.upay.core.type.AccountStatus;
import org.openjava.upay.shared.sequence.IKeyGenerator;
import org.openjava.upay.shared.sequence.ISerialKeyGenerator;
import org.openjava.upay.shared.sequence.KeyGeneratorManager;
import org.openjava.upay.shared.type.ErrorCode;
import org.openjava.upay.trade.dao.IFrozenTransactionDao;
import org.openjava.upay.trade.domain.FrozenFundTransaction;
import org.openjava.upay.trade.domain.TransactionId;
import org.openjava.upay.trade.domain.UnfrozenFundRequest;
import org.openjava.upay.trade.domain.UnfrozenFundTransaction;
import org.openjava.upay.trade.model.FrozenTransaction;
import org.openjava.upay.trade.service.IFundTransactionService;
import org.openjava.upay.trade.type.FrozenStatus;
import org.openjava.upay.trade.type.FrozenType;
import org.openjava.upay.util.AssertUtils;
import org.openjava.upay.util.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

@Service("fundTransactionService")
public class FundTransactionServiceImpl implements IFundTransactionService
{
    @Resource
    private KeyGeneratorManager keyGeneratorManager;

    @Resource
    private IFundAccountDao fundAccountDao;

    @Resource
    private IFrozenTransactionDao frozenTransactionDao;

    @Resource
    private IFundStreamEngine fundStreamEngine;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransactionId freezeAccountFund(Merchant merchant, FrozenFundTransaction transaction)
    {
        AssertUtils.notNull(transaction.getAccountId(), "Argument missed: accountId");
        AssertUtils.isTrue(transaction.getAmount() != null && transaction.getAmount() > 0,
                "Invalid freeze amount");

        Date when = new Date();
        FundAccount account = fundAccountDao.findFundAccountById(transaction.getAccountId());
        if (account == null || account.getStatus() == AccountStatus.LOGOFF) {
            throw new FundTransactionException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
        if (account.getStatus() != AccountStatus.NORMAL) {
            throw new FundTransactionException(ErrorCode.INVALID_ACCOUNT_STATUS);
        }

        boolean result = fundStreamEngine.freeze(transaction.getAccountId(), transaction.getAmount());
        if (!result) {
            throw new FundTransactionException(ErrorCode.FUND_TRANSACTION_FAILED);
        }

        IKeyGenerator keyGenerator = keyGeneratorManager.getKeyGenerator(KeyGeneratorManager.SequenceKey.FROZEN_TRANSACTION);
        ISerialKeyGenerator serialKeyGenerator = keyGeneratorManager.getSerialKeyGenerator();
        String serialNo = transaction.getSerialNo();
        if (ObjectUtils.isEmpty(serialNo)) {
            serialNo = serialKeyGenerator.nextSerialNo("40");
        }

        FrozenTransaction frozenTransaction = new FrozenTransaction();
        frozenTransaction.setId(keyGenerator.nextId());
        frozenTransaction.setSerialNo(serialNo);
        frozenTransaction.setTargetId(account.getId());
        frozenTransaction.setTargetName(account.getName());
        frozenTransaction.setType(FrozenType.SYSTEM_FROZEN);
        frozenTransaction.setAmount(transaction.getAmount());
        frozenTransaction.setStatus(FrozenStatus.FROZEN);
        frozenTransaction.setFrozenTime(when);
        frozenTransaction.setMerchantId(merchant.getId());
        frozenTransaction.setFrozenUid(transaction.getUserId());
        frozenTransaction.setFrozenUname(transaction.getUserName());
        frozenTransaction.setDescription(transaction.getDescription());
        frozenTransactionDao.freezeAccountFund(frozenTransaction);

        TransactionId transactionId = new TransactionId();
        transactionId.setId(frozenTransaction.getId());
        transactionId.setSerialNo(serialNo);
        return transactionId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfreezeAccountFund(Merchant merchant, UnfrozenFundTransaction transaction)
    {
        AssertUtils.notEmpty(transaction.getSerialNo(), "Argument missed: serialNo");

        Date when = new Date();
        FrozenTransaction fundFrozen = frozenTransactionDao.findFundFrozenByNo(transaction.getSerialNo());
        if (fundFrozen == null) {
            throw new FundTransactionException(ErrorCode.TRANSACTION_NOT_FOUND);
        }
        if (fundFrozen.getStatus() == FrozenStatus.UNFROZEN) {
            return;
        }
        fundStreamEngine.unfreeze(fundFrozen.getTargetId(), fundFrozen.getAmount());
        UnfrozenFundRequest request = new UnfrozenFundRequest();
        request.setId(fundFrozen.getId());
        request.setNewStatus(FrozenStatus.UNFROZEN);
        request.setOldStatus(FrozenStatus.FROZEN);
        request.setUnfrozenTime(when);
        request.setUnfrozenUid(transaction.getUserId());
        request.setUnfrozenUname(transaction.getUserName());
        frozenTransactionDao.unfreezeAccountFund(request);
    }
}
