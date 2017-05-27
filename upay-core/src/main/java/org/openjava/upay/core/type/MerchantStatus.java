package org.openjava.upay.core.type;

import org.openjava.upay.util.type.IEnumType;

public enum MerchantStatus implements IEnumType
{
    NORMAL("正常", 1);

    private String name;
    private int code;

    MerchantStatus(String name, int code)
    {
        this.name = name;
        this.code = code;
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
