package com.example.mentoringapis.configurations;

import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.charset.Charset;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedHeaders("*")
                .allowedMethods("*");
    }
    @Value("${mail.mailjetApiKey}")
    private String mailjetApiKey;

    @Bean
    public MailjetClient mailjetClient() throws IOException {
        var apiSecret = new ClassPathResource("secret/mailJetApiSecret.txt")
                .getContentAsString(Charset.defaultCharset());
        return new MailjetClient(ClientOptions.builder().apiKey(mailjetApiKey).apiSecretKey(apiSecret).build());
    }
}
