package hello.board.view.service;

import hello.board.view.entity.ArticleViewCount;
import hello.board.view.repository.ArticleViewCountBackUpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ArticleViewCountBackUpProcessor {
    private final ArticleViewCountBackUpRepository articleViewCountBackUpRepository;

    @Transactional
    public void backup(Long articleId, Long viewCount){
        int result = articleViewCountBackUpRepository.updateViewCount(articleId, viewCount);
        if(result == 0){    // 삽입된 레코드가 없을때
            articleViewCountBackUpRepository.findById(articleId)
                    .ifPresentOrElse(ignored -> { },
                            () -> articleViewCountBackUpRepository.save(ArticleViewCount.init(articleId, viewCount)));
        }
    }
}
