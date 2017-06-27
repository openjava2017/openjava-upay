package org.openjava.upay.shared.type;

import org.openjava.upay.util.type.IEnumType;

public enum ErrorCode implements IEnumType
{
    UNKNOWN_EXCEPTION(1000, "系统未知异常"),

    SERVICE_ACCESS_DENIED(1001, "服务访问拒绝"),

    SERVICE_UNAVAILABLE(1002, "服务不可用"),

    DATA_VERIFY_FAILED(1003, "数据验签失败"),

    DATA_SIGN_FAILED(1004, "数据签名失败"),

    ARGUMENT_MISSED(1010, "系统参数缺失"),

    ILLEGAL_ARGUMENT(1011, "无效系统参数"),

    INVALID_MERCHANT(1012, "无效商户"),

    ACCOUNT_NOT_FOUND(1013, "资金账号不存在"),

    INVALID_ACCOUNT_STATUS(1014, "无效的账户状态"),

    INVALID_ACCOUNT_PASSWORD(1015, "账户密码错误"),

    INSUFFICIENT_ACCOUNT_FUNDS(1020, "账户余额不足"),

    TRANSACTION_NOT_FOUND(1021, "资金事务不存在"),

    INVALID_TRANSACTION_STATUS(1022, "无效的事务状态"),

    DATA_CONCURRENT_MODIFY(1050, "数据并发修改");

    private int code;
    private String name;

    ErrorCode(int code, String name)
    {
        this.code = code;
        this.name = name;
    }

    @Override
    public int getCode()
    {
        return code;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
