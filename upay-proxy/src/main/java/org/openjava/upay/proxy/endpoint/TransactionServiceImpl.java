package org.openjava.upay.proxy.endpoint;

public class TransactionServiceImpl implements ITransactionService
{
    @Override
    public void commit(ServiceRequest<Data> request, ServiceResponse<Data> response)
    {
        System.out.println(request.getData().getName());
        System.out.println("Success");
        response.setData(request.getData());
    }
}
