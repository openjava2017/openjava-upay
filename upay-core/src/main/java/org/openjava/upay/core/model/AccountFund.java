package org.openjava.upay.core.model;

import java.util.Date;

public class AccountFund
{
    private Long id;
    private Long balance;
    private Long frozenAmount;
    private Integer version;
    private Date createdTime;
    private Date modifiedTime;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getBalance()
    {
        return balance;
    }

    public void setBalance(Long balance)
    {
        this.balance = balance;
    }

    public Long getFrozenAmount()
    {
        return frozenAmount;
    }

    public void setFrozenAmount(Long frozenAmount)
    {
        this.frozenAmount = frozenAmount;
    }

    public Integer getVersion()
    {
        return version;
    }

    public void setVersion(Integer version)
    {
        this.version = version;
    }

    public Date getCreatedTime()
    {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime)
    {
        this.createdTime = createdTime;
    }

    public Date getModifiedTime()
    {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime)
    {
        this.modifiedTime = modifiedTime;
    }
}
