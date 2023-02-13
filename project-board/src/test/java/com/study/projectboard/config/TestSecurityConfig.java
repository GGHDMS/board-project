package com.study.projectboard.config;

import com.study.projectboard.domain.UserAccount;
import com.study.projectboard.repository.UserAccountRepository;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.event.annotation.BeforeTestMethod;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@Import(SecurityConfig.class)
public class TestSecurityConfig {

    @MockBean
    UserAccountRepository userAccountRepository;

    @BeforeTestMethod //spring test 할 때
    public void securitySetup() {
        given(userAccountRepository.findById(anyString())).willReturn(Optional.of(UserAccount.of(
                "hsmTest",
                "pw",
                "hsm-test@gmail.com",
                "Hsm-test",
                "memo-test"
        )));
    }


}
