package org.openjava.upay.web.controller;

import org.openjava.upay.shared.sequence.IKeyGenerator;
import org.openjava.upay.shared.sequence.KeyGeneratorManager;
import org.openjava.upay.trade.service.IFundTransactionService;
import org.openjava.upay.web.domain.AjaxMessage;
import org.openjava.upay.web.infrastructure.httl.HttlLayoutViewSupport;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;

@Controller
public class WebApplicationController extends HttlLayoutViewSupport
{
    @Resource
    private KeyGeneratorManager keyGeneratorManager;

    @Resource
    private IFundTransactionService fundTransactionService;

    @RequestMapping(value = "/")
    public ModelAndView index()
    {
        return toDefault("application/index");
    }

    @RequestMapping(value = "/test.action")
    public @ResponseBody AjaxMessage test()
    {
        IKeyGenerator keyGenerator = keyGeneratorManager.getKeyGenerator(KeyGeneratorManager.SequenceKey.TEST_SEQUENCE);
        return AjaxMessage.success("成功:" +  keyGenerator.nextId());
    }

    @RequestMapping(value = "/data.action")
    public @ResponseBody AjaxMessage data()
    {
        fundTransactionService.testCommitTransaction();
        return AjaxMessage.success("成功完成");
    }
}