package com.projects.razorpay.common_lib.web;

import com.projects.razorpay.common_lib.context.MerchantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@RequiredArgsConstructor
public class MerchantContextFilter extends OncePerRequestFilter {

    public static final String MERCHANT_ID_HEADER = "X-Merchant-Id";
    public static final String KEY_ID_HEADER = "X-Key-Id";

    private final MerchantContext merchantContext;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String merchantIdHeader = request.getHeader(MERCHANT_ID_HEADER);

        if(merchantIdHeader != null && !merchantIdHeader.isBlank()) {
            merchantContext.setMerchantId(UUID.fromString(merchantIdHeader));
        }

        String keyIdHeader = request.getHeader(KEY_ID_HEADER);

        if(keyIdHeader != null && !keyIdHeader.isBlank()) {
            merchantContext.setKeyId(keyIdHeader);
        }

        filterChain.doFilter(request, response);
    }
}
