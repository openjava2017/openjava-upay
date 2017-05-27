package org.openjava.upay.shared.type;

import org.openjava.upay.util.type.IEnumType;

public enum EmployeeStatus implements IEnumType
{
    NORMAL("正常", 0),
    
    LOCKED("锁定", 1);
    
    private String name;
    private int code;

    EmployeeStatus(String name, int code)
    {
        this.name = name;
        this.code = code;
    }
    
    public static EmployeeStatus getEmployeeStatus(int code)
    {
        for (EmployeeStatus status : EmployeeStatus.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return null;
    }

    public static String getName(int code)
    {
        for (EmployeeStatus status : EmployeeStatus.values()) {
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
