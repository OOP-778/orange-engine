package com.oop.orangeengine.reflection.resolve.resolvers.method;

import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.reflection.OClass;
import com.oop.orangeengine.reflection.resolve.resolved.ResolvedMethod;
import com.oop.orangeengine.reflection.resolve.resolvers.AResolvers;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class MethodsResolver extends AResolvers<ResolvedMethod> {

    public MethodsResolver(OClass holder) {
        super(holder);
    }

    public MethodsResolver resolve(String methodName, Class... args) {
        return resolve(new OPair<>(methodName, args));
    }

    public MethodsResolver resolve(OPair<String, Class[]> resolvable) {
        Set<OPair<String, Class[]>> set = new HashSet<>();
        set.add(resolvable);
        return resolve(set);
    }

    public MethodsResolver resolve(Set<OPair<String, Class[]>> resolvableSet) {
        Method method = null;

        for (OPair<String, Class[]> resolvable : resolvableSet) {

            try {
                method = getJavaHolder().getMethod(resolvable.getFirst(), resolvable.getSecond());
            } catch (Exception ex) {
                if(ex instanceof NoSuchMethodException) continue;
                handleException(ex);
            }

            if(method != null) {

                ResolvedMethod resolvedMethod = getHolder().registerMethod(method);
                getResolved().offer(resolvedMethod);

            }

        }

        return this;
    }

}
