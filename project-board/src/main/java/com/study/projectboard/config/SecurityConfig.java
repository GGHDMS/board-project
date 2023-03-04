package com.study.projectboard.config;

import com.study.projectboard.dto.security.BoardPrincipal;
import com.study.projectboard.dto.security.KakaoOAuth2Response;
import com.study.projectboard.service.UserAccountService;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

import java.util.UUID;

import static org.springframework.security.config.Customizer.withDefaults;


@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService
    ) throws Exception {
        return http.csrf().disable()
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .mvcMatchers(
                                HttpMethod.GET,
                                "/",
                                "/articles",
                                "/articles/search-hashtag"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(withDefaults()) // .formLogin().and()
                .logout(logout -> logout.logoutSuccessUrl("/")) // 로그아웃 .logoutSuccessUrl("/").and 로그아웃시 이동
                .oauth2Login(oAuth -> oAuth
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oAuth2UserService))
                )
                .build();
    }

//    @Bean
//    public WebSecurityCustomizer webSecurityCustomizer() { 추천하지 않는다 이렇게 하면 spring security가 전혀 관리를 하지 않는다
//        // static resource, css - js
//        return (web) -> web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
//    }

    @Bean
    public UserDetailsService userDetailsService(UserAccountService userAccountService) { // 데이터베이스 기반으로 user 정보를 가져와서 return
        return username -> userAccountService
                .searchUser(username)
                .map(BoardPrincipal::from)
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다 - username: " + username));
    }

    @Bean  // OAuth2UserService 는 interface
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService(
            UserAccountService userAccountService,
            PasswordEncoder passwordEncoder
    ) {
        final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

        return userRequest -> { // userRequest lambda 식의 입력이다.
            OAuth2User oAuth2User = delegate.loadUser(userRequest); // OAuth2UserService 를 사용하려고 할 때 구현 해줘야하는 메소드 DefaultOAuth2UserService 에 구현되어 있다.

            // 아래는 모두 kakao 응답에 맞춰 parsing을 해서 내보내는 것이다
            KakaoOAuth2Response kakaoOAuth2Response = KakaoOAuth2Response.from(oAuth2User.getAttributes());
            String registrationId = userRequest.getClientRegistration().getRegistrationId(); // 고유 값 "kakao" yml 파일에서 작성 해준 값
            String providerId = String.valueOf(kakaoOAuth2Response.getId());
            String username = registrationId + "_" + providerId; // kakaoOAuth2Response 에서는 사용할수 있는 값이 없다.
            String dummyPassword = passwordEncoder.encode("{bcrypt}" + UUID.randomUUID()); // kakao를 통해 로그인 하기 때문에 pw는 사실 필요 없지만 dp 설계가 null 이 안되니 때문에 만들어 넣어준다.

            return userAccountService.searchUser(username)
                    .map(BoardPrincipal::from)
                    .orElseGet(() -> // db 에 있었으면 그대로 진행 없으면 아래와 같이 db에 만들어 준다.
                            BoardPrincipal.from(
                                    userAccountService.saverUser(
                                            username,
                                            dummyPassword,
                                            kakaoOAuth2Response.email(),
                                            kakaoOAuth2Response.nickname(),
                                            null
                                    )
                            )
                    );
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

}
