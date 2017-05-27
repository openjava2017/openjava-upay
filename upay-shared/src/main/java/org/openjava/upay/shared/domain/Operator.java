package org.openjava.upay.shared.domain;

import org.openjava.upay.shared.type.OperatorType;

public class Operator
{
    private Long id;
    private String account;
    private String name;
    private OperatorType type;
    private String institutionCode;
    private String institutionName;
    
    public Operator()
    {
    }
    
    private Operator(Long id, String account, String name, OperatorType type,
        String institutionCode, String institutionName)
    {
        this.id = id;
        this.account = account;
        this.name = name;
        this.type = type;
        this.institutionCode = institutionCode;
        this.institutionName = institutionName;
    }

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

    public OperatorType getType()
    {
        return type;
    }

    public void setType(OperatorType type)
    {
        this.type = type;
    }

    public String getInstitutionCode()
    {
        return institutionCode;
    }

    public void setInstitutionCode(String institutionCode)
    {
        this.institutionCode = institutionCode;
    }
    
    public String getInstitutionName()
    {
        return institutionName;
    }

    public void setInstitutionName(String institutionName)
    {
        this.institutionName = institutionName;
    }

    public static Operator create(Long id, String account, String name, OperatorType type,
        String institutionCode, String institutionName)
    {
        return new Operator(id, account, name, type, institutionCode, institutionName);
    }
}
