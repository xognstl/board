package hello.board.comment.repository;

import hello.board.comment.entity.CommentV2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepositoryV2 extends JpaRepository<CommentV2, Long> {

    // path 가 unique 한 index 로 만들어져있기 때문에 path 로 댓글을 찾을 수 있다.
    @Query("select c from CommentV2 c where c.commentPath.path = :path")    //jqpl
    Optional<CommentV2> findByPath(@Param("path") String path);

    // descendantsTopPath
    @Query(
            value = "select path from comment_v2 " +
                    "where article_id = :articleId " +
                    " and path > :pathPrefix " +    //  자기 자신 제외
                    " and path like :pathPrefix% " +    // parentPath를 prefix로 하는 모든 자손 검색 조건
                    " order by path desc limit 1",
            nativeQuery = true
    )
    Optional<String> findDescendantsTopPath(
            @Param("articleId") Long articleId,
            @Param("pathPrefix") String pathPrefix  // parent Path
    );

    // 게시글 목록 조회
    @Query(
            value = "select comment_v2.comment_id, comment_v2.content, comment_v2.path, comment_v2.article_id,   " +
                    "comment_v2.writer_id, comment_v2.deleted, comment_v2.created_at    " +
                    "from (" +
                    "   select comment_id from comment_v2 where article_id = :articleId " +
                    "   order by path asc   " +
                    "   limit :limit offset :offset " +
                    ") t left join comment_v2 on t.comment_id = comment_v2.comment_id",
            nativeQuery = true
    )
    List<CommentV2> findAll(
            @Param("articleId") Long articleId,
            @Param("offset") Long offset,
            @Param("limit") Long limit
    );

    @Query(
            value = "select count(*) from (" +
                    "   select comment_id from comment_v2 where article_id = :articleId limit :limit" +
                    ") t",
            nativeQuery = true
    )
    Long count(@Param("articleId") Long articleId, @Param("limit") Long limit);

    // 무한 스크롤
    @Query(
            value = "select comment_v2.comment_id, comment_v2.content, comment_v2.path, comment_v2.article_id,   " +
                    "comment_v2.writer_id, comment_v2.deleted, comment_v2.created_at    " +
                    "from comment_v2 " +
                    "where article_id = :articleId  " +
                    "order by path asc  " +
                    "limit :limit",
            nativeQuery = true
    )
    List<CommentV2> findAllInfiniteScroll(
            @Param("articleId") Long articleId,
            @Param("limit") Long limit
    );


    @Query(
            value = "select comment_v2.comment_id, comment_v2.content, comment_v2.path, comment_v2.article_id,   " +
                    "comment_v2.writer_id, comment_v2.deleted, comment_v2.created_at    " +
                    "from comment_v2 " +
                    "where article_id = :articleId and path > :lastPath " +
                    "order by path asc  " +
                    "limit :limit",
            nativeQuery = true
    )
    List<CommentV2> findAllInfiniteScroll(
            @Param("articleId") Long articleId,
            @Param("lastPath") String lastPath,
            @Param("limit") Long limit
    );
}
