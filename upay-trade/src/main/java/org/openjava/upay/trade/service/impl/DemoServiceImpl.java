package org.openjava.upay.trade.service.impl;

import org.openjava.upay.shared.model.Page;
import org.openjava.upay.trade.dao.IDemoDao;
import org.openjava.upay.trade.model.Employee;
import org.openjava.upay.trade.service.IDemoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service("demoService")
public class DemoServiceImpl implements IDemoService
{
    @Resource
    private IDemoDao demoDao;

    @Override
    public Page<Employee> listEmployees(String account, String mobile, long start, int length)
    {
        Long total = demoDao.countEmployees(account, mobile);
        if (total > 0) {
            List<Employee> employees = demoDao.listEmployees(account, mobile, start, length);
            return new Page<>(total, employees);

        }
        return new Page<>();
    }

    @Override
    public List<Employee> listAllEmployees()
    {
        return demoDao.listAllEmployees();
    }
}
