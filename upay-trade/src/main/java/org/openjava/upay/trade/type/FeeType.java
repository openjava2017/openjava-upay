package org.openjava.upay.trade.type;

import org.openjava.upay.util.type.IEnumType;

public enum FeeType implements IEnumType
{
    POUNDAGE("手续费", 1),

    FLAT_COST("工本费", 2),

    CASH_PLEDGE("押金", 3);

    private String name;
    private int code;

    FeeType(String name, int code)
    {
        this.name = name;
        this.code = code;
    }

    public static FeeType getFeeType(int code)
    {
        for (FeeType type : FeeType.values())
        {
            if (type.getCode() == code)
            {
                return type;
            }
        }
        return null;
    }

    public static String getName(int code)
    {
        for (FeeType type : FeeType.values())
        {
            if (type.getCode() == code)
            {
                return type.name;
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
