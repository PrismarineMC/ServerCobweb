package io.github.servercobweb.plugin;

import io.github.servercobweb.Server;
import io.github.servercobweb.event.Event;
import io.github.servercobweb.event.Listener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class MethodEventExecutor implements EventExecutor {

    private Method method;

    public MethodEventExecutor(Method method) {
        this.method = method;
    }

    @Override
    public void execute(Listener listener, Event event) {
        try {
            method.invoke(listener, event);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Server.getInstance().getLogger().logException(e);
        }
    }

    public Method getMethod(){
        return method;
    }
}
