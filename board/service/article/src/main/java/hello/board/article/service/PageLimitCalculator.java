package hello.board.article.service;
// page 번호 활성화에 필요한 공식을 구하는 곳

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)    // 유틸성 클래스기 때문에 private 생성자 , final 클래스 지정
public final class PageLimitCalculator {

    public static Long calculatePageLimit(Long page, Long pageSize, Long movablePageCount) {
        return (((page - 1) / movablePageCount) + 1) * pageSize * movablePageCount + 1;
    }
}
