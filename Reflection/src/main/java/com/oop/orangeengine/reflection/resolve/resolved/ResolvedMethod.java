package com.oop.orangeengine.reflection.resolve.resolved;

import com.oop.orangeengine.reflection.invoker.MethodInvoker;
import lombok.Getter;
import lombok.NonNull;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

@Getter
public class ResolvedMethod {

    private Method javaMethod;
    private Class[] args;

    public ResolvedMethod(@NonNull Method javaMethod) {
        this.javaMethod = javaMethod;
        this.args = javaMethod.getParameterTypes();
        javaMethod.setAccessible(true);
    }

    public String getName() {
        return javaMethod.getName();
    }

    public <T> T invoke(Object holder) {
        return invoke(holder, new Class[0]);
    }

    public <T> T invoke(Object holder, Object... args) {
        return invoke(holder, new HashSet<Object[]>(){{
            add(args);
        }});
    }

    public <T> T invoke(Object holder, Set<Object[]> argsSet) {
        for(Object[] args : argsSet) {
            try {
                return (T) javaMethod.invoke(holder, args);

            } catch (Exception ex) {
                if(!(ex instanceof IllegalArgumentException))
                    ex.printStackTrace();
            }

        }

        return null;

    }

    public MethodInvoker newInvoker() {
        return new MethodInvoker(this);
    }

}
