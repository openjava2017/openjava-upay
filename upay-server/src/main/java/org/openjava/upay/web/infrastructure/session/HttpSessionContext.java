package org.openjava.upay.web.infrastructure.session;

public class HttpSessionContext
{
    private SharedHttpSession session;
    private ISessionContextListener contextListener;
    
    public void attachSession(SharedHttpSession session, ISessionContextListener contextListener)
    {
        this.session = session;
        this.contextListener = contextListener;
        this.session.doInit(this);
    }
    
    public SharedHttpSession getSession()
    {
//        return (session == null || session.isExpired()) ? null : session;
        return session;
    }
    
    public void destroy()
    {
        if (contextListener != null) {
            contextListener.onDestroy();
        }
    }
    
    public void onSessionCreated()
    {
        if (contextListener != null) {
            contextListener.sessionCreated();
        }
    }
    
    public void onSessionInvalidated()
    {
        if (contextListener != null) {
            contextListener.sessionInvalidated();
        }
    }
    
    public static interface ISessionContextListener
    {
        public void sessionCreated();
        
        public void sessionInvalidated();
        
        public void onDestroy();
    }
}
