package org.openjava.upay.core.type;

import org.openjava.upay.util.type.IEnumType;

public enum FundType implements IEnumType
{
    FUND("资金", 1),

    POUNDAGE("手续费", 2),

    FLAT_COST("工本费", 3),

    CASH_PLEDGE("押金", 4);

    private String name;
    private int code;

    FundType(String name, int code)
    {
        this.name = name;
        this.code = code;
    }

    public static FundType getType(int code)
    {
        for (FundType type : FundType.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return null;
    }

    public static String getName(int code)
    {
        for (FundType type : FundType.values()) {
            if (type.getCode() == code) {
                return type.name;
            }
        }
        return null;
    }

    public boolean isFeeType()
    {
        return this != FUND;
    }

    public StatementType getStatementType()
    {
        switch (this) {
            case FUND:
                return StatementType.FUND;
            case POUNDAGE:
                return StatementType.POUNDAGE;
            case FLAT_COST:
                return StatementType.FLAT_COST;
            case CASH_PLEDGE:
                return StatementType.CASH_PLEDGE;
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
