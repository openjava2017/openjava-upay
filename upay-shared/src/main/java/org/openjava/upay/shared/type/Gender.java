package org.openjava.upay.shared.type;

import org.openjava.upay.util.type.IEnumType;

import java.util.Arrays;
import java.util.List;

public enum Gender implements IEnumType
{
    MALE("男", 1),

    FEMALE("女", 2);

    private String name;
    private int code;

    Gender(String name, int code)
    {
        this.name = name;
        this.code = code;
    }   

    public static Gender getGender(int code)
    {
        for (Gender type : Gender.values())
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
        for (Gender type : Gender.values())
        {
            if (type.getCode() == code)
            {
                return type.name;
            }
        }
        return null;
    }

    public static List<Gender> getGenderList()
    {
        return Arrays.asList(Gender.values());
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
