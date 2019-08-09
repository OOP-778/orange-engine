package com.oop.orangeEngine.reflection;

import com.oop.orangeEngine.reflection.resolve.resolvers.field.FieldResolver;
import com.oop.orangeEngine.reflection.resolve.resolvers.field.FieldsResolver;
import com.oop.orangeEngine.reflection.resolve.resolvers.method.MethodResolver;
import com.oop.orangeEngine.reflection.resolve.resolvers.method.MethodsResolver;
import lombok.Getter;
import lombok.Setter;
import com.oop.orangeEngine.reflection.resolve.resolved.ResolvedField;
import com.oop.orangeEngine.reflection.resolve.resolved.ResolvedMethod;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class OClass {

    @Getter
    private Class<?> javaClass;

    @Setter @Getter
    private Consumer<Throwable> exceptionHandler;

    //Resolved Methods
    private Set<ResolvedMethod> methodSet = new HashSet<>();

    //Resolved Fields
    private Set<ResolvedField> fieldSet = new HashSet<>();

    protected OClass(Class<?> owner) {
        this.javaClass = owner;
    }

    public MethodResolver newMethodResolver() {
        MethodResolver methodResolver = new MethodResolver(this);
        methodResolver.setIfFailed(exceptionHandler);

        return methodResolver;
    }

    public MethodsResolver newMethodsResolver() {
        MethodsResolver methodsResolver = new MethodsResolver(this);
        methodsResolver.setIfFailed(exceptionHandler);

        return methodsResolver;
    }

    public FieldResolver newFieldResolver() {
        FieldResolver fieldResolver = new FieldResolver(this);
        fieldResolver.setIfFailed(exceptionHandler);

        return fieldResolver;
    }

    public FieldsResolver newFieldsResolver() {
        FieldsResolver fieldsResolver = new FieldsResolver(this);
        fieldsResolver.setIfFailed(exceptionHandler);

        return fieldsResolver;
    }

    public ResolvedMethod registerMethod(Method method) {

        ResolvedMethod resolvedMethod = new ResolvedMethod(method);
        methodSet.add(resolvedMethod);

        return resolvedMethod;

    }

    public ResolvedField registerField(Field field) {


        ResolvedField resolvedField = new ResolvedField(field);
        fieldSet.add(resolvedField);

        return resolvedField;

    }

    public Optional<ResolvedField> getField(Field field) {
        return getField(field.getName());
    }

    public Optional<ResolvedField> getField(String fieldName) {
        return fieldSet.stream().filter(f2 -> f2.getJavaField().getName().equals(fieldName)).findFirst();
    }

    public Optional<ResolvedMethod> getMethod(Method method) {
        return getMethod(method.getName(), method.getParameterTypes());
    }

    public Optional<ResolvedMethod> getMethod(String methodName, Class... args) {
        return methodSet.stream().filter(m2 -> m2.getJavaMethod().getName() == methodName && m2.getJavaMethod().getParameterTypes() == args).findFirst();
    }

}
