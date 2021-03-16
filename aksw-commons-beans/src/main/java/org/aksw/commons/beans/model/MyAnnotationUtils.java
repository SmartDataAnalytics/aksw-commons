package org.aksw.commons.beans.model;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;

public class MyAnnotationUtils {
	
	/** 
	 * Poor mans findAnnotation implementation that just invokes getAnnotation on the
	 * annotated element.
	 * 
	 * FIXME This implementation may be insufficient as prior code used spring-core's AnnotationUtils
	 * 
	 * @param <A>
	 * @param annotatedElement
	 * @param annotationType
	 * @return
	 */
	public static <A extends Annotation> A findAnnotation(
			AnnotatedElement annotatedElement, Class<A> annotationType) {
		return annotatedElement.getAnnotation(annotationType);
	}

	
    public static <A extends Annotation> A findPropertyAnnotation(Class<?> clazz, PropertyDescriptor pd, Class<A> annotation) {
        A result;

        String propertyName = pd.getName();
        Field f = ReflectionUtils.findField(clazz, propertyName, null);
        result = f != null
                ? f.getAnnotation(annotation)
                : null
                ;

        result = result == null && pd.getReadMethod() != null
                ? MyAnnotationUtils.findAnnotation(pd.getReadMethod(), annotation)
                : result
                ;

        result = result == null && pd.getWriteMethod() != null
                ? MyAnnotationUtils.findAnnotation(pd.getWriteMethod(), annotation)
                : result
                ;

        return result;
    }
}
