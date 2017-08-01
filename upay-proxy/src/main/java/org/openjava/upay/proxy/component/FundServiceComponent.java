package org.openjava.upay.proxy.component;

import org.openjava.upay.core.exception.FundTransactionException;
import org.openjava.upay.proxy.domain.ServiceRequest;
import org.openjava.upay.proxy.domain.ServiceResponse;
import org.openjava.upay.proxy.exception.ServiceAccessException;
import org.openjava.upay.proxy.util.CallableComponent;
import org.openjava.upay.shared.type.ErrorCode;
import org.openjava.upay.trade.domain.*;
import org.openjava.upay.trade.service.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@CallableComponent(id = "payment.service.fund")
@Component("fundServiceComponent")
public class FundServiceComponent
{
    @Resource
    private IDepositTransactionService depositTransactionService;

    @Resource
    private IWithdrawTransactionService withdrawTransactionService;

    @Resource
    private IFeeTransactionService feeTransactionService;

    @Resource
    private ITradeTransactionService tradeTransactionService;

    @Resource
    private IRefundTransactionService refundTransactionService;

    @Resource
    private IFundTransactionService fundTransactionService;

    @Resource
    private IFlushTransactionService flushTransactionService;

    public ServiceResponse<TransactionId> deposit(ServiceRequest<Transaction> request) throws Exception
    {
        try {
            TransactionId result = depositTransactionService.deposit(
                    request.getContext().getMerchant(), request.getData());
            return ServiceResponse.success(result);
        } catch (IllegalArgumentException aex) {
            throw new ServiceAccessException(aex.getMessage(), ErrorCode.ILLEGAL_ARGUMENT.getCode());
        } catch (FundTransactionException fex) {
            throw new ServiceAccessException(fex.getMessage(), fex.getCode());
        } catch (Exception ex) {
            throw new ServiceAccessException("Deposit fund account failed", ex);
        }
    }

    public ServiceResponse<TransactionId> withdraw(ServiceRequest<Transaction> request) throws Exception
    {
        try {
            TransactionId result = withdrawTransactionService.withdraw(
                    request.getContext().getMerchant(), request.getData());
            return ServiceResponse.success(result);
        } catch (IllegalArgumentException aex) {
            throw new ServiceAccessException(aex.getMessage(), ErrorCode.ILLEGAL_ARGUMENT.getCode());
        } catch (FundTransactionException fex) {
            throw new ServiceAccessException(fex.getMessage(), fex.getCode());
        } catch (Exception ex) {
            throw new ServiceAccessException("Withdraw fund account failed", ex);
        }
    }

    public ServiceResponse<TransactionId> payFees(ServiceRequest<Transaction> request) throws Exception
    {
        try {
            TransactionId result = feeTransactionService.payFees(
                    request.getContext().getMerchant(), request.getData());
            return ServiceResponse.success(result);
        } catch (IllegalArgumentException aex) {
            throw new ServiceAccessException(aex.getMessage(), ErrorCode.ILLEGAL_ARGUMENT.getCode());
        } catch (FundTransactionException fex) {
            throw new ServiceAccessException(fex.getMessage(), fex.getCode());
        } catch (Exception ex) {
            throw new ServiceAccessException("Pay fee transaction failed", ex);
        }
    }

    public ServiceResponse<TransactionId> trade(ServiceRequest<TradeTransaction> request) throws Exception
    {
        try {
            TransactionId result = tradeTransactionService.trade(
                    request.getContext().getMerchant(), request.getData());
            return ServiceResponse.success(result);
        } catch (IllegalArgumentException aex) {
            throw new ServiceAccessException(aex.getMessage(), ErrorCode.ILLEGAL_ARGUMENT.getCode());
        } catch (FundTransactionException fex) {
            throw new ServiceAccessException(fex.getMessage(), fex.getCode());
        } catch (Exception ex) {
            throw new ServiceAccessException("Trade transaction failed", ex);
        }
    }

    public ServiceResponse<TransactionId> refund(ServiceRequest<RefundTransaction> request) throws Exception
    {
        try {
            TransactionId result = refundTransactionService.refund(
                    request.getContext().getMerchant(), request.getData());
            return ServiceResponse.success(result);
        } catch (IllegalArgumentException aex) {
            throw new ServiceAccessException(aex.getMessage(), ErrorCode.ILLEGAL_ARGUMENT.getCode());
        } catch (FundTransactionException fex) {
            throw new ServiceAccessException(fex.getMessage(), fex.getCode());
        } catch (Exception ex) {
            throw new ServiceAccessException("refund transaction failed", ex);
        }
    }

    public ServiceResponse<TransactionId> freeze(ServiceRequest<FrozenFundTransaction> request)
    {
        try {
            TransactionId result = fundTransactionService.freezeAccountFund(
                    request.getContext().getMerchant(), request.getData());
            return ServiceResponse.success(result);
        } catch (IllegalArgumentException aex) {
            throw new ServiceAccessException(aex.getMessage(), ErrorCode.ILLEGAL_ARGUMENT.getCode());
        } catch (FundTransactionException fex) {
            throw new ServiceAccessException(fex.getMessage(), fex.getCode());
        } catch (Exception ex) {
            throw new ServiceAccessException("Freeze account fund failed", ex);
        }
    }

    public void unfreeze(ServiceRequest<UnfrozenFundTransaction> request)
    {
        try {
            fundTransactionService.unfreezeAccountFund(request.getContext().getMerchant(), request.getData());
        } catch (IllegalArgumentException aex) {
            throw new ServiceAccessException(aex.getMessage(), ErrorCode.ILLEGAL_ARGUMENT.getCode());
        } catch (FundTransactionException fex) {
            throw new ServiceAccessException(fex.getMessage(), fex.getCode());
        } catch (Exception ex) {
            throw new ServiceAccessException("Unfreeze account fund failed", ex);
        }
    }

    public ServiceResponse<TransactionId> flush(ServiceRequest<FlushTransaction> request)
    {
        try {
            TransactionId result = flushTransactionService.flush(
                    request.getContext().getMerchant(), request.getData());
            return ServiceResponse.success(result);
        } catch (IllegalArgumentException aex) {
            throw new ServiceAccessException(aex.getMessage(), ErrorCode.ILLEGAL_ARGUMENT.getCode());
        } catch (FundTransactionException fex) {
            throw new ServiceAccessException(fex.getMessage(), fex.getCode());
        } catch (Exception ex) {
            throw new ServiceAccessException("Flush account fund failed", ex);
        }
    }
}
