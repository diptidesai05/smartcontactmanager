package com.smart.config;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class MyConfig {

	@Bean
	public UserDetailsService getUserDetailsService() {
		
		return new UserDetailsImp();
	}
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		
		return new BCryptPasswordEncoder();
	}
	@Bean
	public DaoAuthenticationProvider authenticationPRrovider() {
		
		DaoAuthenticationProvider daoAuthenticationProvider=new DaoAuthenticationProvider();
		daoAuthenticationProvider.setUserDetailsService(getUserDetailsService());
		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
		return daoAuthenticationProvider;
		
	}
	
	// configure method
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception
	{
    	
    	httpSecurity.csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(t -> t
            .requestMatchers(antMatcher("/user/**")).hasRole("USER")
            .requestMatchers(antMatcher("/admin/**")).hasRole("ADMIN")
            //.requestMatchers(antMatcher("/**")).permitAll()).formLogin(Customizer.withDefaults());
            .requestMatchers(antMatcher("/**")).permitAll())
            .formLogin(form -> form.loginPage("/signin")
            		.loginProcessingUrl("/dologin")
            		.defaultSuccessUrl("/user/index")
            		);
    	
    	
		/*httpSecurity.csrf(csrf -> csrf.disable())
	        .authorizeHttpRequests(t -> t
                .requestMatchers(antMatcher("/user/**")).hasRole("USER")
                .requestMatchers(antMatcher("/admin/**")).hasRole("ADMIN")
                .requestMatchers(antMatcher("/**")).permitAll()
                .anyRequest()
               .authenticated())
	    .formLogin(form -> form
	    		.loginPage("/signIn") //TODO: need to check and replace
	    		.loginProcessingUrl("/doLogin") //TODO: need to check and replace
                .defaultSuccessUrl("/user/index")
                );*/
	
		return httpSecurity.build();
	}
}
