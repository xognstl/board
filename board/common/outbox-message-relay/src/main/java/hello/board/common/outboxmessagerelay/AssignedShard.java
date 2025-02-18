package hello.board.common.outboxmessagerelay;

import lombok.Getter;

import java.util.List;
import java.util.stream.LongStream;

/**
 * 샤드를 애플리케이션에 균등하게 할당하기 위한 클래스
 * 애플리케이션에 할당된 shard를 List 로 반환
 */
@Getter
public class AssignedShard {
    private List<Long> shards;

    // appId : 지금 실행된 애플리케이션 Id, appIds : 코디네이터에 의해 지금 실행되는 애플리케이션의 목록
    public static AssignedShard of(String appId, List<String> appIds, long shardCount) {
        AssignedShard assignedShard = new AssignedShard();
        assignedShard.shards = assign(appId, appIds, shardCount);   //shard 할당
        return assignedShard;
    }

    private static List<Long> assign(String appId, List<String> appIds, long shardCount) {
        int appIndex = findAppIndex(appId, appIds); // 현재 애플리케이션 index
        if(appIndex == -1) {    // 할당할 샤드 x
            return List.of();   // 빈리스트 반환
        }

        long start = appIndex * shardCount / appIds.size();
        long end = (appIndex + 1) * shardCount / appIds.size() -1;
        // 인덱스를 통해 start - end 사이의 범위가 이 애플리케이션이 할당된 shard 가 된다.
        System.out.println("LongStream.rangeClosed(start, end).boxed().toList(); = " + LongStream.rangeClosed(start, end).boxed().toList());
        return LongStream.rangeClosed(start, end).boxed().toList();
    }

    // 정렬된 상태에서의 앱 목록에서 현재 애플리케이션 id , 그번호 반환
    private static int findAppIndex(String appId, List<String> appIds) {
        for (int i = 0; i < appIds.size(); i++) {
            if(appIds.get(i).equals(appId)) {
                 return i;
            }
        }
        return -1;
    }
}
