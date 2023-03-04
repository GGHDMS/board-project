package com.study.projectboard.service;

import com.study.projectboard.domain.UserAccount;
import com.study.projectboard.dto.UserAccountDto;
import com.study.projectboard.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserAccountService {
    private final UserAccountRepository userAccountRepository;

    public Optional<UserAccountDto> searchUser(String username) { // searchUser 에 대한 결과 처리를 여기서 하지 않고 호출 코드에서 처리 하겠다.
        return userAccountRepository.findById(username).map(UserAccountDto::from);
    }

    @Transactional
    public UserAccountDto saverUser(String username, String userPassword, String email, String nickname, String memo) {
        return UserAccountDto.from(userAccountRepository.save(UserAccount.of(
                username, userPassword, email, nickname, memo, username)) // 가입자가 생성시켰다.
        );
    }
}
