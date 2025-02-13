package hello.board.comment.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
/**
 * 하위 댓글 중에서 가장 큰 path(childrenTopPath)
 * 모든 자손 댓글에서, 가장 큰 path(descendantsTopPath)
 * */
@Getter
@ToString
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentPath {
    private String path;

    // path 가 가질 수 있는 characterSet
    private static final String CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private static final int DEPTH_CHUNK_SIZE = 5;  // 경로정보 depth 당 5개
    private static final int MAX_DEPTH = 5; // 최대 depth

    // MIN_CHUNK = 00000, MAX_CHUNK = zzzzz
    private static final String MIN_CHUNK = String.valueOf(CHARSET.charAt(0)).repeat(DEPTH_CHUNK_SIZE);
    private static final String MAX_CHUNK = String.valueOf(CHARSET.charAt(CHARSET.length() -1)).repeat(DEPTH_CHUNK_SIZE);

    public static CommentPath create(String path) {
        if (isDepthOverflowed(path)) {
            throw new IllegalStateException("Depth overflowed");
        }
        CommentPath commentPath = new CommentPath();
        commentPath.path = path;
        return commentPath;
    }

    private static boolean isDepthOverflowed(String path) {     // 최대 5 depth 가 넘어가면 예외
        return callDepth(path) > MAX_DEPTH; // 5  depth 가 넘어가면 true 반환
    }

    private static int callDepth(String path) {     // depth 정보 계산 = path 길이 / 5
        return path.length() / DEPTH_CHUNK_SIZE;
    }

    public int getDepth() {     // path 의 depth 를 구하는 함수
        return callDepth(path);
    }

    public boolean isRoot() {   // 제일 상위인지
        return callDepth(path) == 1;
    }

    public String getParentPath() { // 현재 path 의 parentPath 를 구하는 함수, 끝에 5개만 잘라내면 된다.
        return path.substring(0, path.length() - DEPTH_CHUNK_SIZE);
    }

    // 현재 path 에 하위 댓글의 path 만드는 메소드
    public CommentPath createChildCommentPath(String descendantsTopPath) {
        if(descendantsTopPath == null) {    // 하위 댓글이 처음 생성 되는 상황
            return CommentPath.create(path + MIN_CHUNK);
        }
        String childrenTopPath = findChildrenTopPath(descendantsTopPath);
        return CommentPath.create(increase(childrenTopPath)); // childrenTopPath + 1
    }

    private String findChildrenTopPath(String descendantsTopPath) {
        return descendantsTopPath.substring(0, (getDepth() + 1) * DEPTH_CHUNK_SIZE);
    }

    private String increase(String path) {
        String lastChunk = path.substring(path.length() - DEPTH_CHUNK_SIZE);// path 의 가장 마지막 5개의 문자열
        if (isChunkOverflowed(lastChunk)) {
            throw new IllegalStateException("Chunk overflowed");
        }
        // overflow 가 나지 않으면 path 에서 + 1
        int charsetLength = CHARSET.length();
        int value = 0;  // lastChunk 를 10 진수로 변환하기 위한 값
        for (char ch : lastChunk.toCharArray()) {
            value = value * charsetLength + CHARSET.indexOf(ch);
        }

        value = value + 1;

        String result = ""; // value 를 62 진수로 변환
        for (int i=0; i < DEPTH_CHUNK_SIZE; i++) {
            result = CHARSET.charAt(value % charsetLength) + result;
            value /= charsetLength;
        }

        return path.substring(0, path.length() - DEPTH_CHUNK_SIZE) + result;    // 상위 댓글 경로 정보 + result

    }

    private boolean isChunkOverflowed(String lastChunk) {   // zzzzz 인지 확인
        return MAX_CHUNK.equals(lastChunk);
    }
}
