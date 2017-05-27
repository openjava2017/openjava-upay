package org.openjava.upay.trade.domain;

import org.openjava.upay.core.type.Pipeline;
import org.openjava.upay.trade.type.FeeType;

public class Fee
{
    private Pipeline pipeline;
    private FeeType type;
    private Long amount;

    public Pipeline getPipeline()
    {
        return pipeline;
    }

    public void setPipeline(Pipeline pipeline)
    {
        this.pipeline = pipeline;
    }

    public FeeType getType()
    {
        return type;
    }

    public void setType(FeeType type)
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
