package org.openjava.upay.util.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.openjava.upay.util.ClassUtils;
import org.openjava.upay.util.type.IEnumType;

public class JsonUtils
{
    static
    {
        registerSerializeConfig("org.openjava.upay.core.type.Pipeline");
        registerSerializeConfig("org.openjava.upay.core.type.AccountType");
        registerSerializeConfig("org.openjava.upay.core.type.FundType");
        registerSerializeConfig("org.openjava.upay.shared.type.Gender");

        registerParserConfig("org.openjava.upay.core.type.Pipeline");
        registerParserConfig("org.openjava.upay.core.type.AccountType");
        registerParserConfig("org.openjava.upay.core.type.FundType");
        registerParserConfig("org.openjava.upay.shared.type.Gender");
    }

    public static String toJsonString(Object obj)
    {
        return JSON.toJSONString(obj, SerializerFeature.WriteEnumUsingToString, SerializerFeature.WriteNullListAsEmpty,
            SerializerFeature.WriteNullStringAsEmpty, SerializerFeature.WriteDateUseDateFormat);
    }
    
    public static <T> T fromJsonString(String json, Class<T> type)
    {
        return JSON.parseObject(json, type);
    }

    public static void registerSerializeConfig(String name)
    {
        try {
            SerializeConfig config = SerializeConfig.getGlobalInstance();
            Class<?> type = ClassUtils.getDefaultClassLoader().loadClass(name);
            config.put(type, new EnumTypeSerializer());
        } catch (Exception ex) {
            throw new RuntimeException("FastJson serialize config init exception", ex);
        }
    }

    public static void registerParserConfig(String name)
    {
        try {
            ParserConfig config = ParserConfig.getGlobalInstance();
            Class<? extends IEnumType> type =
                (Class<? extends  IEnumType>)ClassUtils.getDefaultClassLoader().loadClass(name);
            config.putDeserializer(type, new EnumTypeDeserializer<>(type));
        } catch (Exception ex) {
            throw new RuntimeException("FastJson parser config init exception", ex);
        }
    }
}