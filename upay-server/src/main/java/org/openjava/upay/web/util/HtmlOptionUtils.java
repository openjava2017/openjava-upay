package org.openjava.upay.web.util;

import org.openjava.upay.shared.type.Gender;
import org.openjava.upay.trade.model.Employee;
import org.openjava.upay.web.domain.HtmlOption;

import java.util.ArrayList;
import java.util.List;

public class HtmlOptionUtils
{
    public static List<HtmlOption> genderHtmlOptions(final Class<Gender> enumClass, boolean selected)
    {
        List<HtmlOption> options = new ArrayList<>();

        Gender[] enums = enumClass.getEnumConstants();
        for (int i = 0; i < enums.length; i++) {
            options.add(i == 0 && selected ?
                HtmlOption.create(enums[i].getCode() + "", enums[i].getName(), selected) :
                HtmlOption.create(enums[i].getCode() + "", enums[i].getName()));
        }
        return options;
    }

    public static List<HtmlOption> employeeHtmlOptions(final List<Employee> employees, boolean selected)
    {
        List<HtmlOption> options = new ArrayList<>();

        for (int i = 0; i < employees.size(); i++) {
            Employee employee = employees.get(i);
            options.add(i == 0 && selected ?
                    HtmlOption.create(employee.getId() + "", employee.getName(), selected) :
                    HtmlOption.create(employee.getId() + "", employee.getName()));
        }
        return options;
    }
}
