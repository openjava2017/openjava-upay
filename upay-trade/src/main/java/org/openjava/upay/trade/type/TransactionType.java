package org.openjava.upay.trade.type;

import org.openjava.upay.util.type.IEnumType;

public enum TransactionType implements IEnumType
{
    DEPOSIT("充值", 10),

    WITHDRAW("提现", 11),

    TRADE("交易", 12),

    PAY_FEE("缴费", 14),

    REFUND("退款", 15),

    FLUSH("冲正", 16);

    private String name;
    private int code;

    TransactionType(String name, int code)
    {
        this.name = name;
        this.code = code;
    }

    public static TransactionType getTransactionType(int code)
    {
        for (TransactionType type : TransactionType.values())
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
        for (TransactionType type : TransactionType.values())
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
