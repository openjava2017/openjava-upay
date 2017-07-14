package org.openjava.upay.proxy.component;

import org.openjava.upay.core.exception.FundTransactionException;
import org.openjava.upay.proxy.domain.ServiceRequest;
import org.openjava.upay.proxy.domain.ServiceResponse;
import org.openjava.upay.proxy.exception.ServiceAccessException;
import org.openjava.upay.proxy.util.CallableComponent;
import org.openjava.upay.shared.type.ErrorCode;
import org.openjava.upay.trade.domain.AccountId;
import org.openjava.upay.trade.domain.RegisterTransaction;
import org.openjava.upay.trade.service.IAccountTransactionService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@CallableComponent(id = "payment.service.account")
@Component("accountServiceComponent")
public class AccountServiceComponent
{
    @Resource
    private IAccountTransactionService accountTransactionService;

    public ServiceResponse<AccountId> register(ServiceRequest<RegisterTransaction> request) throws Exception
    {
        try {
            AccountId result = accountTransactionService.register(
                    request.getContext().getMerchant(), request.getData());
            return ServiceResponse.success(result);
        } catch (IllegalArgumentException aex) {
            throw new ServiceAccessException(aex.getMessage(), ErrorCode.ILLEGAL_ARGUMENT.getCode());
        } catch (FundTransactionException fex) {
            throw new ServiceAccessException(fex.getMessage(), fex.getCode());
        } catch (Exception ex) {
            throw new ServiceAccessException("Register new fund account failed", ex);
        }
    }

    public void freeze(ServiceRequest<AccountId> request) throws Exception
    {
        try {
            accountTransactionService.freezeFundAccount(request.getData().getId());
        } catch (IllegalArgumentException aex) {
            throw new ServiceAccessException(aex.getMessage(), ErrorCode.ILLEGAL_ARGUMENT.getCode());
        } catch (FundTransactionException fex) {
            throw new ServiceAccessException(fex.getMessage(), fex.getCode());
        } catch (Exception ex) {
            throw new ServiceAccessException("Freeze fund account failed", ex);
        }
    }

    public void unfreeze(ServiceRequest<AccountId> request) throws Exception
    {
        try {
            accountTransactionService.unfreezeFundAccount(request.getData().getId());
        } catch (IllegalArgumentException aex) {
            throw new ServiceAccessException(aex.getMessage(), ErrorCode.ILLEGAL_ARGUMENT.getCode());
        } catch (FundTransactionException fex) {
            throw new ServiceAccessException(fex.getMessage(), fex.getCode());
        } catch (Exception ex) {
            throw new ServiceAccessException("Unfreeze fund account failed", ex);
        }
    }
}