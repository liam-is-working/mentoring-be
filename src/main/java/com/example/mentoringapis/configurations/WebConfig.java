package com.example.mentoringapis.configurations;

import com.example.mentoringapis.entities.AppConfig;
import com.example.mentoringapis.repositories.AppConfigRepository;
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
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

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
    @Value("${mail.mailjetEnable}")
    private Boolean mailjetEnable;

    @Bean
    public MailjetClient mailjetClient() throws IOException {
        //TODO enable
        if(!mailjetEnable)
            return new MailjetClient(ClientOptions.builder().apiKey("dummy").apiSecretKey("dummy").build());

        var apiSecret = new ClassPathResource("secret/mailJetApiSecret.txt")
                .getContentAsString(Charset.defaultCharset());
        return new MailjetClient(ClientOptions.builder().apiKey(mailjetApiKey).apiSecretKey(apiSecret).build());
    }

    @Bean(name = "appConfig")
    public AppConfig appConfig(AppConfigRepository appConfigRepository){
        var configs = appConfigRepository.findAll();
        return configs.stream().max(Comparator.comparingInt(AppConfig::getId)).orElse(null);

    }
}
