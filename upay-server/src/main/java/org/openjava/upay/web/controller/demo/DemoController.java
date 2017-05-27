package org.openjava.upay.web.controller.demo;

import org.openjava.upay.shared.model.Page;
import org.openjava.upay.trade.model.Employee;
import org.openjava.upay.trade.service.IDemoService;
import org.openjava.upay.web.domain.TablePage;
import org.openjava.upay.web.infrastructure.httl.HttlLayoutViewSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(value = "/demo")
public class DemoController extends HttlLayoutViewSupport
{
    private static Logger LOG = LoggerFactory.getLogger(DemoController.class);

    @Resource
    private IDemoService demoService;

    @RequestMapping(value = "/info.action")
    public ModelAndView info()
    {
        return toEmpty("demo/info");
    }

    @RequestMapping(value = "/table.action")
    public ModelAndView table()
    {
        Map<String, Object> params = new HashMap<>();
        params.put("employees", demoService.listAllEmployees());
        return toEmpty("demo/table", params);
    }

    @RequestMapping(value = "/listEmployees.action")
    public @ResponseBody
    TablePage<Employee> listEmployees(String account, String mobile, TablePage page)
    {
        Page<Employee> employees = demoService.listEmployees(account, mobile, page.getStart(), page.getLength());
        return page.wrapData(employees.getTotal(), employees.getData());
    }

    @RequestMapping(value = "/dialog.action")
    public ModelAndView dialog(String param)
    {
        LOG.info("param = " + param);
        return toEmpty("demo/dialog");
    }
}
