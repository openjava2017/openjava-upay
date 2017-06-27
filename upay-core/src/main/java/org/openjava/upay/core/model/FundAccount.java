package org.openjava.upay.core.model;

import org.openjava.upay.core.type.AccountStatus;
import org.openjava.upay.core.type.AccountType;
import org.openjava.upay.shared.type.Gender;

import java.util.Date;

public class FundAccount
{
    private Long id;
    private AccountType type;
    private String code;
    private String name;
    private Gender gender;
    private String mobile;
    private String email;
    private String idCode;
    private String address;
    private String loginPwd;
    private String password;
    private Boolean pwdChange;
    private Date loginTime;
    private String secretKey;
    private String headImg;
    private Long merchantId;
    private AccountStatus status;
    private Date lockTime;
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

    public String getLoginPwd()
    {
        return loginPwd;
    }

    public void setLoginPwd(String loginPwd)
    {
        this.loginPwd = loginPwd;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public Boolean getPwdChange()
    {
        return pwdChange;
    }

    public void setPwdChange(Boolean pwdChange)
    {
        this.pwdChange = pwdChange;
    }

    public Date getLoginTime()
    {
        return loginTime;
    }

    public void setLoginTime(Date loginTime)
    {
        this.loginTime = loginTime;
    }

    public String getSecretKey()
    {
        return secretKey;
    }

    public void setSecretKey(String secretKey)
    {
        this.secretKey = secretKey;
    }

    public String getHeadImg()
    {
        return headImg;
    }

    public void setHeadImg(String headImg)
    {
        this.headImg = headImg;
    }

    public Long getMerchantId()
    {
        return merchantId;
    }

    public void setMerchantId(Long merchantId)
    {
        this.merchantId = merchantId;
    }

    public AccountStatus getStatus()
    {
        return status;
    }

    public void setStatus(AccountStatus status)
    {
        this.status = status;
    }

    public Date getLockTime()
    {
        return lockTime;
    }

    public void setLockTime(Date lockTime)
    {
        this.lockTime = lockTime;
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
