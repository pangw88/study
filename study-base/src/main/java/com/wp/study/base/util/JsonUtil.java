package com.wp.study.base.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonUtil {
	
	private static final Logger LOG = LoggerFactory.getLogger(JsonUtil.class);
	
	private static final ObjectMapper objectMapper = new ObjectMapper();
	private static final JsonFactory jsonFactory = new JsonFactory();
    

    /**
     * JSON格式字符串转Bean
     * @param json 对象字符串
     * @param requiredType 目标类
     * @return Object 转换后的对象
     */
    public static <T> T convertJsonToBean(String json, Class<T> requiredType) {
    	T t = null;
    	if (StringUtils.isNotEmpty(json)) {
    		try {
            	t = objectMapper.readValue(json, requiredType);
            } catch (IOException e) {
                LOG.error("将json字符串{}转为{}类型时发生异常", json, requiredType);
            }
		} else {
			LOG.warn("json string is empty!");
		}
        return t;
    }

    /**
     * Bean转JSON格式字符串
     * @param bean 要转换的对象
     * @return String JSON格式的字符串
     */
    public static String convertBeanToJson(Object bean) {
    	String json = null;
        if (bean != null) {
        	JsonGenerator jsonGenerator = null;
        	StringWriter stringWriter = null;
        	try {
        		stringWriter = new StringWriter();
        		jsonGenerator = jsonFactory.createJsonGenerator(stringWriter);
        		objectMapper.writeValue(jsonGenerator, bean);
                json = stringWriter.toString();
                jsonGenerator.close();
            } catch (IOException e) {
                LOG.error(e.getMessage());
            }
		} else {
			LOG.warn("bean is null!");
		}
        return json;
    }
    
    
    /**
     * 从JSON字符串中取出指定字段的值
     * @param json JSON字符串
     * @param field 指定字段名
     * @return 对应的value
     */
    public static String getFieldValueFromJson(String json, String field) {
    	String value = null;
    	if (StringUtils.isNotEmpty(json)) {
    		JsonParser jsonParser = null;
    		JsonNode jsonNode = null;
    		try {
    			jsonFactory.setCodec(objectMapper);
                jsonParser = jsonFactory.createJsonParser(json);
                jsonNode = jsonParser.readValueAsTree();
                value = jsonNode.get(field).getTextValue();
            } catch (IOException e) {
            	LOG.error(e.getMessage());
            }
    	} else {
			LOG.warn("json string is empty!");
		}
        return value;
    }
    
    /**
     * 从JSON数据流取出指定字段的值
     * @param in JSON数据流
     * @param field 指定字段名
     * @return 对应的value
     */
    public static String getFieldValueFromJson(InputStream in, String field) {
    	String value = null;
    	if (in != null) {
    		JsonParser jsonParser = null;
    		JsonNode jsonNode = null;
	        try {
	        	jsonFactory.setCodec(objectMapper);
	        	jsonParser = jsonFactory.createJsonParser(in);
	        	jsonNode = jsonParser.readValueAsTree();
	        	value = jsonNode.get(field).getTextValue();
	        } catch (IOException e) {
	        	LOG.error(e.getMessage());
	        }
    	} else {
    		LOG.warn("input stream is null!");
		}
        return value;
    }
    
    /**
     * json字符串转JsonNode
     * @param json
     * @return JsonNode
     */
    public static JsonNode getJsonNodeFromJson(String json) {
    	JsonNode jsonNode = null;
    	if (StringUtils.isNotEmpty(json)) {
    		JsonParser jsonParser = null;
    		try {
    			jsonFactory.setCodec(objectMapper);
    			jsonParser = jsonFactory.createJsonParser(json);
                jsonNode = jsonParser.readValueAsTree();
            } catch (IOException e) {
            	LOG.error(e.getMessage());
            }
    	} else {
			LOG.warn("json string is empty!");
		}
    	return jsonNode;
    }
    
	public static <T> List<T> getList(String json, Class<T> requiredType) {
		List<T> list = null;
		if(StringUtils.isNotEmpty(json)) {
			TypeFactory typeFactory = null;
			try {
				typeFactory = TypeFactory.defaultInstance();
				// 指定容器结构和类型，这里是ArrayList和requiredType
				list = objectMapper.readValue(json, 
						typeFactory.constructCollectionType(ArrayList.class, requiredType));
			} catch (Exception e) {
				LOG.error(e.getMessage());
			}
		} else {
			LOG.warn("json string is empty!");
		}	
		return list;
	}

}
