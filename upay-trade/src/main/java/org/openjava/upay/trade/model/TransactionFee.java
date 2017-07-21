package org.openjava.upay.trade.model;

import org.openjava.upay.core.type.FundType;
import org.openjava.upay.core.type.Pipeline;

import java.util.Date;

public class TransactionFee
{
    private Long id;
    private Long transactionId;
    private Pipeline pipeline;
    private FundType type;
    private Long amount;
    private Date createdTime;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getTransactionId()
    {
        return transactionId;
    }

    public void setTransactionId(Long transactionId)
    {
        this.transactionId = transactionId;
    }

    public Pipeline getPipeline()
    {
        return pipeline;
    }

    public void setPipeline(Pipeline pipeline)
    {
        this.pipeline = pipeline;
    }

    public FundType getType()
    {
        return type;
    }

    public void setType(FundType type)
    {
        this.type = type;
    }

    public Long getAmount()
    {
        return amount;
    }

    public void setAmount(Long amount)
    {
        this.amount = amount;
    }

    public Date getCreatedTime()
    {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime)
    {
        this.createdTime = createdTime;
    }
}
