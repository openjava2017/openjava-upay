package org.openjava.upay.shared.model;

import java.util.Collections;
import java.util.List;

public class Page<T>
{
    private Long total;
    private List<T> data;
    
    public Page()
    {
        this.total = 0L;
        this.data = Collections.emptyList();
    }
    
    public Page(Long total, List<T> data)
    {
        this.total = total;
        this.data = data;
    }

    public Long getTotal()
    {
        return total;
    }

    public void setTotal(Long total)
    {
        this.total = total;
    }

    public List<T> getData()
    {
        return data;
    }

    public void setData(List<T> data)
    {
        this.data = data;
    }
}
