/**
 *    Copyright 2015 Radium226
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.github.radium226.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public final class ListenerList<T> {

    final private static Logger LOGGER = LoggerFactory.getLogger(ListenerList.class);

    private final Set<T> listeners = Sets.newHashSet();
    private final Class<T> listenerClass;

    private ListenerList(Class<T> listenerClass) {
        super();

        this.listenerClass = listenerClass;
    }

    public static <T> ListenerList<T> of(Class<T> listenerClass) {
        return new ListenerList<>(listenerClass);
    }

    public boolean add(T listener) {
        return listeners.add(listener);
    }

    public boolean remove(T listener) {
        return listeners.remove(listener);
    }

    public boolean contains(T listener) {
        return listeners.contains(listener);
    }

    public void fire(String methodName, Object... parameters) {
        for (T listener : listeners) {
            try {
                Class<?>[] parameterTypes = Lists.transform(Arrays.asList(parameters), (Object parameter) -> {
                    Class<?> type = parameter.getClass();
                    return type;
                }).toArray(new Class<?>[0]);
                Method method = listenerClass.getMethod(methodName, parameterTypes);
                method.invoke(listener, parameters);
            } catch (IllegalAccessException | SecurityException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException e) {
                LOGGER.warn("Unable to invoke {}", methodName, e);
            }
        }
    }

}
