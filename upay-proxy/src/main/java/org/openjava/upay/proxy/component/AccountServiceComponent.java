package org.openjava.upay.proxy.component;

import org.openjava.upay.core.exception.FundTransactionException;
import org.openjava.upay.proxy.domain.ServiceRequest;
import org.openjava.upay.proxy.domain.ServiceResponse;
import org.openjava.upay.proxy.util.CallableComponent;
import org.openjava.upay.trade.domain.TradeTransaction;
import org.openjava.upay.trade.domain.Transaction;
import org.openjava.upay.trade.domain.TransactionId;
import org.openjava.upay.trade.service.IDepositTransactionService;
import org.openjava.upay.trade.service.IFeeTransactionService;
import org.openjava.upay.trade.service.ITradeTransactionService;
import org.openjava.upay.trade.service.IWithdrawTransactionService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@CallableComponent(id = "payment.service.account")
@Component("accountServiceComponent")
public class AccountServiceComponent
{
    @Resource
    private IDepositTransactionService depositTransactionService;

    @Resource
    private IWithdrawTransactionService withdrawTransactionService;

    @Resource
    private IFeeTransactionService feeTransactionService;

    @Resource
    private ITradeTransactionService tradeTransactionService;

    public ServiceResponse<TransactionId> deposit(ServiceRequest<Transaction> request) throws Exception
    {
        try {
            TransactionId result = depositTransactionService.deposit(
                    request.getContext().getMerchant(), request.getData());
            return ServiceResponse.success(result);
        } catch (FundTransactionException fex) {
            return ServiceResponse.failure(fex.getCode(), fex.getMessage());
        }
    }

    public ServiceResponse<TransactionId> withdraw(ServiceRequest<Transaction> request) throws Exception
    {
        try {
            TransactionId result = withdrawTransactionService.withdraw(
                    request.getContext().getMerchant(), request.getData());
            return ServiceResponse.success(result);
        } catch (FundTransactionException fex) {
            return ServiceResponse.failure(fex.getCode(), fex.getMessage());
        }
    }

    public ServiceResponse<TransactionId> makeFee(ServiceRequest<Transaction> request) throws Exception
    {
        try {
            TransactionId result = feeTransactionService.makeFee(
                    request.getContext().getMerchant(), request.getData());
            return ServiceResponse.success(result);
        } catch (FundTransactionException fex) {
            return ServiceResponse.failure(fex.getCode(), fex.getMessage());
        }
    }

    public ServiceResponse<TransactionId> trade(ServiceRequest<TradeTransaction> request) throws Exception
    {
        try {
            TransactionId result = tradeTransactionService.trade(
                    request.getContext().getMerchant(), request.getData());
            return ServiceResponse.success(result);
        } catch (FundTransactionException fex) {
            return ServiceResponse.failure(fex.getCode(), fex.getMessage());
        }
    }
}