package zerobase.bud.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import zerobase.bud.oauth.service.CustomOAuth2UserService;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfiguration {
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .headers()
                    .frameOptions()
                        .disable()
                .and()
                    .authorizeRequests()
                        .antMatchers("/**", "/h2-console/**").permitAll()
                            .anyRequest().authenticated()
                .and()
                    .csrf()
                        .ignoringAntMatchers("/h2-console/**").disable()
                            .httpBasic()
                .and()
                    .sessionManagement()
                        .sessionCreationPolicy(SessionCreationPolicy.NEVER)
                .and()
                    .logout()
                        .logoutSuccessUrl("/")
                .and()
                    .oauth2Login()
                        .defaultSuccessUrl("/login/oauth2")
                            .userInfoEndpoint()
                                .userService(customOAuth2UserService);
        return http.build();
    }
}