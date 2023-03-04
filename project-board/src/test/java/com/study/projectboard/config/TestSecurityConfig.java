package com.study.projectboard.config;

import com.study.projectboard.dto.UserAccountDto;
import com.study.projectboard.service.UserAccountService;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.event.annotation.BeforeTestMethod;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@Import(SecurityConfig.class)
public class TestSecurityConfig {

    @MockBean
    UserAccountService userAccountService;

    @BeforeTestMethod //spring test 할 때
    public void securitySetup() {
        given(userAccountService.searchUser(anyString()))
                .willReturn(Optional.of(createUserAccountDto()));
        given(userAccountService.saverUser(anyString(), anyString(), anyString(), anyString(), anyString()))
                .willReturn(createUserAccountDto());
    }

    private UserAccountDto createUserAccountDto(){
        return UserAccountDto.of(
                "hsmTest",
                "pw",
                "hsm-test@gmail.com",
                "Hsm-test",
                "memo-test"
        );
    }

}
