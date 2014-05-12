/*
 * Copyright (c) 2010. Konstantin Pribluda (konstantin.pribluda@gmail.com)
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *	 http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package de.pribluda.android.jsonmarshaller;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Date;

/**
 * marshall beans to JSON into writer
 */
public class JSONMarshaller {
	private static final String GETTER_PREFIX = "get";
	private static final int BEGIN_INDEX = GETTER_PREFIX.length();
	public static final String IS_PREFIX = "is";
	public static final int IS_LENGTH = 2;

	/**
	 * marshall supplied object (tree?) to JSON
	 *
	 * @param object
	 * @return
	 */
	public static JSONObject marshall(Object object) throws InvocationTargetException, JSONException, IllegalAccessException, NoSuchMethodException {
		JSONObject retval = new JSONObject();
		marshallRecursive(retval, object);
		return retval;
	}

	/**
	 * recursively marshall to JSON
	 *
	 * @param sink
	 * @param object
	 */
	static void marshallRecursive(JSONObject sink, Object object) throws JSONException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
		// nothing to marshall
		if (object == null)
			return;
		// primitive object is a field and does not interes us here
		if (object.getClass().isPrimitive())
			return;
		// object not null,  and is not primitive - iterate through getters
		for (Method method : object.getClass().getMethods()) {
			// our getters are parameterless and start with "get"
			if ((method.getName().startsWith(GETTER_PREFIX) && method.getName().length() > BEGIN_INDEX || method.getName().startsWith(IS_PREFIX) && method.getName().length() > IS_LENGTH) && (method.getModifiers() & Modifier.PUBLIC) != 0 && method.getParameterTypes().length == 0 && method.getReturnType() != void.class) {
				// is return value primitive?
				Class<?> type = method.getReturnType();
				if (type.isPrimitive() || String.class.equals(type)) {
					// it is, marshall it
					Object val = method.invoke(object);
					if (val != null) {
						sink.put(propertize(method.getName()), val);
					}
					continue;
				} else if (type.isArray()) {
					Object val = marshallArray(method.invoke(object));
					if (val != null) {
						sink.put(propertize(method.getName()), val);
					}
					continue;
				} else if (type.isAssignableFrom(Date.class)) {
					Date date = (Date) method.invoke(object);
					if (date != null) {
						sink.put(propertize(method.getName()), date.getTime());
					}
					continue;
				} else if (type.isAssignableFrom(Boolean.class)) {
					Boolean b = (Boolean) method.invoke(object);
					if (b != null) {
						sink.put(propertize(method.getName()), b.booleanValue());
					}
					continue;
				} else if (type.isAssignableFrom(Integer.class)) {
					Integer i = (Integer) method.invoke(object);
					if (i != null) {
						sink.put(propertize(method.getName()), i.intValue());
					}
					continue;
				} else if (type.isAssignableFrom(Long.class)) {
					Long l = (Long) method.invoke(object);
					if (l != null) {
						sink.put(propertize(method.getName()), l.longValue());
					}
					continue;
				} else {
					// does it have default constructor?
					try {
						if (method.getReturnType().getConstructor() != null) {
							Object val = marshall(method.invoke(object));
							if (val != null) {
								sink.put(propertize(method.getName()), val);
							}
							continue;
						}
					} catch (NoSuchMethodException ex) {
						// just ignore it here, it means no such constructor was found
					}
				}
			}
		}
	}

	/**
	 * recursively marshall [multidimensional? - of course!!! ] array
	 *
	 * @param array
	 * @return
	 */
	public static JSONArray marshallArray(Object array) throws InvocationTargetException, NoSuchMethodException, JSONException, IllegalAccessException {
		if (array != null && array.getClass().isArray()) {
			Class<?> componentType = array.getClass().getComponentType();
			JSONArray retval = new JSONArray();
			final int arrayLength = Array.getLength(array);
			// stirngs and primitives must be marshalled directly
			if (componentType.isPrimitive() || String.class.equals(componentType)) {

				for (int i = 0; i < arrayLength; i++) {
					retval.put(Array.get(array, i));
				}
			} else if (componentType.isArray()) {
				// that's cool, nested array recurse
				for (int i = 0; i < arrayLength; i++) {
					retval.put(marshallArray(Array.get(array, i)));
				}
			} else {
				// treat component as a bean   if it got default constructor
				try {
					if (componentType.getConstructor() != null) {
						for (int i = 0; i < arrayLength; i++) {
							retval.put(marshall(Array.get(array, i)));
						}
					}
				} catch (NoSuchMethodException ex) {
					// just ignore it here, it means no such constructor was found
				}
			}

			return retval;
		}

		return null;
	}

	/**
	 * convert method name to property
	 *
	 * @param name
	 */
	public static String propertize(String name) {
		int offset =  name.startsWith(IS_PREFIX) ? IS_LENGTH : BEGIN_INDEX;
		return name.substring(offset, offset + 1).toLowerCase() + name.substring(offset + 1);
	}
}
