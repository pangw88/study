package com.wp.study.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonUtil {

    private static final ObjectMapper mapper = new ObjectMapper();
    
    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    /**
     * JSON格式字符串转Bean
     * @param json 对象字符串
     * @param targetClass 目标类
     * @return Object 转换后的对象
     */
    public static <T> T convertJsonToBean(String json, Class<T> targetClass) {
        try {
            return mapper.readValue(json, targetClass);
        } catch (IOException e) {
            logger.error("将json字符串{}转为{}类型时发生异常", json, targetClass.toString());
            return null;
        }
    }

    /**
     * Bean转JSON格式字符串
     * @param bean 要转换的对象
     * @return String JSON格式的字符串
     */
    public static String convertBeanToJson(Object bean) {
        StringWriter sw = new StringWriter();
        JsonGenerator gen;
        try {
            gen = new JsonFactory().createJsonGenerator(sw);
            mapper.writeValue(gen, bean);
            gen.close();
        } catch (IOException e) {
            logger.error(e.getMessage());
            return "";
        }
        return sw.toString();
    }
    
    
    /**
     * 从JSON字符串中取出指定字段的值
     * @param json JSON字符串
     * @param field 指定字段名
     * @return 对应的value
     */
    public static String getFieldValueFromJson(String json, String field) {
        try {
            JsonFactory factory = new JsonFactory(new ObjectMapper());
            JsonParser parser = factory.createJsonParser(json);
            JsonNode jsonNode = parser.readValueAsTree();
            return jsonNode.get(field).getTextValue();
        } catch (IOException e) {
            return "";
        }
    }
    
    /**
     * 从JSON数据流取出指定字段的值
     * @param in JSON数据流
     * @param field 指定字段名
     * @return 对应的value
     */
    public static String getFieldValueFromJson(InputStream in, String field) {
        try {
            JsonFactory factory = new JsonFactory(new ObjectMapper());
            JsonParser parser = factory.createJsonParser(in);
            JsonNode jsonNode = parser.readValueAsTree();
            return jsonNode.get(field).getTextValue();
        } catch (IOException e) {
            return "";
        }
    }
    
    /**
     * json字符串转JsonNode
     * @param json
     * @return JsonNode
     */
    public static JsonNode getJsonNodeFromJson(String json) {
        try {
            JsonFactory factory = new JsonFactory(new ObjectMapper());
            JsonParser parser = factory.createJsonParser(json);
            return parser.readValueAsTree();
        } catch (IOException e) {
            return null;
        }
    }
    
	public static <T> List<T> getList(String jsonVal, Class<T> clazz) {
		List<T> list = null;
		try {
			if(CommonUtil.isNotEmpty(jsonVal)) {
				TypeFactory t = TypeFactory.defaultInstance();
				// 指定容器结构和类型（这里是ArrayList和clazz）
				list = mapper.readValue(jsonVal,t.constructCollectionType(ArrayList.class,clazz));
			}
		} catch (Exception e) {
			logger.info("json转换异常：" + e.getMessage());
		}
		return list;
	}

	public static void main(String[] args) {
		Timestamp t = new Timestamp(System.currentTimeMillis());
		HashMap<String, Timestamp> h = new HashMap<String, Timestamp>();
		h.put("k", t);
		System.out.println(convertBeanToJson(h));
	}
	
}
