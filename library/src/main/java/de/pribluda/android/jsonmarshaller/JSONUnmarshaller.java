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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * performs unmarshalling of JSON data creating objects
 */
public class JSONUnmarshaller {
	private static final String SETTER_PREFIX = "set";


	@SuppressWarnings({ "rawtypes" })
	static final HashMap<Class, Class> primitves = new HashMap<Class, Class>();

	static {
		primitves.put(Integer.TYPE, Integer.class);
		primitves.put(Long.TYPE, Long.class);
		primitves.put(Double.TYPE, Double.class);
		primitves.put(Boolean.TYPE, Boolean.class);
	}

	/**
	 * TODO: provide support for nested JSON objects
	 * TODO: provide support for embedded JSON Arrays
	 *
	 * @param jsonObject
	 * @param beanToBeCreatedClass
	 * @param <T>
	 * @return
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws JSONException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> T unmarshall(JSONObject jsonObject, java.lang.Class<T> beanToBeCreatedClass) throws IllegalAccessException, InstantiationException, JSONException, NoSuchMethodException, InvocationTargetException {
		T value = beanToBeCreatedClass.getConstructor().newInstance();

		Iterator keys = jsonObject.keys();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			Object field = jsonObject.get(key);

			//  capitalise to standard setter pattern
			String methodName = SETTER_PREFIX + key.substring(0, 1).toUpperCase() + key.substring(1);

			//System.err.println("method name:" + methodName);

			Method method = getCandidateMethod(beanToBeCreatedClass, methodName);

			if (method != null) {
				Class clazz = method.getParameterTypes()[0];

				// discriminate based on type
				if (field.equals(JSONObject.NULL)) {
					method.invoke(value, clazz.cast(null));
				} else if (field instanceof String) {
					// check if we're an enum
					if (clazz.isEnum()) {
						Object enm = clazz.getMethod("valueOf", String.class).invoke(null, field);
						try {
							beanToBeCreatedClass.getMethod(methodName, clazz).invoke(value, enm);
							continue;
						} catch (NoSuchMethodException e) {
							// that means there was no such method, proceed
						}
					}

					// string shall be used directly, either to set or as constructor parameter (if suitable)
					try {
						beanToBeCreatedClass.getMethod(methodName, String.class).invoke(value, field);
						continue;
					} catch (NoSuchMethodException e) {
						// that means there was no such method, proceed
					}
					// or maybe there is method with suitable parameter?
					if (clazz.isPrimitive() && primitves.get(clazz) != null) {
						clazz = primitves.get(clazz);
					}
					try {
						method.invoke(value, clazz.getConstructor(String.class).newInstance(field));
					} catch (NoSuchMethodException nsme) {
						// we are failed here,  but so what? be lenient
					}

				}
				// we are done with string
				else if (clazz.isArray() || clazz.isAssignableFrom(List.class)) {
					// JSON array corresponds either to array type, or to some collection
					if (field instanceof JSONObject) {
						JSONArray array = new JSONArray();
						array.put(field);
						field = array;
					}

					// we are interested in arrays for now
					if (clazz.isArray()) {
						// populate field value from JSON Array
						Object fieldValue = populateRecursive(clazz, field);
						method.invoke(value, fieldValue);
					} else if (clazz.isAssignableFrom(List.class)) {
						try {
							Type type = method.getGenericParameterTypes()[0];
							if (type instanceof ParameterizedType) {
								Type param = ((ParameterizedType) type).getActualTypeArguments()[0];
								if (param instanceof Class) {
									Class c = (Class) param;

									// populate field value from JSON Array
									Object fieldValue = populateRecursiveList(clazz, c, field);
									method.invoke(value, fieldValue);
								}
							}
						} catch (Exception e) {
							// failed
						}
					}

				} else if (field instanceof JSONObject) {
					 // JSON object means nested bean - process recursively
					method.invoke(value, unmarshall((JSONObject) field, clazz));
				} else if (clazz.equals(Date.class)) {
					method.invoke(value, new Date((Long)field));
				} else {

					// fallback here,  types not yet processed will be
					// set directly ( if possible )
					// TODO: guard this? for better leniency
					method.invoke(value, field);
				}

			}
		}
		return value;
	}

	/**
	 * recursively populate array out of hierarchy of JSON Objects
	 *
	 * @param arrayClass original array class
	 * @param json	   json object in question
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Object populateRecursive(Class arrayClass, Object json) throws JSONException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
		if (arrayClass.isArray() && json instanceof JSONArray) {
			final int length = ((JSONArray) json).length();
			final Class componentType = arrayClass.getComponentType();
			Object retval = Array.newInstance(componentType, length);
			for (int i = 0; i < length; i++) {
				Array.set(retval, i, populateRecursive(componentType, ((JSONArray) json).get(i)));
			}
			return retval;
		} else {
			// this is leaf object, JSON needs to be unmarshalled,
			if (json instanceof JSONObject) {
				return unmarshall((JSONObject) json, arrayClass);
			} else {
				// while all others can be returned verbatim
				return json;
			}
		}
	}

	/**
	 * recursively populate array out of hierarchy of JSON Objects
	 *
	 * @param arrayClass original array class
	 * @param json	   json object in question
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Object populateRecursiveList(Class arrayClass, Class clazz, Object json) throws JSONException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
		if (arrayClass.isAssignableFrom(List.class) && json instanceof JSONArray) {
			final int length = ((JSONArray) json).length();
			Object retval = new ArrayList();
			for (int i = 0; i < length; i++) {
				((List) retval).add(populateRecursive(clazz, ((JSONArray) json).get(i)));
			}
			return retval;
		} else {
			// this is leaf object, JSON needs to be unmarshalled,
			if (json instanceof JSONObject) {
				return unmarshall((JSONObject) json, arrayClass);
			} else {
				// while all others can be returned verbatim
				return json;
			}
		}
	}

	/**
	 * determine array dimenstions in recursive way
	 * TODO: do we need this at all?
	 *
	 * @param dimensions
	 * @param jsonArray
	 */
	@SuppressWarnings("unused")
	private static void recurseDimensions(ArrayList<Integer> dimensions, JSONArray jsonArray) throws JSONException {
		dimensions.add(jsonArray.length());
		if (jsonArray.get(0) instanceof JSONArray) {

		}
	}

	/**
	 * retrieve candidate setter method
	 *
	 * @param clazz
	 * @param name
	 * @return
	 */
	@SuppressWarnings({ "rawtypes" })
	private static Method getCandidateMethod(Class clazz, String name) {
		for (Method method : clazz.getMethods()) {
			if (name.equals(method.getName()) && method.getParameterTypes().length == 1)
				return method;
		}
		return null;
	}

	/**
	 * recursively retrieve base array class
	 *
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings({ "unused", "rawtypes" })
	private static Class retrieveArrayBase(Class clazz) {
		if (clazz.isArray())
			return retrieveArrayBase(clazz.getComponentType());
		return clazz;
	}

	/**
	 * convenience method parsing JSON on the fly
	 *
	 * @param json
	 * @param beanToBeCreatedClass
	 * @param <T>
	 * @return
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws JSONException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static <T> T unmarshall(String json, java.lang.Class<T> beanToBeCreatedClass) throws InvocationTargetException, NoSuchMethodException, JSONException, InstantiationException, IllegalAccessException {
		return unmarshall(new JSONObject(json), beanToBeCreatedClass);
	}
}
