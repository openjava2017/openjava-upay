package org.openjava.upay.trade.domain;

import org.openjava.upay.core.type.AccountType;
import org.openjava.upay.core.type.Pipeline;
import org.openjava.upay.shared.type.Gender;

import java.util.List;

public class RegisterTransaction
{
    private AccountType type;
    private String code;
    private String name;
    private Gender gender;
    private String mobile;
    private String email;
    private String idCode;
    private String address;
    private String password;

    public AccountType getType()
    {
        return type;
    }

    public void setType(AccountType type)
    {
        this.type = type;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
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

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getIdCode()
    {
        return idCode;
    }

    public void setIdCode(String idCode)
    {
        this.idCode = idCode;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
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
