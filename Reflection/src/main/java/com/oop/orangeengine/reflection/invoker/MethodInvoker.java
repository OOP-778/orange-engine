package com.oop.orangeengine.reflection.invoker;

import com.oop.orangeengine.reflection.resolve.resolved.ResolvedMethod;

public class MethodInvoker {

    private ResolvedMethod method;
    private boolean invoked = false;
    private Object object;

    public MethodInvoker(ResolvedMethod method) {
        this.method = method;
    }

    public MethodInvoker object(Object object) {
        this.object = object;
        return this;
    }

    public MethodInvoker invoke(Object... args) {
        if(invoked) return this;

        try {

            method.invoke(object, args);
            invoked = true;

        } catch (Exception ex) {

            if (ex instanceof IllegalArgumentException) return this;
            ex.printStackTrace();

        }

        return this;
    }

}
