package com.freightos.pms.adapter.out.mart.cancel;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mongo currentOp + killOp 을 이용해 특정 comment를 가진 op을 능동 취소한다.
 *
 * $ownOps:true로 서비스 계정 자신의 op만 조회하므로 추가 inprog 권한이 필요 없다.
 * 모든 예외는 삼키고 debug 로그만 남긴다(best-effort). maxTime 백스톱이 최후 보장이다.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "pms.mart.line-accel", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class PmsMongoOpKiller {

    private final MongoClient mongoClient;

    /**
     * 지정된 comment를 가진 진행 중인 op을 모두 즉시 종료한다.
     * 실패해도 예외를 전파하지 않는다.
     */
    public void killByComment(String comment) {
        try {
            MongoDatabase admin = mongoClient.getDatabase("admin");
            Document currentOpCmd = new Document("currentOp", 1)
                    .append("$ownOps", true)
                    .append("command.comment", comment);
            Document res = admin.runCommand(currentOpCmd);

            List<Document> inprog = res.getList("inprog", Document.class);
            if (inprog == null || inprog.isEmpty()) {
                // op이 아직 currentOp에 노출되지 않은 race — maxTime이 백스톱 역할을 한다
                log.debug("killByComment: comment={} — op not yet visible in currentOp", comment);
                return;
            }

            for (Document op : inprog) {
                Object opid = op.get("opid");
                if (opid == null) continue;
                try {
                    admin.runCommand(new Document("killOp", 1).append("op", opid));
                    log.debug("killByComment: killed opid={} comment={}", opid, comment);
                } catch (Exception killEx) {
                    // 개별 killOp 실패 — op이 이미 완료됐을 가능성(benign)
                    log.debug("killByComment: killOp failed opid={} comment={} reason={}", opid, comment, killEx.getMessage());
                }
            }
        } catch (Exception ex) {
            // currentOp 조회 자체 실패 — debug 로그만(운영 노이즈 방지)
            log.debug("killByComment: currentOp query failed comment={} reason={}", comment, ex.getMessage());
        }
    }
}
