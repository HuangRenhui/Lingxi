package com.lingxi.config;

import com.lingxi.security.JwtAuthenticationFilter;
import com.lingxi.security.RestAccessDeniedHandler;
import com.lingxi.security.RestAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 安全配置：无状态 JWT + 统一 JSON 异常响应。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] WHITE_LIST = {
            "/health",
            "/auth/login",
            // 注销与刷新由 Controller 自行解析 Token：即使 Token 已过期也应返回统一业务错误而不是被过滤器拦截。
            "/auth/logout",
            "/auth/refresh",
            // 容器错误转发目标：未放行时匿名访问不存在路径会被判未认证，404 变成 401
            "/error",
            "/doc.html",
            "/webjars/**",
            "/favicon.ico",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-resources/**"
    };

    private static final List<String> DEFAULT_ALLOWED_ORIGINS =
            Collections.unmodifiableList(Arrays.asList("http://localhost:5173", "http://127.0.0.1:5173"));

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;
    private final List<String> allowedOrigins;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          RestAuthenticationEntryPoint authenticationEntryPoint,
                          RestAccessDeniedHandler accessDeniedHandler,
                          @Value("${lingxi.cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173}")
                          List<String> allowedOrigins) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.allowedOrigins = allowedOrigins;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 前后端通过 JWT 无状态认证，不依赖 Cookie/Session，因此关闭 CSRF 与 Session；
        // 注意：若未来引入 Cookie 认证，需重新评估 CSRF 防护。
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 浏览器跨域预检请求不携带凭证，必须放行，否则前端请求会在 OPTIONS 阶段被拦截。
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(WHITE_LIST).permitAll()
                        // 管理端接口在 URL 层再兜一层：即使新增的 Controller 忘记调用 requireAdmin()，
                        // 非管理员也只能拿到 403，避免因遗漏校验直接越权。
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                // 认证失败与权限不足分别交给两个 Handler，保证始终返回统一的 JSON 结构，
                // 而不是 Spring Security 默认的 HTML 登录页或空响应。
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                // JWT 过滤器需早于用户名密码过滤器执行，以便后续的认证判断能直接读取到 SecurityContext。
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 允许来源从配置读取，默认仅放行本地前端；不允许使用 "*" 与凭证组合，避免任意站点携带凭证访问。
        List<String> origins = new ArrayList<>();
        if (allowedOrigins != null) {
            for (String origin : allowedOrigins) {
                if (origin != null && !origin.trim().isEmpty()) {
                    origins.add(origin.trim());
                }
            }
        }
        if (origins.isEmpty()) {
            origins.addAll(DEFAULT_ALLOWED_ORIGINS);
        }
        if (origins.contains("*")) {
            throw new IllegalStateException("lingxi.cors.allowed-origins 不允许配置 *，请改为具体来源");
        }
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
