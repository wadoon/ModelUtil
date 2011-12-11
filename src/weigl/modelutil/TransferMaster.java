package weigl.modelutil;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

public class TransferMaster {

    private static final Map<Class<?>, PropertyDescriptor> methodForTypeMap = new HashMap<>();

    public static void registerType(Class<?> clazz, String prop)
	    throws IntrospectionException {
	BeanInfo i = Introspector.getBeanInfo(clazz);
	for (PropertyDescriptor p : i.getPropertyDescriptors()) {
	    if (p.getName().equalsIgnoreCase(prop)) {
		registerType(clazz, p);
	    }
	}
    }

    private static void registerType(Class<?> clazz, PropertyDescriptor p) {
	methodForTypeMap.put(clazz, p);
    }

    public static void get(Object to, Object bean) {
	try {
	    Map<Field, String> fieldprop = getBindFields(to.getClass());
	    for (Entry<Field, String> e : fieldprop.entrySet()) {
		Object value = valueFromBean(bean, e.getValue());
		Method m = getMethodForRead(e.getKey().getType());
		m.invoke(to, value);
	    }
	} catch (IntrospectionException | IllegalAccessException
		| IllegalArgumentException | InvocationTargetException e) {
	    e.printStackTrace();
	}
    }

    public static void set(Object from, Object bean) {
	try {
	    Map<Field, String> fieldprop = getBindFields(from.getClass());
	    for (Entry<Field, String> e : fieldprop.entrySet()) {
		Method m = getMethodForRead(e.getKey().getType());
		System.out.println("Call " + m.getName() + " for Field "
			+ e.getKey().getName());

		Object a = e.getKey().get(from);
		Object value = m.invoke(a);
		System.out.println("Value is: " + from + " set to "
			+ e.getValue());

		valueToBean(bean, e.getValue(), value);
	    }
	} catch (IntrospectionException | IllegalAccessException
		| IllegalArgumentException | InvocationTargetException e) {
	    e.printStackTrace();
	}
    }

    private static Method getMethodForRead(Class<?> clazz) {
	return methodForTypeMap.get(clazz).getReadMethod();
    }

    private static Method getMethodForWrite(Class<?> clazz) {
	return methodForTypeMap.get(clazz).getWriteMethod();
    }

    private static Object valueFromBean(Object from, String string)
	    throws IntrospectionException, IllegalAccessException,
	    IllegalArgumentException, InvocationTargetException {
	return valueFromBean(from, string.split("."));
    }

    private static Object valueFromBean(Object from, String[] split)
	    throws IntrospectionException, IllegalAccessException,
	    IllegalArgumentException, InvocationTargetException {
	PropertyDescriptor pd = propFromBean(from, split);
	if (pd != null)
	    return pd.getReadMethod().invoke(from);
	return null;
    }

    private static Object valueToBean(Object from, String path, Object value)
	    throws IntrospectionException, IllegalAccessException,
	    IllegalArgumentException, InvocationTargetException {
	//TODO split not working right split("abc",".") = [] 
	return valueToBean(from, tokenize(path,"."), value);
    }

    private static Object valueToBean(Object from, String[] split, Object value)
	    throws IntrospectionException, IllegalAccessException,
	    IllegalArgumentException, InvocationTargetException {
	PropertyDescriptor pd = propFromBean(from, split);
	if (pd != null)
	    return pd.getWriteMethod().invoke(from, value);
	return null;
    }

    private static PropertyDescriptor propFromBean(Object from, String[] split)
	    throws IntrospectionException, IllegalAccessException,
	    IllegalArgumentException, InvocationTargetException {
	BeanInfo cur = Introspector.getBeanInfo(from.getClass());
	for (int i = 0; i < split.length; i++) {
	    // all properties
	    PropertyDescriptor[] props = cur.getPropertyDescriptors();
	    for (PropertyDescriptor p : props) {// found property
		if (p.getName().equalsIgnoreCase(split[i])) {
		    if (i == split.length - 1) { // last property
			return p;
		    } else {
			from = p.getReadMethod().invoke(from);// get bean
			cur = Introspector.getBeanInfo(from.getClass());
		    }
		}
	    }
	}
	return null;
    }
    
    private static String[] tokenize(String haystack, String pattern)
    {
	StringTokenizer st = new StringTokenizer(haystack, pattern);
	String[] s = new String[st.countTokens()];
	for (int i = 0; i < s.length; i++) {
	    s[i] = st.nextToken();
	}
	return s;
    }

    private static Map<Field, String> getBindFields(
	    Class<? extends Object> clazz) {
	Field f[] = clazz.getFields();
	Map<Field, String> map = new HashMap<>();
	for (Field field : f) {
	    System.out.println(field.getName());
	    BindField bf = field.getAnnotation(BindField.class);
	    if (bf != null)
		map.put(field, bf.value());
	}
	return map;
    }

}
