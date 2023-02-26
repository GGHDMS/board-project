package com.study.projectboard.service;

import com.study.projectboard.domain.Hashtag;
import com.study.projectboard.repository.HashtagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class HashtagService {

    private final HashtagRepository hashtagRepository;

    public Set<String> parseHashtagNames(String content) {
        if (content == null) {
            return Set.of();
        }
        Pattern pattern = Pattern.compile("#[\\w가-힣]+"); // \w -> 알파벳 + 숫자 + _ 중 하나 , 가-힣 -> 한글 전체 , + -> 한개 이상
        Matcher matcher = pattern.matcher(content.strip()); // strip 문자 앞 뒤의 공백 제거 matcher 를 이용해 정규식 적용
        Set<String> result = new HashSet<>();

        while (matcher.find()) {
            result.add(matcher.group().replace("#", "")); // #을 정규식에서 같이 parsing 했었는데 탈락시킨다.
        }

        return Set.copyOf(result); //  unmodifiable Set 을 만들어서 return 불변성을 만들어 준다.
    }

    public Set<Hashtag> findHashtagsByNames(Set<String> hashtagNames) {
        return new HashSet<>(hashtagRepository.findByHashtagNameIn(hashtagNames)); // List 로 받아서 Set 으로 바꿔 return 해준다.
    }

    @Transactional
    public void deleteHashtagWithoutArticles(Long hashtagId) { // 해당 해시태그가 아무런 게시글에도 없을 때 해시태그를 삭제시킨다.
        Hashtag hashtag = hashtagRepository.getReferenceById(hashtagId);
        if (hashtag.getArticles().isEmpty()) {
            hashtagRepository.delete(hashtag);
        }
    }
}
