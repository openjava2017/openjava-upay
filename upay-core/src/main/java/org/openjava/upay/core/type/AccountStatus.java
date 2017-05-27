package org.openjava.upay.core.type;

import org.openjava.upay.util.type.IEnumType;

public enum AccountStatus implements IEnumType
{
    NORMAL("正常", 1),

    FRONZEN("冻结", 2),

    LOCKED("锁定", 3),

    LOGOFF("注销", 10);

    private String name;
    private int code;

    AccountStatus(String name, int code)
    {
        this.name = name;
        this.code = code;
    }

    public static AccountStatus getAccountStatus(int code)
    {
        for (AccountStatus type : AccountStatus.values())
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
