package org.openjava.upay.trade.model;

import org.openjava.upay.trade.type.FrozenStatus;
import org.openjava.upay.trade.type.FrozenType;

import java.util.Date;

public class FundFrozen
{
    private Long id;
    private String serialNo;
    private Long accountId;
    private String accountName;
    private FrozenType type;
    private Long amount;
    private FrozenStatus status;
    private Date frozenTime;
    private Date unfrozenTime;
    private Long merchantId;
    private Long frozenUid;
    private String frozenUname;
    private Long unfrozenUid;
    private String unfrozenUname;
    private String description;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

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

    public String getAccountName()
    {
        return accountName;
    }

    public void setAccountName(String accountName)
    {
        this.accountName = accountName;
    }

    public FrozenType getType()
    {
        return type;
    }

    public void setType(FrozenType type)
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

    public FrozenStatus getStatus()
    {
        return status;
    }

    public void setStatus(FrozenStatus status)
    {
        this.status = status;
    }

    public Date getFrozenTime()
    {
        return frozenTime;
    }

    public void setFrozenTime(Date frozenTime)
    {
        this.frozenTime = frozenTime;
    }

    public Date getUnfrozenTime()
    {
        return unfrozenTime;
    }

    public void setUnfrozenTime(Date unfrozenTime)
    {
        this.unfrozenTime = unfrozenTime;
    }

    public Long getMerchantId()
    {
        return merchantId;
    }

    public void setMerchantId(Long merchantId)
    {
        this.merchantId = merchantId;
    }

    public Long getFrozenUid()
    {
        return frozenUid;
    }

    public void setFrozenUid(Long frozenUid)
    {
        this.frozenUid = frozenUid;
    }

    public String getFrozenUname()
    {
        return frozenUname;
    }

    public void setFrozenUname(String frozenUname)
    {
        this.frozenUname = frozenUname;
    }

    public Long getUnfrozenUid()
    {
        return unfrozenUid;
    }

    public void setUnfrozenUid(Long unfrozenUid)
    {
        this.unfrozenUid = unfrozenUid;
    }

    public String getUnfrozenUname()
    {
        return unfrozenUname;
    }

    public void setUnfrozenUname(String unfrozenUname)
    {
        this.unfrozenUname = unfrozenUname;
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
