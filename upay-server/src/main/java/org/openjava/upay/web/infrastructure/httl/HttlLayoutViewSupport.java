package org.openjava.upay.web.infrastructure.httl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.servlet.ModelAndView;

public abstract class HttlLayoutViewSupport
{
    private static final String LAYOUT_KEY = "layout";
    
    private static final String LAYOUT_DEFAULT = "/default.httl";
    private static final String LAYOUT_CLASSIC = "/classic.httl";
    private static final String LAYOUT_EMPTY = "/empty.httl";
    
    public ModelAndView toClassic(String view)
    {
        return toClassic(view, null);
    }
    
    public ModelAndView toClassic(String view, Map<String, Object> params)
    {
        if (params == null) {
            params = new HashMap<String, Object>();
        }
        
        params.put(LAYOUT_KEY, LAYOUT_CLASSIC);
        return new ModelAndView(view, params);
    }
    
    public ModelAndView toDefault(String view)
    {
        return toDefault(view, null);
    }
    
    public ModelAndView toDefault(String view, Map<String, Object> params)
    {
        if (params == null) {
            params = new HashMap<String, Object>();
        }
        
        params.put(LAYOUT_KEY, LAYOUT_DEFAULT);
        return new ModelAndView(view, params);
    }
    
    public ModelAndView toLayout(String view, String layout)
    {
        return toLayout(view, layout, null);
    }
    
    public ModelAndView toEmpty(String view)
    {
        return toEmpty(view,  null);
    }

    public ModelAndView toEmpty(String view, Map<String, Object> params)
    {
        if (params == null) {
            params = new HashMap<String, Object>();
        }

        params.put(LAYOUT_KEY, LAYOUT_EMPTY);
        return new ModelAndView(view, params);
    }
    
    public ModelAndView toLayout(String view, String layout, Map<String, Object> params)
    {
        if (params == null) {
            params = new HashMap<String, Object>();
        }
        
        params.put(LAYOUT_KEY, layout);
        return new ModelAndView(view, params);
    }


}
