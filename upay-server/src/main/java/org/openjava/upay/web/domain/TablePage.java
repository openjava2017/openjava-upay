package org.openjava.upay.web.domain;

import java.util.List;

public class TablePage<T>
{
    private long start;
    private int length;

    private long recordsTotal;
    private long recordsFiltered;
    private List<T> data;

    public TablePage()
    {
    }

    public long getStart()
    {
        return start;
    }

    public void setStart(long start)
    {
        this.start = start;
    }

    public int getLength()
    {
        return length;
    }

    public void setLength(int length)
    {
        this.length = length;
    }

    public long getRecordsTotal()
    {
        return recordsTotal;
    }

    public void setRecordsTotal(long recordsTotal)
    {
        this.recordsTotal = recordsTotal;
    }

    public long getRecordsFiltered()
    {
        return recordsFiltered;
    }

    public void setRecordsFiltered(long recordsFiltered)
    {
        this.recordsFiltered = recordsFiltered;
    }

    public List<T> getData()
    {
        return data;
    }

    public void setData(List<T> data)
    {
        this.data = data;
    }

    public TablePage wrapData(long total, List<T> data)
    {
        this.recordsTotal = total;
        this.recordsFiltered = total;
        this.data = data;
        return this;
    }
}