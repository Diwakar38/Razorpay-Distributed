package com.projects.razorpay.api_gateway_service.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class HeaderAugmentingRequestWrapper extends HttpServletRequestWrapper {
    private final Map<String, String> extraHeaders = new LinkedHashMap<>();

    public HeaderAugmentingRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    public void putHeaders(String name, String value) {
        extraHeaders.put(name, value);
    }

    @Override
    public String getHeader(String name) {
        String value = extraHeaders.get(name);
        return value != null ? value : super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        String value = extraHeaders.get(name);
        return value != null ? Collections.enumeration(Collections.singletonList(value)) : super.getHeaders(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        var names = new LinkedHashSet<String>(extraHeaders.keySet());
        names.addAll(Collections.list(super.getHeaderNames()));
        return Collections.enumeration(names);
    }
}
