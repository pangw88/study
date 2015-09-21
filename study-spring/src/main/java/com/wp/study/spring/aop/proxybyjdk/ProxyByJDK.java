package com.wp.study.spring.aop.proxybyjdk;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.wp.study.spring.aop.proxybyjdk.invocation.ModelInvocationHandler;
import com.wp.study.spring.aop.proxybyjdk.model.ProxyModelIntf;
import com.wp.study.spring.aop.proxybyjdk.model.ProxyModelIntfImpl;


public class ProxyByJDK {

	/**
	 * spring将委托给spring容器管理的类，使用代理方式生成实例，
	 * 就可以动态给被代理类中的方法添加权限、日志、事物等等逻辑
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// 被代理的对象
		ProxyModelIntfImpl proxyModelIntfImpl = new ProxyModelIntfImpl();
		// 将被代理对象提交给调用处理器
		InvocationHandler ih = new ModelInvocationHandler(proxyModelIntfImpl);
		
		// 生成代理对象
		ProxyModelIntf proxyModelIntf = (ProxyModelIntf) Proxy.newProxyInstance(
				ProxyModelIntf.class.getClassLoader(), new Class[]{ProxyModelIntf.class}, ih);
		
		Field[] fields = proxyModelIntf.getClass().getFields();
		if(fields != null && fields.length > 0) {
			for(Field field : fields) {
				System.out.println("Field name is : " + field.getName());
			}
		}
		
		System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		
		Method[] methods = proxyModelIntf.getClass().getMethods();
		if(methods != null && methods.length > 0) {
			for(Method method : methods) {
				System.out.println("Method name is : " + method.getName());
			}
		}
		
		
		System.out.println("sampleIntfProxy is instance of SampleIntfImpl : "
				+ (proxyModelIntf instanceof ProxyModelIntfImpl));
		// 使用代理对象调用方法，隐藏被代理对象的实现
		proxyModelIntf.print(proxyModelIntf.getClass() + 
				" : interface proxy use jdk proxy and invocation");
	}
}
