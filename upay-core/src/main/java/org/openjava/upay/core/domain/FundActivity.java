package org.openjava.upay.core.domain;

import org.openjava.upay.core.type.Action;
import org.openjava.upay.core.type.FundType;
import org.openjava.upay.core.type.Pipeline;
import org.openjava.upay.core.type.StatementType;

public class FundActivity
{
    private Long transactionId;
    private Pipeline pipeline;
    private Action action;
    private FundType type;
    private Long amount;
    private String description;

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

    public Action getAction()
    {
        return action;
    }

    public void setAction(Action action)
    {
        this.action = action;
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

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
}
