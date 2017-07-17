package org.openjava.upay.core.type;

import org.openjava.upay.util.type.IEnumType;

public enum StatementType implements IEnumType
{
    FUND("资金流水", 1),

    POUNDAGE("手续费流水", 2),

    FLAT_COST("工本费流水", 3),

    CASH_PLEDGE("押金流水", 4);

    private String name;
    private int code;

    StatementType(String name, int code)
    {
        this.name = name;
        this.code = code;
    }

    public static StatementType getType(int code)
    {
        for (StatementType type : StatementType.values()) {
            if (type.getCode() == code) {
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
