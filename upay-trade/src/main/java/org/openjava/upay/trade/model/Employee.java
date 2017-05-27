package org.openjava.upay.trade.model;

import org.openjava.upay.shared.type.EmployeeStatus;
import org.openjava.upay.shared.type.Gender;

import java.util.Date;

public class Employee
{
    private Long id;
    private String account;
    private String name;
    private Gender gender;
    private String mobile;
    private String telphone;
    private String email;
    private String password;
    private EmployeeStatus status;
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

    public String getAccount()
    {
        return account;
    }

    public void setAccount(String account)
    {
        this.account = account;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Gender getGender()
    {
        return gender;
    }

    public void setGender(Gender gender)
    {
        this.gender = gender;
    }

    public String getMobile()
    {
        return mobile;
    }

    public void setMobile(String mobile)
    {
        this.mobile = mobile;
    }

    public String getTelphone()
    {
        return telphone;
    }

    public void setTelphone(String telphone)
    {
        this.telphone = telphone;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public EmployeeStatus getStatus()
    {
        return status;
    }

    public void setStatus(EmployeeStatus status)
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
