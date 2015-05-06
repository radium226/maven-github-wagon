package com.github.radium226.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ListenerList<T> {

    final private static Logger LOGGER = LoggerFactory.getLogger(ListenerList.class);

    private Set<T> listeners = Sets.newHashSet();
    private Class<T> listenerClass;

    private ListenerList(Class<T> listenerClass) {
        super();

        this.listenerClass = listenerClass;
    }

    public static <T> ListenerList<T> of(Class<T> listenerClass) {
        return new ListenerList<T>(listenerClass);
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
                Class<?>[] parameterTypes = Lists.transform(Arrays.asList(parameters), new Function<Object, Class<?>>() {

                    @Override
                    public Class<?> apply(Object parameter) {
                        Class<?> type = parameter.getClass();
                        return type;
                    }

                }).toArray(new Class<?>[0]);
                Method method = listenerClass.getMethod(methodName, parameterTypes);
                method.invoke(listener, parameters);
            } catch (IllegalAccessException e) {
                LOGGER.warn("Unable to invoke {}", methodName, e);
            } catch (SecurityException e) {
                LOGGER.warn("Unable to invoke {}", methodName, e);
            } catch (NoSuchMethodException e) {
                LOGGER.warn("Unable to invoke {}", methodName, e);
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Unable to invoke {}", methodName, e);
            } catch (InvocationTargetException e) {
                LOGGER.warn("Unable to invoke {}", methodName, e);
            }
        }
    }

}
