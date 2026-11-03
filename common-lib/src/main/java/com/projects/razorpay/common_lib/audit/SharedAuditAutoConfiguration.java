package com.projects.razorpay.common_lib.audit;

import com.projects.razorpay.common_lib.context.MerchantContext;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;

@AutoConfiguration
public class SharedAuditAutoConfiguration {

    @Bean("auditorAwareImpl")
    public AuditorAware<String> auditorAware(MerchantContext merchantContext){
        return new AuditorAwareImpl(merchantContext);
    }
}
