package org.openjava.upay.trade.dao;

import org.apache.ibatis.annotations.Param;
import org.openjava.upay.shared.mybatis.MybatisMapperSupport;
import org.openjava.upay.trade.model.Employee;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("demoDao")
public interface IDemoDao extends MybatisMapperSupport
{
    Long countEmployees(@Param("account") String account, @Param("mobile") String mobile);

    List<Employee> listEmployees(@Param("account") String account, @Param("mobile") String mobile, @Param("start") long start, @Param("length") int length);

    List<Employee> listAllEmployees();
}
