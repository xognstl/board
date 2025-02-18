package hello.board.common.outboxmessagerelay;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class AssignedShardTest {

    @Test
    void ofTest() {
        // given
        Long shardCount = 64L;
        List<String> appList = List.of("appId1", "appId2", "appId3");//코디네이터에 살아있다고 떠있는 애플리케이션 수

        // when
        AssignedShard assignedShard1 = AssignedShard.of(appList.get(0), appList, shardCount);// 각 애플리케이션에 샤드를 할당
        AssignedShard assignedShard2 = AssignedShard.of(appList.get(1), appList, shardCount);
        AssignedShard assignedShard3 = AssignedShard.of(appList.get(2), appList, shardCount);
        AssignedShard assignedShard4 = AssignedShard.of("invalid", appList, shardCount);
        // then
        //4개의 assignedShard 에서 shardList 를 꺼내서 하나의 리스트로
        List<Long> result = Stream.of(assignedShard1.getShards(), assignedShard2.getShards(),
                        assignedShard3.getShards(), assignedShard4.getShards())
                .flatMap(List::stream)
                .toList();
        System.out.println("result = " + result);
        assertThat(result).hasSize(shardCount.intValue());
        
        for(int i=0; i<shardCount.intValue(); i++) {
            assertThat(result.get(i)).isEqualTo(i);
        }

        assertThat(assignedShard4.getShards()).isEmpty();

    }
}