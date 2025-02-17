package hello.board.view.service;

import hello.board.view.repository.ArticleViewCountRepository;
import hello.board.view.repository.ArticleViewDistributedLockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ArticleViewService {
    private final ArticleViewCountRepository articleViewCountRepository;
    private final ArticleViewCountBackUpProcessor articleViewCountBackUpProcessor;
    private final ArticleViewDistributedLockRepository articleViewDistributedLockRepository;

    private static final int BACKUP_BATCH_SIZE = 100;
    private static final Duration TTL = Duration.ofMinutes(10);

    public Long increase(Long articleId, Long userId) {
        /* 어뷰징 방지 S, 실패시 증가 X, 현재 조회수 반환 */
        if (!articleViewDistributedLockRepository.lock(articleId, userId, TTL)) {
            return articleViewCountRepository.read(articleId);
        }
        /* 어뷰징 방지 E */

        Long count = articleViewCountRepository.increase(articleId);
        if(count % BACKUP_BATCH_SIZE == 0) {
            articleViewCountBackUpProcessor.backup(articleId, count);
        }
        return count;
    }

    public Long count(Long articleId) {
        return articleViewCountRepository.read(articleId);
    }
}
