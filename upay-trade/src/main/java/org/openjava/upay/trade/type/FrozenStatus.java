package org.openjava.upay.trade.type;

import org.openjava.upay.util.type.IEnumType;

public enum FrozenStatus implements IEnumType
{
    FROZEN("冻结", 1),

    UNFROZEN("解冻", 2);

    private String name;
    private int code;

    FrozenStatus(String name, int code)
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
