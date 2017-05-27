package org.openjava.upay.shared.sequence;

public interface ISerialKeyGenerator
{
    String nextSerialNo(String typeCode, String scope);
}
