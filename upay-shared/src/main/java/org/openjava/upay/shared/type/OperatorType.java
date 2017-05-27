package org.openjava.upay.shared.type;

public enum OperatorType
{
    ROOT("超级管理员", 0),

    ADMIN("市场管理员", 1),

    FINANCE("财务", 2),

    CASHIER("总收银", 3),

    COUNTER("柜员", 4);

    private String name;
    private int code;

    OperatorType(String name, int code)
    {
        this.name = name;
        this.code = code;
    }

    public static OperatorType getOperatorType(int code)
    {
        for (OperatorType status : OperatorType.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return null;
    }

    public static String getName(int code)
    {
        for (OperatorType status : OperatorType.values()) {
            if (status.getCode() == code) {
                return status.name;
            }
        }
        return null;
    }

    public String getName()
    {
        return name;
    }

    public int getCode()
    {
        return code;
    }
}