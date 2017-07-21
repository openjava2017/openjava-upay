package org.openjava.upay.trade.domain;

import org.openjava.upay.core.type.Pipeline;

import java.util.List;

public class Transaction
{
    private String serialNo;
    private Long accountId;
    private Pipeline pipeline;
    private Long amount;
    private String description;
    private List<Fee> fees;
    private String password;

    public String getSerialNo()
    {
        return serialNo;
    }

    public void setSerialNo(String serialNo)
    {
        this.serialNo = serialNo;
    }

    public Long getAccountId()
    {
        return accountId;
    }

    public void setAccountId(Long accountId)
    {
        this.accountId = accountId;
    }

    public Pipeline getPipeline()
    {
        return pipeline;
    }

    public void setPipeline(Pipeline pipeline)
    {
        this.pipeline = pipeline;
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

    public List<Fee> getFees()
    {
        return fees;
    }

    public void setFees(List<Fee> fees)
    {
        this.fees = fees;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }
}
