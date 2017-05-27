package org.openjava.upay.shared.mybatis;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.openjava.upay.util.type.IEnumType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GenericEnumTypeHandler<E extends IEnumType> extends BaseTypeHandler<E>
{
    private final E[] enums;

    public GenericEnumTypeHandler(Class<E> type)
    {
        if(type == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        } else {
            this.enums = type.getEnumConstants();
            if(this.enums == null) {
                throw new IllegalArgumentException(type.getSimpleName() + " does not represent an enum type.");
            }
        }
    }

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, E e, JdbcType jdbcType) throws SQLException
    {
        preparedStatement.setInt(i, e.getCode());
    }

    @Override
    public E getNullableResult(ResultSet resultSet, String columnName) throws SQLException
    {
        int code = resultSet.getInt(columnName);
        if(resultSet.wasNull()) {
            return null;
        } else {
            return getEnumType(code);
        }
    }

    @Override
    public E getNullableResult(ResultSet resultSet, int columnIndex) throws SQLException
    {
        int code = resultSet.getInt(columnIndex);
        if (resultSet.wasNull()) {
            return null;
        } else {
            return getEnumType(code);
        }
    }

    @Override
    public E getNullableResult(CallableStatement callableStatement, int columnIndex) throws SQLException
    {
        int code = callableStatement.getInt(columnIndex);
        if (callableStatement.wasNull()) {
            return null;
        } else {
            return getEnumType(code);
        }
    }

    private E getEnumType(int code)
    {
        for (E e : enums) {
           if (e.getCode() == code) {
               return e;
           }
        }

        return null;
    }
}
