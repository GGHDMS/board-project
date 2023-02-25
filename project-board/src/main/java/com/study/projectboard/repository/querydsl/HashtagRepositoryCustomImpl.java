package com.study.projectboard.repository.querydsl;

import com.study.projectboard.domain.Hashtag;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

import static com.study.projectboard.domain.QHashtag.hashtag;

public class HashtagRepositoryCustomImpl extends QuerydslRepositorySupport implements HashtagRepositoryCustom{

    public HashtagRepositoryCustomImpl() {
        super(Hashtag.class);
    }

    @Override
    public List<String> findAllHashtagNames() {
        return from(hashtag)
                .select(hashtag.hashtagName)
                .fetch();
    }
}
