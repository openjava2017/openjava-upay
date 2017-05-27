package org.openjava.upay.trade.service;

import org.openjava.upay.shared.model.Page;
import org.openjava.upay.trade.model.Employee;

import java.util.List;

public interface IDemoService
{
    Page<Employee> listEmployees(String account, String mobile, long start, int length);

    List<Employee> listAllEmployees();
}
