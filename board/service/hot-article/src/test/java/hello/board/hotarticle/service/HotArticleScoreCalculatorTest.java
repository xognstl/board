package hello.board.hotarticle.service;

import hello.board.hotarticle.repository.ArticleCommentCountRepository;
import hello.board.hotarticle.repository.ArticleLikeCountRepository;
import hello.board.hotarticle.repository.ArticleViewCountRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.random.RandomGenerator;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class HotArticleScoreCalculatorTest {
    @InjectMocks
    HotArticleScoreCalculator hotArticleScoreCalculator;
    @Mock   // HotArticleScoreCalculator 에 선언된 의존성 주입
    ArticleLikeCountRepository articleLikeCountRepository;
    @Mock
    ArticleViewCountRepository articleViewCountRepository;
    @Mock
    ArticleCommentCountRepository articleCommentCountRepository;

    @Test
    void calculateTst() {
        // given
        Long articleId = 1L;
        long likeCount = RandomGenerator.getDefault().nextLong(100); // 100 미만의 난수
        long commentCount = RandomGenerator.getDefault().nextLong(100);
        long viewCount = RandomGenerator.getDefault().nextLong(100);
        given(articleLikeCountRepository.read(articleId)).willReturn(likeCount);    // 가짜객체의 응갑값이 willReturn에 반환
        given(articleViewCountRepository.read(articleId)).willReturn(viewCount);
        given(articleCommentCountRepository.read(articleId)).willReturn(commentCount);

        //when
        long score = hotArticleScoreCalculator.calculate(articleId);

        // then
        assertThat(score).isEqualTo(3 * likeCount + 2 * commentCount + 1 * viewCount);
    }
}