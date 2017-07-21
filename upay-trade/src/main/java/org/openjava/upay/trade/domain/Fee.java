package org.openjava.upay.trade.domain;

import org.openjava.upay.core.type.FundType;
import org.openjava.upay.core.type.Pipeline;

public class Fee
{
    private Pipeline pipeline;
    private FundType type;
    private Long amount;

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
}
