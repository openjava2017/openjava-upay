package org.openjava.upay.core.type;

import org.openjava.upay.util.type.IEnumType;

public enum Pipeline implements IEnumType
{
    CASH("现金", 1),

    ACCOUNT("账户", 2),

    POS("POS", 3);

    private String name;
    private int code;

    Pipeline(String name, int code)
    {
        this.name = name;
        this.code = code;
    }

    public static Pipeline getPipeline(int code)
    {
        for (Pipeline type : Pipeline.values())
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