package org.openjava.upay.web.infrastructure.session;

public interface ISessionSerializer
{
    public byte[] serializeKey(String key);
    
    public byte[] serializeSession(SharedHttpSession session);

    public SharedHttpSession deserializeSession(byte[] data);
}
