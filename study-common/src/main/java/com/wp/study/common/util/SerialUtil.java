package com.wp.study.common.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerialUtil {

	private static final Logger LOG = LoggerFactory.getLogger(SerialUtil.class);
	
	public static <T extends Serializable> void objSerializable(T obj) {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = new FileOutputStream("e:/serial.txt");
			oos = new ObjectOutputStream(fos);
			oos.writeObject(obj);
			oos.flush();
			fos.flush();
			fos.close();
			oos.close();
		} catch(FileNotFoundException fnfe) {
			LOG.error(fnfe.getMessage());
		} catch(IOException ioe) {
			LOG.error(ioe.getMessage());
		} finally {
			if(null != fos) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(null != oos) {
				try {
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static <T extends Serializable> T serializableObj(Class<T> objType) {
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		T t = null;
		try {
			fis = new FileInputStream("e:/serial.txt");
			ois = new ObjectInputStream(fis);
			Object obj = ois.readObject();
			fis.close();
			ois.close();
			t = objType.cast(obj);
		} catch(FileNotFoundException fnfe) {
			LOG.error(fnfe.getMessage());
		} catch(IOException ioe) {
			LOG.error(ioe.getMessage());
		} catch(ClassNotFoundException cnfe) {
			LOG.error(cnfe.getMessage());
		} catch(ClassCastException cce) {
			LOG.error(cce.getMessage());
		} finally {
			if(null != fis) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(null != ois) {
				try {
					ois.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return t;
	}

}
