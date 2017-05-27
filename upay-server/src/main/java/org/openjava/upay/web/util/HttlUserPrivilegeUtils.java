package org.openjava.upay.web.util;


public class HttlUserPrivilegeUtils
{
    /*public static boolean hasPagePrivilege(int code)
    {
        HttpServletRequest request = ServletResolver.getRequest();
        if (request == null) {
            return false;
        }
        
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        
        Operator operator = (Operator) session.getAttribute(WebConstants.SESSION_KEY_OPERATOR);
        if (operator == null) {
            return false;
        }
        
        if (operator.getType() == OperatorType.ROOT) {
            return true;
        }
        
        UserPermission permission = (UserPermission) session.getAttribute(WebConstants.SESSION_KEY_PERMISSION);
        if (permission == null) {
            return false;
        }
        
        List<HtmlPage> pages = permission.getHtmlPages();
        if (pages != null) {
            for (HtmlPage page : pages) {
                if (page.getCode() == code || page.getParentCode() == code) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public static boolean hasElementPrivilege(int code)
    {
        HttpServletRequest request = ServletResolver.getRequest();
        if (request == null) {
            return false;
        }
        
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        
        Operator operator = (Operator) session.getAttribute(WebConstants.SESSION_KEY_OPERATOR);
        if (operator == null) {
            return false;
        }
        
        if (operator.getType() == OperatorType.ROOT) {
            return true;
        }
        
        UserPermission permission = (UserPermission) session.getAttribute(WebConstants.SESSION_KEY_PERMISSION);
        if (permission == null) {
            return false;
        }
        
        List<HtmlElement> elements = permission.getHtmlElements();
        if (elements != null) {
            for (HtmlElement element : elements) {
                if (element.getCode() == code) {
                    return false;
                }
            }
        }
        
        return true;
    }*/
}
