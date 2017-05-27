package org.openjava.upay.web.domain;

public class HtmlOption
{
    private final static String SELECTED = "selected";
    
    private String value;
    private String label;
    private String selected;
    
    private HtmlOption(String value, String label)
    {
        this(value, label, false);
    }
    
    private HtmlOption(String value, String label, boolean selected)
    {
        this.value = value;
        this.label = label;
        this.selected = selected ? SELECTED : "";
    }
    
    public String getValue()
    {
        return value;
    }

    public String getLabel()
    {
        return label;
    }

    public String getSelected()
    {
        return selected;
    }

    public static HtmlOption create(String value, String label)
    {
        return new HtmlOption(value, label);
    }
    
    public static HtmlOption create(String value, String label, boolean selected)
    {
        return new HtmlOption(value, label, selected);
    }
}
