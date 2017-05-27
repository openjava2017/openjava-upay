package org.openjava.upay.web.infrastructure.session;

public interface ISessionRepository
{
    public SharedHttpSession loadSession(String sessionId, int maxInactiveInterval);
    
    public void saveSession(SharedHttpSession session, int maxInactiveInterval);
    
    public void removeSession(SharedHttpSession session);
}
