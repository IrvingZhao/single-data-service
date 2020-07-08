package com.xlb.service.data.client.util.base;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collector;

/**
 * <p>属性自动加载类</p>
 * <p>类将自动加载Classpath下的所有.properties文件。</p>
 * <p>文件中key值冲突时，后加载的文件将覆盖先加载的文件。</p>
 *
 * @author Irving.Zhao
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public final class Property {

    private static final Properties properties = new Properties();
    //Entry转mapCollector
    private static final Collector<Map.Entry<Object, Object>, HashMap<String, String>, HashMap<String, String>> mapCollector = Collector.of((Supplier<HashMap<String, String>>) HashMap::new, (t, a) -> t.put(String.valueOf(a.getKey()), String.valueOf(a.getValue())), (l, r) -> {
        l.putAll(r);
        return l;
    });

    static {
        loadProperties();
    }

    private Property() {
    }

    /**
     * <p>加载根目录下的所有配置文件</p>
     * <p>加载为覆盖式更新，会将新文件内的属性覆盖原有同名属性</p>
     */
    public static void loadProperties() {
        try {
            File file = new File(Property.class.getResource("/").toURI());
            loadProperties(file);
        } catch (URISyntaxException e) {
            log.error("初始化根目录异常", e);
            e.printStackTrace();
        }
    }

    /**
     * 加载制定目录下的配置文件
     * <p>加载为覆盖式更新，会将新文件内的属性覆盖原有同名属性</p>
     *
     * @param file 目录
     */
    public static void loadProperties(File file) {
        try {
            File[] propertyFiles = file.listFiles((pathname) -> pathname.isDirectory() || (pathname.isFile() && pathname.getName().indexOf("properties") > 0));
            assert propertyFiles != null;
            for (File item : propertyFiles) {
                if (item.isFile()) {
                    properties.load(new FileReader(item));
                } else if (item.isDirectory()) {
                    loadProperties(item);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * 通过键获得配置文件中的值
     *
     * @param key 键
     * @return 返回匹配到的值
     */
    public static String get(String key) {
        return properties.getProperty(key);
    }

    /**
     * 通过正则匹配键，获得键值
     *
     * @param pattern 正则表达式
     * @return 所有匹配到的值数组
     */
    public static String[] getValues(String pattern) {
        Pattern pat = Pattern.compile(pattern);
        Set<Map.Entry<Object, Object>> entries = properties.entrySet();
        List<String> values = entries.parallelStream()
                .filter((item) -> pat.matcher(item.getKey().toString()).matches())
                .collect(ArrayList<String>::new, (t, a) -> t.add(String.valueOf(a.getValue())), ArrayList::addAll);
        return values.toArray(new String[]{});
    }

    /**
     * 通过正则表达式获得属性键值对
     *
     * @param pattern key值正则表达式
     * @return 所有匹配到的键值对
     */
    public static Map<String, String> getKeyValues(String pattern) {
        Pattern pat = Pattern.compile(pattern);
        return getKeyValues(pat);
    }

    /**
     * 通过正则查找属性值
     *
     * @param pattern 正则
     * @return 所有匹配到的键值对
     */
    public static Map<String, String> getKeyValues(Pattern pattern) {
        Set<Map.Entry<Object, Object>> entries = properties.entrySet();
        return entries.parallelStream()
                .filter((item) -> pattern.matcher(item.getKey().toString()).matches())
                .collect(mapCollector);
    }
}
