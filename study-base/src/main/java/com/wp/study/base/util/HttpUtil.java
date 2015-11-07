package com.wp.study.base.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtil {

	private static final Logger LOG = LoggerFactory.getLogger(HttpUtil.class);
	
	private static final String DEFAULT_CHARSET = "UTF-8";
	private static final int DEFAULT_CONNECT_TIMEOUT = 3000;
	private static final int DEFAULT_SOCKET_TIMEOUT = 20000;
	
	
	public static void doGet(String url) {
		doGet(url, Void.class);
	}
	
	public static <T> T doGet(String url, Class<T> requiredType) {
		return doGet(url, null, requiredType);
	}
	
	public static <T> T doGet(String url, String charset, Class<T> requiredType) {
		return doGet(url, null, charset, requiredType);
	}
	
	public static void doGet(String url, int connectTimeout, int socketTimeout) {
		doGet(url, connectTimeout, socketTimeout, Void.class);
	}
	
	public static <T> T doGet(String url, int connectTimeout, int socketTimeout, Class<T> requiredType) {
		return doGet(url, connectTimeout, socketTimeout, null, requiredType);
	}
	
	public static <T> T doGet(String url, int connectTimeout, int socketTimeout, String charset, Class<T> requiredType) {
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(
				connectTimeout).setSocketTimeout(socketTimeout).build();
		return doGet(url, requestConfig, charset, requiredType);
	}
	
	public static <T> T doGet(String url, RequestConfig requestConfig, String charset, Class<T> requiredType) {
		T t = null;
		if(StringUtils.isNotEmpty(url)) {
			// DefaultHttpClient is deprecated, use CloseableHttpClient
			CloseableHttpClient httpClient = null;
			HttpGet httpGet = null;
			CloseableHttpResponse response = null;
			try {
				httpClient = HttpClients.createDefault();
			    httpGet = new HttpGet(url);
				// set request config
				if(requestConfig != null) {
					httpGet.setConfig(requestConfig);
				} else {
					// default connect and socket timeout is -1, so need set default value!
					requestConfig = RequestConfig.custom().setConnectTimeout(
							DEFAULT_CONNECT_TIMEOUT).setSocketTimeout(DEFAULT_SOCKET_TIMEOUT).build();
				}
				// set charset
				if(charset == null) {
					charset = DEFAULT_CHARSET;
				}
			    response = httpClient.execute(httpGet);
			    t = getResponse(response, charset, requiredType);
			    // 建立的http连接，仍被response保持着，为了释放资源，手动取消连接
		        response.close();
			} catch (Exception e) {
				LOG.error(e.getMessage());
			} finally {
				try {
					if (response != null) {
						response.close();
					}
				} catch(IOException ioe) {
					LOG.error(ioe.getMessage());
				}
		    }
		} else {
			LOG.warn("url is empty!");
		}
		return t;
	}
	
	
	public static <T> T doPost(String url, RequestConfig requestConfig, String charset, Map<String, Object> params, Class<T> requiredType) {
		T t = null;
		if(StringUtils.isNotEmpty(url)) {
			// DefaultHttpClient is deprecated, use CloseableHttpClient
			CloseableHttpClient httpClient = null;
			HttpPost httpPost = null;
			CloseableHttpResponse response = null;
			try {
				httpClient = HttpClients.createDefault();
				httpPost = new HttpPost(url);
				// set request config
				if(requestConfig != null) {
					httpPost.setConfig(requestConfig);
				} else {
					// default connect and socket timeout is -1, so need set default value!
					requestConfig = RequestConfig.custom().setConnectTimeout(
							DEFAULT_CONNECT_TIMEOUT).setSocketTimeout(DEFAULT_SOCKET_TIMEOUT).build();
				}
				// set charset
				if(charset == null) {
					charset = DEFAULT_CHARSET;
				}
				// set paramters
				if(params != null && params.size() > 0) {
					List <NameValuePair> nvps = new ArrayList <NameValuePair>();
					Object value = null;
					for(Map.Entry<String, Object> entry : params.entrySet()) {
						value = entry.getValue();
						nvps.add(new BasicNameValuePair(entry.getKey(), 
								value == null ? "" : value.toString()));
					}
				    httpPost.setEntity(new UrlEncodedFormEntity(nvps, charset));
				}
			    response = httpClient.execute(httpPost);
			    t = getResponse(response, charset, requiredType);
			    // 建立的http连接，仍被response保持着，为了释放资源，手动取消连接
		        response.close();
			} catch (Exception e) {
				LOG.error(e.getMessage());
			} finally {
				try {
					if (response != null) {
						response.close();
					}
				} catch(IOException ioe) {
					LOG.error(ioe.getMessage());
				}
		    }
		} else {
			LOG.warn("url is empty!");
		}
		return t;
	}
	
	private static <T> T getResponse(HttpResponse response, String charset, Class<T> requiredType) throws Exception {
		T t = null;
		int statusCode = response.getStatusLine().getStatusCode();
	    if (statusCode == HttpStatus.SC_OK) {
	    	HttpEntity httpEntity = response.getEntity();
	    	String res = EntityUtils.toString(httpEntity, charset);
	    	if(StringUtils.isNotEmpty(res)) {
	    		// if need not void result
	    		if(!Void.class.isAssignableFrom(requiredType)) {
	    			t = JsonUtil.convertJsonToBean(res, requiredType);
	    		}
	    	} else {
	    		LOG.warn("http response is empty!");
	    	}
	    	// after process response, ensure it is fully consumed
	    	EntityUtils.consume(httpEntity);
		} else {
			LOG.warn("request failed, status code = {}", statusCode);
		}
		return t;
	}
	
}
