package vn.productstore;

import java.util.Locale;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  @Bean
  UserDetailsService userDetailsService(JdbcTemplate db) {
    return email ->
        (UserDetails)
            db
                .query(
                    "SELECT *, CASE WHEN locked_until>SYSUTCDATETIME() THEN 1 ELSE 0 END locked"
                        + " FROM Users WHERE email=?",
                    (r, n) ->
                        User.withUsername((String) r.getString("email"))
                            .password(r.getString("password_hash"))
                            .roles(new String[] {r.getString("role")})
                            .accountLocked(r.getBoolean("locked"))
                            .build(),
                    new Object[] {email.strip().toLowerCase(Locale.ROOT)})
                .stream()
                .findFirst()
                .orElseThrow(
                    () ->
                        new UsernameNotFoundException(
                            "Tài khoản không hợp lệ"));
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http, JdbcTemplate db) throws Exception {
    http.authorizeHttpRequests(
            a ->
                ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)
                        ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)
                                ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)
                                        a.requestMatchers(
                                            new String[] {
                                              "/css/**",
                                              "/js/**",
                                              "/images/**",
                                              "/favicon.svg",
                                              "/",
                                              "/products",
                                              "/products/*",
                                              "/login",
                                              "/register",
                                              "/error"
                                            }))
                                    .permitAll()
                                    .requestMatchers(new String[] {"/admin/**"}))
                            .hasRole("ADMIN")
                            .anyRequest())
                    .authenticated())
        .formLogin(
            f ->
                ((FormLoginConfigurer)
                        ((FormLoginConfigurer)
                                ((FormLoginConfigurer)
                                        f.loginPage("/login")
                                            .usernameParameter("email")
                                            .defaultSuccessUrl("/", false))
                                    .successHandler(
                                        (request, response, auth) -> {
                                          db.update(
                                              "UPDATE Users SET failed_attempts=0,locked_until=NULL"
                                                  + " WHERE email=?",
                                              new Object[] {auth.getName()});
                                          SavedRequestAwareAuthenticationSuccessHandler handler =
                                              new SavedRequestAwareAuthenticationSuccessHandler();
                                          handler.setDefaultTargetUrl(
                                              auth.getAuthorities().stream()
                                                      .anyMatch(
                                                          a ->
                                                              a.getAuthority().equals("ROLE_ADMIN"))
                                                  ? "/admin"
                                                  : "/");
                                          handler.onAuthenticationSuccess(request, response, auth);
                                        }))
                            .failureHandler(
                                (request, response, error) -> {
                                  String email = request.getParameter("email");
                                  if (email != null && email.length() <= 254) {
                                    db.update(
                                        "UPDATE Users SET failed_attempts=CASE WHEN"
                                            + " locked_until<=SYSUTCDATETIME() THEN 1 ELSE"
                                            + " failed_attempts+1 END, locked_until=CASE WHEN"
                                            + " locked_until>SYSUTCDATETIME() THEN locked_until"
                                            + " WHEN locked_until<=SYSUTCDATETIME() THEN NULL WHEN"
                                            + " failed_attempts>=4 THEN"
                                            + " DATEADD(minute,15,SYSUTCDATETIME()) ELSE NULL END"
                                            + " WHERE email=?",
                                        new Object[] {email.strip().toLowerCase(Locale.ROOT)});
                                  }
                                  response.sendRedirect(request.getContextPath() + "/login?error");
                                }))
                    .permitAll())
        .logout(
            l ->
                l.logoutSuccessUrl("/login?logout")
                    .invalidateHttpSession(true)
                    .deleteCookies(new String[] {"JSESSIONID"}))
        .headers(
            h ->
                h.contentSecurityPolicy(
                    c ->
                        c.policyDirectives(
                            "default-src 'self'; img-src 'self' https: data:; style-src 'self';"
                                + " script-src 'self'; base-uri 'self'; form-action 'self';"
                                + " frame-ancestors 'self'")));
    return (SecurityFilterChain) http.build();
  }
}
