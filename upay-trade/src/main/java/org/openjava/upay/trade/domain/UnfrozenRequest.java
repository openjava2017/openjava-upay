package org.openjava.upay.trade.domain;

import org.openjava.upay.trade.type.FrozenStatus;

import java.util.Date;

public class UnfrozenRequest
{
    private Long id;
    private FrozenStatus newStatus;
    private FrozenStatus oldStatus;
    private Date unfrozenTime;
    private Long unfrozenUid;
    private String unfrozenUname;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public FrozenStatus getNewStatus()
    {
        return newStatus;
    }

    public void setNewStatus(FrozenStatus newStatus)
    {
        this.newStatus = newStatus;
    }

    public FrozenStatus getOldStatus()
    {
        return oldStatus;
    }

    public void setOldStatus(FrozenStatus oldStatus)
    {
        this.oldStatus = oldStatus;
    }

    public Date getUnfrozenTime()
    {
        return unfrozenTime;
    }

    public void setUnfrozenTime(Date unfrozenTime)
    {
        this.unfrozenTime = unfrozenTime;
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
}
