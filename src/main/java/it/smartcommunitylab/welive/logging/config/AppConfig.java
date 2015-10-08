package it.smartcommunitylab.welive.logging.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@Configuration
@ComponentScan(basePackages = { "it.smartcommunitylab.welive.logging" })
@PropertySource("classpath:/application.properties")
public class AppConfig {

}
