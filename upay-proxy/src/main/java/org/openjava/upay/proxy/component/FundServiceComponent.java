package org.openjava.upay.proxy.component;

import org.openjava.upay.core.exception.FundTransactionException;
import org.openjava.upay.proxy.domain.ServiceRequest;
import org.openjava.upay.proxy.domain.ServiceResponse;
import org.openjava.upay.proxy.exception.ServiceAccessException;
import org.openjava.upay.proxy.util.CallableComponent;
import org.openjava.upay.shared.type.ErrorCode;
import org.openjava.upay.trade.domain.*;
import org.openjava.upay.trade.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@CallableComponent(id = "payment.service.fund")
@Component("fundServiceComponent")
public class FundServiceComponent
{
    private static Logger LOG = LoggerFactory.getLogger(FundServiceComponent.class);

    @Resource
    private IDepositTransactionService depositTransactionService;

    @Resource
    private IWithdrawTransactionService withdrawTransactionService;

    @Resource
    private IFeeTransactionService feeTransactionService;

    @Resource
    private ITradeTransactionService tradeTransactionService;

    @Resource
    private IFundTransactionService fundTransactionService;

    public ServiceResponse<TransactionId> deposit(ServiceRequest<Transaction> request) throws Exception
    {
        try {
            TransactionId result = depositTransactionService.deposit(
                    request.getContext().getMerchant(), request.getData());
            return ServiceResponse.success(result);
        } catch (IllegalArgumentException aex) {
            LOG.error(aex.getMessage());
            return ServiceResponse.failure(ErrorCode.ILLEGAL_ARGUMENT.getCode(), aex.getMessage());
        } catch (FundTransactionException fex) {
            LOG.error(fex.getMessage());
            return ServiceResponse.failure(fex.getCode(), fex.getMessage());
        }
    }

    public ServiceResponse<TransactionId> withdraw(ServiceRequest<Transaction> request) throws Exception
    {
        try {
            TransactionId result = withdrawTransactionService.withdraw(
                    request.getContext().getMerchant(), request.getData());
            return ServiceResponse.success(result);
        } catch (IllegalArgumentException aex) {
            LOG.error(aex.getMessage());
            return ServiceResponse.failure(ErrorCode.ILLEGAL_ARGUMENT.getCode(), aex.getMessage());
        } catch (FundTransactionException fex) {
            LOG.error(fex.getMessage());
            return ServiceResponse.failure(fex.getCode(), fex.getMessage());
        }
    }

    public ServiceResponse<TransactionId> payFees(ServiceRequest<Transaction> request) throws Exception
    {
        try {
            TransactionId result = feeTransactionService.payFees(
                    request.getContext().getMerchant(), request.getData());
            return ServiceResponse.success(result);
        } catch (IllegalArgumentException aex) {
            LOG.error(aex.getMessage());
            return ServiceResponse.failure(ErrorCode.ILLEGAL_ARGUMENT.getCode(), aex.getMessage());
        } catch (FundTransactionException fex) {
            LOG.error(fex.getMessage());
            return ServiceResponse.failure(fex.getCode(), fex.getMessage());
        }
    }

    public ServiceResponse<TransactionId> trade(ServiceRequest<TradeTransaction> request) throws Exception
    {
        try {
            TransactionId result = tradeTransactionService.trade(
                    request.getContext().getMerchant(), request.getData());
            return ServiceResponse.success(result);
        } catch (IllegalArgumentException aex) {
            LOG.error(aex.getMessage());
            return ServiceResponse.failure(ErrorCode.ILLEGAL_ARGUMENT.getCode(), aex.getMessage());
        } catch (FundTransactionException fex) {
            LOG.error(fex.getMessage());
            return ServiceResponse.failure(fex.getCode(), fex.getMessage());
        }
    }

    public ServiceResponse<TransactionId> freeze(ServiceRequest<FrozenTransaction> request)
    {
        try {
            TransactionId result = fundTransactionService.freezeAccountFund(
                    request.getContext().getMerchant(), request.getData());
            return ServiceResponse.success(result);
        } catch (IllegalArgumentException aex) {
            LOG.error(aex.getMessage());
            return ServiceResponse.failure(ErrorCode.ILLEGAL_ARGUMENT.getCode(), aex.getMessage());
        } catch (FundTransactionException fex) {
            LOG.error(fex.getMessage());
            return ServiceResponse.failure(fex.getCode(), fex.getMessage());
        }
    }

    public void unfreeze(ServiceRequest<UnfrozenTransaction> request)
    {
        try {
            fundTransactionService.unfreezeAccountFund(
                    request.getContext().getMerchant(), request.getData());
        } catch (IllegalArgumentException aex) {
            throw new ServiceAccessException(aex.getMessage(), ErrorCode.ILLEGAL_ARGUMENT.getCode());
        } catch (FundTransactionException fex) {
            throw new ServiceAccessException(fex.getMessage(), fex.getCode());
        }
    }
}
