package com.bruteforce.ila.security;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

/**
 * Same rules as StudentScopeInterceptor, for request bodies (chat, viva, quiz, sessions,
 * study plans, new units and topics, dependencies, previous-year papers).
 */
@ControllerAdvice
public class StudentBodyGuardAdvice extends RequestBodyAdviceAdapter {

    private static final List<String> OWNED_FIELDS =
            List.of("subjectId", "unitId", "topicId", "prerequisiteTopicId", "paperId");

    private final AccessGuard accessGuard;

    public StudentBodyGuardAdvice(AccessGuard accessGuard) {
        this.accessGuard = accessGuard;
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        Long me = AccessGuard.currentStudentId();
        if (me == null || body == null) {
            return body;
        }

        Object student = read(body, "studentId");
        if (student != null && !String.valueOf(student).equals(String.valueOf(me))) {
            throw new AccessDeniedException("You can only access your own data.");
        }

        for (String field : OWNED_FIELDS) {
            Object value = read(body, field);
            if (value instanceof Long id) {
                accessGuard.requireOwnerOfField(field, id);
            }
        }
        return body;
    }

    /** Reads body.getXxx() (or a record-style xxx()) when the body has it, otherwise null. */
    private Object read(Object body, String field) {
        String getterName = "get" + Character.toUpperCase(field.charAt(0)) + field.substring(1);
        for (String name : new String[] {getterName, field}) {
            try {
                Method accessor = body.getClass().getMethod(name);
                accessor.setAccessible(true);
                return accessor.invoke(body);
            } catch (ReflectiveOperationException | RuntimeException e) {
                // try the next name
            }
        }
        return null;
    }
}
