package com.xlb.service.data.client.util.base;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.Map;

/**
 * 序列化与反序列化工具类
 *
 * @author 赵嘉楠
 * @version 1.0
 * @since 1.0
 */
public final class ObjectStringSerialUtil {

    private final static ObjectStringSerialUtil me = new ObjectStringSerialUtil();
    private final ObjectMapper OBJECT_MAPPER;
    private final XmlMapper XML_MAPPER;
    private final Logger logger;
    private final SimpleFilterProvider JSON_FILTER_PROVIDER;
    private final SimpleFilterProvider XML_FILTER_PROVIDER;

    private ObjectStringSerialUtil() {
        OBJECT_MAPPER = new ObjectMapper();
        JSON_FILTER_PROVIDER = new SimpleFilterProvider();

        XML_MAPPER = new XmlMapper();
        XML_FILTER_PROVIDER = new SimpleFilterProvider();

        logger = LoggerFactory.getLogger(ObjectStringSerialUtil.class);

        OBJECT_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        JSON_FILTER_PROVIDER.setFailOnUnknownId(false);
        OBJECT_MAPPER.setFilterProvider(JSON_FILTER_PROVIDER);

        XML_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        XML_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        XML_FILTER_PROVIDER.setFailOnUnknownId(false);
        XML_MAPPER.setFilterProvider(XML_FILTER_PROVIDER);
    }


    public enum SerialType {
        JSON {
            protected ObjectMapper getMapper(ObjectStringSerialUtil serialUtil) {
                return serialUtil.OBJECT_MAPPER;
            }

            protected SimpleFilterProvider getFilterProvider(ObjectStringSerialUtil serialUtil) {
                return serialUtil.JSON_FILTER_PROVIDER;
            }
        }, XML {
            protected ObjectMapper getMapper(ObjectStringSerialUtil serialUtil) {
                return serialUtil.XML_MAPPER;
            }

            protected SimpleFilterProvider getFilterProvider(ObjectStringSerialUtil serialUtil) {
                return serialUtil.XML_FILTER_PROVIDER;
            }
        };

        protected abstract ObjectMapper getMapper(ObjectStringSerialUtil serialUtil);

        protected abstract SimpleFilterProvider getFilterProvider(ObjectStringSerialUtil serialUtil);
    }

    public enum PropertyFilterType {
        EXCEPT, INCLUDE
    }

    public static ObjectStringSerialUtil getSerialUtil() {
        return me;
    }

    public static ObjectStringSerialUtil newInstance() {
        return new ObjectStringSerialUtil();
    }


    /**
     * <p>添加过滤条件</p>
     *
     * @param filterId   过滤id，调用时使用
     * @param type       序列号类型，JSON或XML
     * @param filterType 过滤方式，包含或去除
     * @param props      属性名
     */
    public void addFilter(String filterId, SerialType type, PropertyFilterType filterType, String... props) {
        if (filterType == PropertyFilterType.EXCEPT) {
            type.getFilterProvider(this).addFilter(filterId, SimpleBeanPropertyFilter.serializeAllExcept(props));
        } else if (filterType == PropertyFilterType.INCLUDE) {
            type.getFilterProvider(this).addFilter(filterId, SimpleBeanPropertyFilter.filterOutAllExcept(props));
        }
    }

    public void addFilter(String filterId, SerialType type, PropertyFilter filter) {
        type.getFilterProvider(this).addFilter(filterId, filter);
    }

    public void removeFilter(String filterId, SerialType type) {
        type.getFilterProvider(this).removeFilter(filterId);
    }

    public void removeFilter(String filterId) {
        for (int i = 0; i < SerialType.values().length; i++) {
            removeFilter(filterId, SerialType.values()[i]);
        }
    }

    /**
     * <p>添加过滤条件</p>
     * <p>序列号json和xml都将应用</p>
     *
     * @param filterId   过滤id
     * @param filterType 过滤方式，包含或去除
     * @param props      属性名
     */
    public void addFilter(String filterId, PropertyFilterType filterType, String... props) {
        for (int i = 0; i < SerialType.values().length; i++) {
            addFilter(filterId, SerialType.values()[i], filterType, props);
        }
    }


    /**
     * <p>序列化对象</p>
     *
     * @param value      需要被序列化的对象
     * @param formatType 序列化格式
     * @return 序列化结果
     */
    public String serial(Object value, SerialType formatType) {
        try {
            return formatType.getMapper(this).writeValueAsString(value);
        } catch (JsonProcessingException e) {
            logger.error("序列化失败", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * <p>序列化对象，并将序列化结果输出到指定输出流中</p>
     *
     * @param outputStream 序列化结果输出流
     * @param value        需要被序列化的对象
     * @param formatType   序列化格式
     */
    public void serial(OutputStream outputStream, Object value, SerialType formatType) {
        try {
            formatType.getMapper(this).writeValue(outputStream, value);
        } catch (IOException e) {
            logger.error("序列化失败", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * <p>序列化对象，并将序列化结果写入到writer中</p>
     *
     * @param writer     序列化结果写入对象
     * @param value      需要被序列化的对象
     * @param formatType 序列化格式
     */
    public void serial(Writer writer, Object value, SerialType formatType) {
        try {
            formatType.getMapper(this).writeValue(writer, value);
        } catch (IOException e) {
            logger.error("序列化失败", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * <p>将字符串反序列化成map对象</p>
     *
     * @param content    需要解析的内容
     * @param formatType 序列化格式
     * @return 反序列化后的结果
     */
    public Map parse(String content, SerialType formatType) {
        return parse(content, Map.class, formatType);
    }

    /**
     * <p>将字符串反序列化成map对象</p>
     *
     * @param content    需要解析的内容
     * @param formatType 序列化格式
     * @return 反序列化后的结果
     */
    public Map parse(Reader content, SerialType formatType) {
        return parse(content, Map.class, formatType);
    }

    /**
     * <p>将字符串反序列化成map对象</p>
     *
     * @param content    需要解析的内容
     * @param formatType 序列化格式
     * @return 反序列化后的结果
     */
    public Map parse(InputStream content, SerialType formatType) {
        return parse(content, Map.class, formatType);
    }

    /**
     * <p>将字符串反序列化成map对象</p>
     *
     * @param content    需要解析的内容
     * @param formatType 序列化格式
     * @return 反序列化后的结果
     */
    public Map parse(URL content, SerialType formatType) {
        return parse(content, Map.class, formatType);
    }

    /**
     * <p>将字符串反序列化成map对象</p>
     *
     * @param content    需要解析的内容
     * @param type       返回结果类型
     * @param formatType 序列化格式
     * @param <T>        返回对象类型
     * @return 反序列化后的结果
     */
    public <T> T parse(String content, Class<T> type, SerialType formatType) {
        try {
            return formatType.getMapper(this).readValue(content, type);
        } catch (IOException e) {
            logger.error("序列化失败", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * <p>将字符串反序列化成map对象</p>
     *
     * @param content    需要解析的内容
     * @param type       返回结果类型
     * @param formatType 序列化格式
     * @param <T>        返回对象类型
     * @return 反序列化后的结果
     */
    public <T> T parse(Reader content, Class<T> type, SerialType formatType) {
        try {
            return formatType.getMapper(this).readValue(content, type);
        } catch (IOException e) {
            logger.error("序列化失败", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * <p>将字符串反序列化成map对象</p>
     *
     * @param content    需要解析的内容
     * @param type       返回结果类型
     * @param formatType 序列化格式
     * @param <T>        返回对象类型
     * @return 反序列化后的结果
     */
    public <T> T parse(InputStream content, Class<T> type, SerialType formatType) {
        try {
            return formatType.getMapper(this).readValue(content, type);
        } catch (IOException e) {
            logger.error("序列化失败", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * <p>将字符串反序列化成map对象</p>
     *
     * @param content    需要解析的内容
     * @param type       返回结果类型
     * @param formatType 序列化格式
     * @param <T>        返回对象类型
     * @return 反序列化后的结果
     */
    public <T> T parse(URL content, Class<T> type, SerialType formatType) {
        try {
            return formatType.getMapper(this).readValue(content, type);
        } catch (IOException e) {
            logger.error("序列化失败", e);
            throw new RuntimeException(e);
        }
    }

    public void configure(MapperFeature f, boolean state) {
        OBJECT_MAPPER.configure(f, state);
        XML_MAPPER.configure(f, state);
    }

    public void configure(SerializationFeature f, boolean state) {
        OBJECT_MAPPER.configure(f, state);
        XML_MAPPER.configure(f, state);
    }

    public void configure(DeserializationFeature f, boolean state) {
        OBJECT_MAPPER.configure(f, state);
        XML_MAPPER.configure(f, state);
    }

    public void configure(JsonParser.Feature f, boolean state) {
        OBJECT_MAPPER.configure(f, state);
        XML_MAPPER.configure(f, state);
    }

    public void configure(JsonGenerator.Feature f, boolean state) {
        OBJECT_MAPPER.configure(f, state);
        XML_MAPPER.configure(f, state);
    }
}
