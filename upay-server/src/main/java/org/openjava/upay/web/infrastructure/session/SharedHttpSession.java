package org.openjava.upay.web.infrastructure.session;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

@SuppressWarnings("deprecation")
public class SharedHttpSession implements HttpSession
{
    private String id;
    private long creationTime = 0L;
    private int maxInactiveInterval;
    private transient boolean isNew = false;
    private transient boolean isDirty = false;
    private transient boolean isExpired = false;
    private transient HttpSessionContext context;
    
    private Map<String, Object> data = new ConcurrentHashMap<String, Object>();
    
    public SharedHttpSession()
    {
    }
    
    public SharedHttpSession(String id, int maxInactiveInterval)
    {
        this.id = id;
        this.creationTime = System.currentTimeMillis();
        this.maxInactiveInterval = maxInactiveInterval;
    }
    
    public void doInit(HttpSessionContext context)
    {
        this.context = context;
        if (isNew()) {
            context.onSessionCreated();
        }
    }
    
    public SharedHttpSession wrap(boolean isNew, boolean isDirty, boolean isExpired)
    {
        this.isNew = isNew;
        this.isDirty = isDirty;
        this.isExpired = isExpired;
        return this;
    }
    
    @Override
    public String getId()
    {
        return this.id;
    }

    @Override
    public long getCreationTime()
    {
        return this.creationTime;
    }
    
    @Override
    public long getLastAccessedTime()
    {
        return System.currentTimeMillis();
    }

    @Override
    public ServletContext getServletContext()
    {
        return null;
    }

    @Override
    public void setMaxInactiveInterval(int interval)
    {
        this.maxInactiveInterval = interval;
    }

    @Override
    public int getMaxInactiveInterval()
    {
        return this.maxInactiveInterval;
    }

    @Override
    public javax.servlet.http.HttpSessionContext getSessionContext()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getAttribute(String name)
    {
        return data.get(name);
    }

    @Override
    public Object getValue(String name)
    {
        return data.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames()
    {
        final Iterator<String> iterator = data.keySet().iterator();
        return new Enumeration<String>() {
            public boolean hasMoreElements()
            {
                return iterator.hasNext();
            }

            public String nextElement()
            {
                return iterator.next();
            }
        };
    }

    @Override
    public String[] getValueNames()
    {
        String[] names = new String[data.size()];
        return data.keySet().toArray(names);
    }

    @Override
    public void setAttribute(String name, Object value)
    {
        data.put(name, value);
        isDirty = true;
    }

    @Override
    public void putValue(String name, Object value)
    {
        data.put(name, value);
        isDirty = true;
    }

    @Override
    public void removeAttribute(String name)
    {
        data.remove(name);
        isDirty = true;
    }

    @Override
    public void removeValue(String name)
    {
        data.remove(name);
        isDirty = true;
    }

    @Override
    public void invalidate()
    {
        isExpired = true;
        isDirty = true;
        context.onSessionInvalidated();
    }
    
    public boolean isExpired()
    {
        return isExpired;
    }
    
    public boolean isDirty()
    {
        return isDirty;
    }

    @Override
    public boolean isNew()
    {
        return this.isNew;
    }
}
