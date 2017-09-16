package com.wp.study.base.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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
	private static final int DEFAULT_CONNECT_TIMEOUT = 5000;
	private static final int DEFAULT_SOCKET_TIMEOUT = 100000;
	
	public static void doGet(String url) {
		doGet(url, Void.class);
	}
	
	public static <T> T doGet(String url, Class<T> requiredType) {
		return doGet(url, DEFAULT_CONNECT_TIMEOUT, DEFAULT_SOCKET_TIMEOUT, requiredType);
	}
	
	public static <T> T doGet(String url, int connectTimeout, int socketTimeout, Class<T> requiredType) {
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(
				connectTimeout).setSocketTimeout(socketTimeout).build();
		return doGet(url, requestConfig, DEFAULT_CHARSET, requiredType);
	}
	
	public static <T> T doPost(String url, Map<String, Object> params, int connectTimeout, int socketTimeout, Class<T> requiredType) {
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(
				connectTimeout).setSocketTimeout(socketTimeout).build();
		return doPost(url, requestConfig, DEFAULT_CHARSET, params, requiredType);
	}
	
	public static int doGetDownload(String url, File output) {
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(
				DEFAULT_CONNECT_TIMEOUT).setSocketTimeout(DEFAULT_SOCKET_TIMEOUT).build();
		return doGetDownload(url, requestConfig, output);
	}
	
	public static int doPostDownload(String url, Map<String, Object> params, File output) {
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(
				DEFAULT_CONNECT_TIMEOUT).setSocketTimeout(DEFAULT_SOCKET_TIMEOUT).build();
		return doPostDownload(url, requestConfig, params, output);
	}
	
	public static <T> T doGet(String url, RequestConfig requestConfig, String charset, Class<T> requiredType) {
		T t = null;
		if(StringUtils.isEmpty(url)) {
			LOG.warn("url is empty!");
			return t;
		}
		int status = HttpStatus.SC_BAD_REQUEST;
		int index = 0;
		while(index++ < 3 && status != HttpStatus.SC_OK) {
			// DefaultHttpClient is deprecated, use CloseableHttpClient
			CloseableHttpClient httpClient = null;
			HttpGet httpGet = null;
			CloseableHttpResponse response = null;
			try {
				httpClient = HttpClients.createDefault();
			    httpGet = new HttpGet(url);
				// set request config
				if(requestConfig == null) {
					// default connect and socket timeout is -1, so need set default value!
					requestConfig = RequestConfig.custom().setConnectTimeout(
							DEFAULT_CONNECT_TIMEOUT).setSocketTimeout(DEFAULT_SOCKET_TIMEOUT).build();
				}
				httpGet.setConfig(requestConfig);
				// set charset
				if(charset == null) {
					charset = DEFAULT_CHARSET;
				}
			    response = httpClient.execute(httpGet);
			    status = response.getStatusLine().getStatusCode();
			    t = getResponse(response, charset, requiredType);
			} catch (Exception e) {
				LOG.error("doGet fail, url={}, error:", url, e);
			} finally {
				// 建立的http连接，仍被response保持着，为了释放资源，手动取消连接
				closeQuietly(response);
				closeQuietly(httpClient);
		    }
		}
		return t;
	}
	
	public static <T> T doPost(String url, RequestConfig requestConfig, String charset, Map<String, Object> params, Class<T> requiredType) {
		T t = null;
		if(StringUtils.isEmpty(url)) {
			LOG.warn("url is empty!");
			return t;
		}
		int status = HttpStatus.SC_BAD_REQUEST;
		int index = 0;
		while(index++ < 3 && status != HttpStatus.SC_OK) {
			// DefaultHttpClient is deprecated, use CloseableHttpClient
			CloseableHttpClient httpClient = null;
			HttpPost httpPost = null;
			CloseableHttpResponse response = null;
			try {
				httpClient = HttpClients.createDefault();
				httpPost = new HttpPost(url);
				// set request config
				if(requestConfig == null) {
					// default connect and socket timeout is -1, so need set default value!
					requestConfig = RequestConfig.custom().setConnectTimeout(
							DEFAULT_CONNECT_TIMEOUT).setSocketTimeout(DEFAULT_SOCKET_TIMEOUT).build();
				}
				httpPost.setConfig(requestConfig);
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
			    status = response.getStatusLine().getStatusCode();
			    t = getResponse(response, charset, requiredType);
			} catch (Exception e) {
				LOG.error("doPost fail, url={}, error:", url, e);
			} finally {
				// 建立的http连接，仍被response保持着，为了释放资源，手动取消连接
				closeQuietly(response);
				closeQuietly(httpClient);
		    }
		}
		return t;
	}
	
	public static int doGetDownload(String url, RequestConfig requestConfig, File output) {
		int status = HttpStatus.SC_BAD_REQUEST;
		if(StringUtils.isEmpty(url)) {
			LOG.warn("url is empty!");
			return status;
		}
		int index = 0;
		while(index++ < 3 && status != HttpStatus.SC_OK) {
			// DefaultHttpClient is deprecated, use CloseableHttpClient
			CloseableHttpClient httpClient = null;
			HttpGet httpGet = null;
			CloseableHttpResponse response = null;
			try {
				if(url.startsWith("https")) {
					httpClient = SeeSSLCloseableHttpClient.getCloseableHttpClient();
				} else {
					httpClient = HttpClients.createDefault();
				}
			    httpGet = new HttpGet(url);
				// set request config
			    if(requestConfig == null) {
					// default connect and socket timeout is -1, so need set default value!
					requestConfig = RequestConfig.custom().setConnectTimeout(
							DEFAULT_CONNECT_TIMEOUT).setSocketTimeout(DEFAULT_SOCKET_TIMEOUT).build();
				}
				httpGet.setConfig(requestConfig);
			    response = httpClient.execute(httpGet);
			    status = response.getStatusLine().getStatusCode();
			    if (status == HttpStatus.SC_OK) {
			    	InputStream is = response.getEntity().getContent();
				    OutputStream os = new FileOutputStream(output);
				    try {
				    	int bytesRead = 0;
				    	byte[] buffer = new byte[8192];
				    	while ((bytesRead = is.read(buffer, 0, 8192)) != -1) {
				    		os.write(buffer, 0, bytesRead);
				    	}
				    	os.flush();
				    } finally {
				    	closeQuietly(os);
				    	closeQuietly(is);
				    }
			    }
			} catch (Exception e) {
				LOG.error("doGetDownload fail, url={}, error:", url, e);
			} finally {
				// 建立的http连接，仍被response保持着，为了释放资源，手动取消连接
				closeQuietly(response);
				closeQuietly(httpClient);
		    }
		}
		return status;
	}
	
	public static int doPostDownload(String url, RequestConfig requestConfig, Map<String, Object> params, File output) {
		int status = HttpStatus.SC_BAD_REQUEST;
		if(StringUtils.isEmpty(url)) {
			LOG.warn("url is empty!");
			return status;
		}
		int index = 0;
		while(index++ < 3 && status != HttpStatus.SC_OK) {
			// DefaultHttpClient is deprecated, use CloseableHttpClient
			CloseableHttpClient httpClient = null;
			HttpPost httpPost = null;
			CloseableHttpResponse response = null;
			try {
				if(url.startsWith("https")) {
					httpClient = SeeSSLCloseableHttpClient.getCloseableHttpClient();
				} else {
					httpClient = HttpClients.createDefault();
				}
			    httpPost = new HttpPost(url);
				// set request config
			    if(requestConfig == null) {
					// default connect and socket timeout is -1, so need set default value!
					requestConfig = RequestConfig.custom().setConnectTimeout(
							DEFAULT_CONNECT_TIMEOUT).setSocketTimeout(DEFAULT_SOCKET_TIMEOUT).build();
				}
			    httpPost.setConfig(requestConfig);
			    // set paramters
				if(params != null && params.size() > 0) {
					List <NameValuePair> nvps = new ArrayList <NameValuePair>();
					Object value = null;
					for(Map.Entry<String, Object> entry : params.entrySet()) {
						value = entry.getValue();
						nvps.add(new BasicNameValuePair(entry.getKey(), 
								value == null ? "" : value.toString()));
					}
				    httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
				}
			    response = httpClient.execute(httpPost);
			    status = response.getStatusLine().getStatusCode();
			    if (status == HttpStatus.SC_OK) {
			    	InputStream is = response.getEntity().getContent();
				    OutputStream os = new FileOutputStream(output);
				    try {
				    	int bytesRead = 0;
				    	byte[] buffer = new byte[8192];
				    	while ((bytesRead = is.read(buffer, 0, 8192)) != -1) {
				    		os.write(buffer, 0, bytesRead);
				    	}
				    	os.flush();
				    } finally {
				    	closeQuietly(os);
				    	closeQuietly(is);
				    }
			    }
			} catch (Exception e) {
				LOG.error("doPostDownload fail, url={}, error:", url, e);
			} finally {
				// 建立的http连接，仍被response保持着，为了释放资源，手动取消连接
				closeQuietly(response);
				closeQuietly(httpClient);
		    }
		}
		return status;
	}
	
	private static <T> T getResponse(HttpResponse response, String charset, Class<T> requiredType) throws Exception {
		T t = null;
		int statusCode = response.getStatusLine().getStatusCode();
	    if (statusCode != HttpStatus.SC_OK) {
	    	LOG.warn("request failed, status code = {}", statusCode);
	    	return t;
		}
	    HttpEntity httpEntity = response.getEntity();
    	String res = EntityUtils.toString(httpEntity, charset);
    	if(StringUtils.isEmpty(res)) {
    		LOG.warn("http response is empty!");
    		return t;
    	}
    	// if need not void result
		if(String.class.isAssignableFrom(requiredType)) {
			t = requiredType.cast(res);
		} else if(!Void.class.isAssignableFrom(requiredType)) {
			t = JsonUtil.convertJsonToBean(res, requiredType);
		}
    	// after process response, ensure it is fully consumed
    	EntityUtils.consume(httpEntity);
		return t;
	}
	
	private static void closeQuietly(Closeable object) {
		if(null == object) {
			return;
		}
		try {
			object.close();
		} catch(Exception e) {
			LOG.error("closeQuietly fail, error:", e);
		}
	}
	
}
