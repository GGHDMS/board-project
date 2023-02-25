package com.study.projectboard.repository.querydsl;

import com.study.projectboard.domain.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;

public interface ArticleRepositoryCustom {
    /**
     * @deprecated 해시캐그 도메인을 새로 만들었으므로 이 코드는 더 이상 사용할 필요 없다.
     * @see HashtagRepositoryCustom #findAllDistinctHashtags()
     */

    @Deprecated
    List<String> findAllDistinctHashtags();

    Page<Article> findByHashtagNames(Collection<String> hashtagName, Pageable pageable);
}
