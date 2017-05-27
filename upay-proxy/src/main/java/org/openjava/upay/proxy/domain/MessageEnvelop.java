package org.openjava.upay.proxy.domain;

import org.openjava.upay.trade.type.Phase;

public class MessageEnvelop
{
    private Long appId;
    private String accessToken;
    private String signature;
    private String body;
    private String charset;
    private Phase phase;

    public Long getAppId()
    {
        return appId;
    }

    public void setAppId(Long appId)
    {
        this.appId = appId;
    }

    public String getAccessToken()
    {
        return accessToken;
    }

    public void setAccessToken(String accessToken)
    {
        this.accessToken = accessToken;
    }

    public String getSignature()
    {
        return signature;
    }

    public void setSignature(String signature)
    {
        this.signature = signature;
    }

    public String getBody()
    {
        return body;
    }

    public void setBody(String body)
    {
        this.body = body;
    }

    public String getCharset()
    {
        return charset;
    }

    public void setCharset(String charset)
    {
        this.charset = charset;
    }

    public Phase getPhase()
    {
        return phase;
    }

    public void setPhase(Phase phase)
    {
        this.phase = phase;
    }
}
