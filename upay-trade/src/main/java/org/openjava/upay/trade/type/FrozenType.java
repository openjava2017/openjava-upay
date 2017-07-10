package org.openjava.upay.trade.type;

import org.openjava.upay.util.type.IEnumType;

public enum FrozenType implements IEnumType
{
    SYSTEM_FROZEN("系统冻结", 1),

    TRADE_FROZEN("交易冻结", 2);

    private String name;
    private int code;

    FrozenType(String name, int code)
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
