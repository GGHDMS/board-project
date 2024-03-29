package com.study.projectboard.dto.security;

import com.study.projectboard.dto.UserAccountDto;
import lombok.Data;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class BoardPrincipal implements UserDetails, OAuth2User {
    private String username;
    private String password;
    Collection<? extends GrantedAuthority> authorities;
    private String email;
    private String nickname;
    private String memo;
    private Map<String, Object> oAuth2Attribute;


    private BoardPrincipal(String username, String password, Collection<? extends GrantedAuthority> authorities, String email, String nickname, String memo, Map<String, Object> oAuth2Attribute) {
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.email = email;
        this.nickname = nickname;
        this.memo = memo;
        this.oAuth2Attribute = oAuth2Attribute;
    }

    public static BoardPrincipal of(String username, String password, String email, String nickname, String memo) {
        return BoardPrincipal.of(username, password, email, nickname, memo, Map.of());
    }

    public static BoardPrincipal of(String username, String password, String email, String nickname, String memo, Map<String, Object> oAuth2Attribute) {
        Set<RoleType> roleTypes = Set.of(RoleType.USER);

        return new BoardPrincipal(
                username,
                password,
                roleTypes.stream()
                        .map(RoleType::getName)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toUnmodifiableSet()),
                email,
                nickname,
                memo,
                oAuth2Attribute
        );
    }

    public static BoardPrincipal from(UserAccountDto dto) {
        return BoardPrincipal.of(
                dto.getUserId(),
                dto.getUserPassword(),
                dto.getEmail(),
                dto.getNickname(),
                dto.getMemo()
        );
    }

    public UserAccountDto toDto(){
        return UserAccountDto.of(
                username,
                password,
                email,
                nickname,
                memo
        );
    }


    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


    // OAuth2 에서 필요하다.
    @Override
    public Map<String, Object> getAttributes() { return getOAuth2Attribute(); }
    // OAuth2 에서 필요하다.
    @Override
    public String getName() { return getUsername(); }

    @Getter
    public enum RoleType {
        USER("ROLE_USER");

        private final String name;

        RoleType(String name) {
            this.name = name;
        }
    }
}
