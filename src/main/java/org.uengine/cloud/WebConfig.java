package org.uengine.cloud;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.uengine.cloud.tenant.TenantAwareFilter;

/**
 * Created by uengine on 2017. 11. 1..
 */
@Configuration
@EnableWebMvc
public class WebConfig extends WebMvcConfigurerAdapter{

    @Bean
    public TenantAwareFilter tenantAwareFilter() {
        return new TenantAwareFilter();
    }

    @Bean
    public RestFilter restFilter() {
        return new RestFilter();
    }

    @Bean
    public CorsFilter corsFilter() {
        return new CorsFilter();
    }
}
