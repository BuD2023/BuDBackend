package zerobase.bud.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("https://stately-yeot-007fa8.netlify.app/")
                .allowedOrigins("https://mlf.vercel.app/")
                .allowedOrigins("http://localhost:8080/")
                .allowedOrigins("http://localhost:5173/")
                .allowedMethods("GET", "POST", "DELETE", "PUT")
                .allowCredentials(false);
    }
}
