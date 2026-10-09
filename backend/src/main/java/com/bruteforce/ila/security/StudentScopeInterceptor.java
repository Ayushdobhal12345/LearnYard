package com.bruteforce.ila.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Runs before every /api request and refuses it when it points at someone else's data:
 * another student's id, or a subject, unit, topic, dependency or paper owned by another student.
 * It reads ?name= parameters and /{name} path variables called studentId, subjectId, unitId, topicId.
 */
@Component
public class StudentScopeInterceptor implements HandlerInterceptor {

    private static final List<String> OWNED_IDS = List.of("subjectId", "unitId", "topicId", "paperId");

    private final AccessGuard accessGuard;

    public StudentScopeInterceptor(AccessGuard accessGuard) {
        this.accessGuard = accessGuard;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Long me = AccessGuard.currentStudentId();
        if (me == null) {
            return true;
        }

        Map<?, ?> pathVariables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) instanceof Map<?, ?> map
                ? map : Map.of();

        // 1. the student named in the URL must be the logged-in student
        checkStudent(me, request.getParameter("studentId"));
        Object studentFromPath = pathVariables.get("studentId");
        if (studentFromPath != null) {
            checkStudent(me, studentFromPath.toString());
        }

        // 2. any subject / unit / topic / paper named in the URL must belong to them
        for (String name : OWNED_IDS) {
            checkOwned(name, request.getParameter(name));
            Object fromPath = pathVariables.get(name);
            if (fromPath != null) {
                checkOwned(name, fromPath.toString());
            }
        }

        // 3. endpoints that use a plain {id}
        Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        Long id = parse(pathVariables.get("id"));
        if (pattern != null && id != null) {
            switch (pattern.toString()) {
                case "/api/units/{id}" -> accessGuard.requireUnitOwner(id);
                case "/api/topics/{id}" -> accessGuard.requireTopicOwner(id);
                case "/api/dependencies/{id}" -> accessGuard.requireDependencyOwner(id);
                case "/api/students/{id}" -> accessGuard.requireSelf(id);
                default -> { }
            }
        }
        return true;
    }

    private void checkStudent(Long me, String claimed) {
        if (claimed != null && !claimed.isBlank() && !claimed.trim().equals(String.valueOf(me))) {
            throw new AccessDeniedException("You can only access your own data.");
        }
    }

    private void checkOwned(String name, String value) {
        Long id = parse(value);
        if (id != null) {
            accessGuard.requireOwnerOfField(name, id);
        }
    }

    private Long parse(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(value.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
