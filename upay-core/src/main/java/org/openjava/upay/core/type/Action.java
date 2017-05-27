package org.openjava.upay.core.type;

import org.openjava.upay.util.type.IEnumType;

public enum Action implements IEnumType
{
    INCOME("收入", 1),

    OUTGO("支出", 2);

    private String name;
    private int code;

    Action(String name, int code)
    {
        this.name = name;
        this.code = code;
    }

    public static Action getAction(int code)
    {
        for (Action type : Action.values())
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
