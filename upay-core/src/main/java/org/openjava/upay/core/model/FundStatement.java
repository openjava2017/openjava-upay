package org.openjava.upay.core.model;

import org.openjava.upay.core.type.Action;
import org.openjava.upay.core.type.Pipeline;

import java.util.Date;

public class FundStatement
{
    private Long id;
    private Long accountId;
    private Long transactionId;
    private Pipeline pipeline;
    private Action action;
    private Long balance;
    private Long amount;
    private String description;
    private Date createdTime;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getAccountId()
    {
        return accountId;
    }

    public void setAccountId(Long accountId)
    {
        this.accountId = accountId;
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

    public Action getAction()
    {
        return action;
    }

    public void setAction(Action action)
    {
        this.action = action;
    }

    public Long getBalance()
    {
        return balance;
    }

    public void setBalance(Long balance)
    {
        this.balance = balance;
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

    public Date getCreatedTime()
    {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime)
    {
        this.createdTime = createdTime;
    }
}
