package com.wp.study.base.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerialUtils {

	private static final Logger LOG = LoggerFactory.getLogger(SerialUtils.class);
	
	public static <T extends Serializable> void objSerializable(T obj) {
		if(obj != null) {
			FileOutputStream fos = null;
			ObjectOutputStream oos = null;
			try {
				fos = new FileOutputStream("e:/serial.txt");
				oos = new ObjectOutputStream(fos);
				oos.writeObject(obj);
				oos.flush();
				fos.flush();
			} catch(FileNotFoundException fnfe) {
				LOG.error(fnfe.getMessage());
			} catch(IOException ioe) {
				LOG.error(ioe.getMessage());
			} finally {
				IoUtils.closeQuietly(fos, oos);
			}
		} else {
			LOG.warn("obj is null!");
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
			IoUtils.closeQuietly(fis, ois);
		}
		return t;
	}

}
