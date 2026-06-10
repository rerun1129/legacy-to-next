package com.freightos.pms.adapter.out.mart.countindex;

import lombok.extern.slf4j.Slf4j;
import org.roaringbitmap.RoaringBitmap;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Count Index Redis 쓰기 전담 헬퍼 (package-private).
 *
 * PmsCountIndexMaintainer의 500줄 근접 방지 목적으로 분리된 클래스.
 * 비트맵 diff 적용(applyBitmapDiff), collapse hash diff 적용,
 * 네임스페이스 flush(flushAll) 로직을 담는다.
 */
@Slf4j
final class PmsCountIndexRedisWriter {

    /** pipeline 당 최대 커맨드 수. */
    private static final int PIPELINE_CHUNK_SIZE = 2000;

    private final RedisTemplate<String, byte[]> redisTemplate;
    private final String prefix;

    PmsCountIndexRedisWriter(RedisTemplate<String, byte[]> redisTemplate, String prefix) {
        this.redisTemplate = redisTemplate;
        this.prefix        = prefix;
    }

    // ── 비트맵 diff 적용 ──────────────────────────────────────────────────────

    /**
     * 비트맵 키별 GET→add/remove→SET (pipeline 청크 분할).
     * ≤PIPELINE_CHUNK_SIZE 커맨드 단위로 실행한다.
     */
    void applyBitmapDiff(Map<String, List<Integer>> toAdd, Map<String, List<Integer>> toRemove) {
        Set<String> affectedKeys = new HashSet<>();
        affectedKeys.addAll(toAdd.keySet());
        affectedKeys.addAll(toRemove.keySet());

        List<String> keyList = new ArrayList<>(affectedKeys);
        for (int start = 0; start < keyList.size(); start += PIPELINE_CHUNK_SIZE) {
            int end = Math.min(start + PIPELINE_CHUNK_SIZE, keyList.size());
            List<String> chunk = keyList.subList(start, end);
            List<byte[]> existing = redisTemplate.opsForValue().multiGet(chunk);
            if (existing == null) continue;

            Map<String, byte[]> updates = new HashMap<>(chunk.size() * 2);
            for (int i = 0; i < chunk.size(); i++) {
                String bitmapKey = chunk.get(i);
                RoaringBitmap bitmap = PmsCountIndexMaintainer.deserialize(existing.get(i));
                for (int ord : toAdd.getOrDefault(bitmapKey, List.of()))    bitmap.add(ord);
                for (int ord : toRemove.getOrDefault(bitmapKey, List.of())) bitmap.remove(ord);
                updates.put(bitmapKey, PmsCountIndexMaintainer.serialize(bitmap));
            }
            redisTemplate.executePipelined((RedisCallback<Object>) conn -> {
                updates.forEach((k, v) -> conn.stringCommands().set(k.getBytes(StandardCharsets.UTF_8), v));
                return null;
            });
        }
    }

    // ── collapse hash diff 적용 ───────────────────────────────────────────────

    void applyCollapseHashDiff(Map<String, String> collapseToAdd, List<String> collapseToRemove) {
        String hashKey = PmsCountIndexKeys.docCollapseHash(prefix);
        if (!collapseToRemove.isEmpty()) {
            redisTemplate.opsForHash().delete(hashKey, collapseToRemove.toArray());
        }
        if (!collapseToAdd.isEmpty()) {
            Map<String, byte[]> entries = new HashMap<>(collapseToAdd.size() * 2);
            for (Map.Entry<String, String> e : collapseToAdd.entrySet()) {
                entries.put(e.getKey(), e.getValue().getBytes(StandardCharsets.UTF_8));
            }
            redisTemplate.opsForHash().putAll(hashKey, entries);
        }
    }

    // ── 네임스페이스 flush ────────────────────────────────────────────────────

    /**
     * prefix 하위 모든 키를 SCAN+UNLINK로 삭제한다(bulk rebuild 전 flush).
     * KEYS 대신 SCAN으로 대용량 네임스페이스도 안전하게 처리한다.
     */
    void flushAll() {
        try {
            String pattern = PmsCountIndexKeys.scanPattern(prefix);
            long deleted   = 0;
            ScanOptions opts = ScanOptions.scanOptions().match(pattern).count(200).build();
            try (org.springframework.data.redis.core.Cursor<byte[]> c =
                    redisTemplate.getConnectionFactory().getConnection().scan(opts)) {
                List<String> batch = new ArrayList<>(200);
                while (c.hasNext()) {
                    batch.add(new String(c.next(), StandardCharsets.UTF_8));
                    if (batch.size() >= 200) {
                        redisTemplate.unlink(batch);
                        deleted += batch.size();
                        batch.clear();
                    }
                }
                if (!batch.isEmpty()) {
                    redisTemplate.unlink(batch);
                    deleted += batch.size();
                }
            }
            log.info("Count Index flushAll: {}개 키 삭제", deleted);
        } catch (Exception e) {
            log.warn("Count Index flushAll 실패: {}", e.toString());
        }
    }
}
