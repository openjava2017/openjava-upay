package org.openjava.upay.trade.domain;

import org.openjava.upay.core.model.Merchant;
import org.openjava.upay.core.type.Pipeline;
import org.openjava.upay.trade.type.TransactionType;

import java.util.List;

public class Transaction
{
    private Merchant merchant;

    private Long id;
    private TransactionType type;
    private String serialNo;
    private Long fromId;
    private Long toId;
    private Pipeline pipeline;
    private Long amount;
    private String description;
    private List<Fee> fees;
    private String password;

    public Merchant getMerchant()
    {
        return merchant;
    }

    public void setMerchant(Merchant merchant)
    {
        this.merchant = merchant;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public TransactionType getType()
    {
        return type;
    }

    public void setType(TransactionType type)
    {
        this.type = type;
    }

    public String getSerialNo()
    {
        return serialNo;
    }

    public void setSerialNo(String serialNo)
    {
        this.serialNo = serialNo;
    }

    public Long getFromId()
    {
        return fromId;
    }

    public void setFromId(Long fromId)
    {
        this.fromId = fromId;
    }

    public Long getToId()
    {
        return toId;
    }

    public void setToId(Long toId)
    {
        this.toId = toId;
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
