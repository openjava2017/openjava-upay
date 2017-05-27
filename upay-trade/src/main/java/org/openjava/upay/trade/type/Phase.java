package org.openjava.upay.trade.type;

import org.openjava.upay.util.type.IEnumType;

public enum Phase implements IEnumType
{
    PHASE_FIRST("第一阶段", 1),

    PHASE_SECOND("第二阶段", 2);

    private String name;
    private int code;

    Phase(String name, int code)
    {
        this.name = name;
        this.code = code;
    }

    public static Phase getEmployeeStatus(int code)
    {
        for (Phase status : Phase.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return null;
    }

    public static String getName(int code)
    {
        for (Phase status : Phase.values()) {
            if (status.getCode() == code) {
                return status.name;
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
