package org.openjava.upay.trade.type;

import org.openjava.upay.util.type.IEnumType;

public enum TransactionStatus implements IEnumType
{
    STATUS_APPLY("申请", 1),

    STATUS_COMPLETED("完成", 4);

    private String name;
    private int code;

    TransactionStatus(String name, int code)
    {
        this.name = name;
        this.code = code;
    }

    public static TransactionStatus getTransactionStatus(int code)
    {
        for (TransactionStatus type : TransactionStatus.values())
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
        for (TransactionStatus type : TransactionStatus.values())
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
