package com.oop.orangeEngine.reflection.resolve.resolvers.method;

import com.oop.orangeEngine.reflection.OClass;
import com.oop.orangeEngine.reflection.resolve.resolved.ResolvedMethod;
import com.oop.orangeEngine.reflection.resolve.resolvers.AResolver;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class MethodResolver extends AResolver<ResolvedMethod> {

    private String[] names;
    private Set<Class[]> args = new HashSet<>();

    public MethodResolver(OClass holder) {
        super(holder);
    }

    public MethodResolver withNames(String... names) {
        this.names = names;
        return this;
    }

    public MethodResolver withParams(Class... args) {
        this.args.add(args);
        return this;
    }

    @Override
    public Optional<ResolvedMethod> resolve() {
        if (getHolder() == null || names == null)
            handleException(new NullPointerException("Holder class or Names are null!"));

        Method method = null;
        boolean isParamsEmpty = args.isEmpty();

        //Loop through methodNames
        for (String methodName : names) {

            if(isParamsEmpty) {
                try{
                    method = getJavaHolder().getMethod(methodName);
                    break;
                } catch (Exception ex) {
                    if(ex instanceof NoSuchMethodException) continue;
                    handleException(ex);
                }

            } else {
                for(Class[] params : args) {

                    try{
                        method = getJavaHolder().getMethod(methodName, params);
                        break;
                    } catch (Exception ex) {
                        if(ex instanceof NoSuchMethodException) continue;
                        handleException(ex);
                    }

                }

            }

        }

        if(method != null)
            return Optional.of(getHolder().registerMethod(method));

        return Optional.empty();

    }

}
