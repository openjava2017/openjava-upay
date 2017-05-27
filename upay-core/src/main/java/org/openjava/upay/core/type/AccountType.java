package org.openjava.upay.core.type;

import org.openjava.upay.util.type.IEnumType;

public enum AccountType implements IEnumType
{
    MERCHANT("商户", 1),

    INDIVIDUAL("个人", 2);

    private String name;
    private int code;

    AccountType(String name, int code)
    {
        this.name = name;
        this.code = code;
    }

    public static AccountType getAccountType(int code)
    {
        for (AccountType type : AccountType.values())
        {
            if (type.getCode() == code)
            {
                return type;
            }
        }
        return null;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public int getCode()
    {
        return code;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
