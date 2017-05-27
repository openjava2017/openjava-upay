package org.openjava.upay.trade.model;

import org.openjava.upay.core.type.Pipeline;
import org.openjava.upay.trade.type.TransactionStatus;
import org.openjava.upay.trade.type.TransactionType;

import java.util.Date;

public class FundTransaction
{
    private Long id;
    private Long merchantId;
    private String serialNo;
    private TransactionType type;
    private Long fromId;
    private String fromName;
    private Long toId;
    private String toName;
    private Pipeline pipeline;
    private Long amount;
    private TransactionStatus status;
    private String description;
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

    public Long getMerchantId()
    {
        return merchantId;
    }

    public void setMerchantId(Long merchantId)
    {
        this.merchantId = merchantId;
    }

    public String getSerialNo()
    {
        return serialNo;
    }

    public void setSerialNo(String serialNo)
    {
        this.serialNo = serialNo;
    }

    public TransactionType getType()
    {
        return type;
    }

    public void setType(TransactionType type)
    {
        this.type = type;
    }

    public Long getFromId()
    {
        return fromId;
    }

    public void setFromId(Long fromId)
    {
        this.fromId = fromId;
    }

    public String getFromName()
    {
        return fromName;
    }

    public void setFromName(String fromName)
    {
        this.fromName = fromName;
    }

    public Long getToId()
    {
        return toId;
    }

    public void setToId(Long toId)
    {
        this.toId = toId;
    }

    public String getToName()
    {
        return toName;
    }

    public void setToName(String toName)
    {
        this.toName = toName;
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

    public TransactionStatus getStatus()
    {
        return status;
    }

    public void setStatus(TransactionStatus status)
    {
        this.status = status;
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

    public Date getModifiedTime()
    {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime)
    {
        this.modifiedTime = modifiedTime;
    }
}
