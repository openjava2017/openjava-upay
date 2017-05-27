package org.openjava.upay.util.json;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import org.openjava.upay.util.type.IEnumType;

import java.io.IOException;
import java.lang.reflect.Type;

public class EnumTypeSerializer implements ObjectSerializer
{
    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException
    {
        SerializeWriter out = serializer.getWriter();
        if (object == null) {
            serializer.getWriter().writeNull();
            return;
        }

        out.writeInt(((IEnumType) object).getCode());
    }
}
