package org.openjava.upay.trade.domain;

public class PaymentServiceRequest<E>
{
    private PaymentRequestContext context = new PaymentRequestContext();

    private E data;

    public PaymentRequestContext getContext()
    {
        return context;
    }

    public void setContext(PaymentRequestContext context)
    {
        this.context = context;
    }

    public E getData()
    {
        return data;
    }

    public void setData(E data)
    {
        this.data = data;
    }
}
