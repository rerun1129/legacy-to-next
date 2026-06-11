package com.freightos.admin.adapter.out.redis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightos.admin.application.commoncode.projection.CommonCodeSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CommonCodeRedisAdapterTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    // ObjectMapper는 실제 인스턴스 사용 — 직렬화 정확성 검증이 목적
    private final ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private CommonCodeRedisAdapter adapter;

    // @InjectMocks 이후 objectMapper 주입은 생성자 주입 아닌 필드 주입이므로
    // 직접 생성하는 방식으로 처리
    private CommonCodeRedisAdapter adapterWithRealMapper() {
        return new CommonCodeRedisAdapter(redisTemplate, objectMapper);
    }

    @Test
    void putGroupCodes_serializes_codeAndLabelAndLabelKo() throws Exception {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        CommonCodeSummary boundExp = new CommonCodeSummary(1L, "Bound", "EXP", "Export", "수출", 0, true);
        CommonCodeSummary boundImp = new CommonCodeSummary(2L, "Bound", "IMP", "Import", "수입", 1, true);

        adapterWithRealMapper().putGroupCodes("Bound", List.of(boundExp, boundImp));

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        then(valueOperations).should().set(eq("cc:Bound"), jsonCaptor.capture(), any(Duration.class));

        JsonNode root = objectMapper.readTree(jsonCaptor.getValue());
        assertThat(root.isArray()).isTrue();
        assertThat(root).hasSize(2);

        JsonNode first = root.get(0);
        assertThat(first.get("code").asText()).isEqualTo("EXP");
        assertThat(first.get("label").asText()).isEqualTo("Export");
        assertThat(first.get("labelKo").asText()).isEqualTo("수출");
    }

    @Test
    void putGroupCodes_usesKeyPrefix_cc_colon() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        adapterWithRealMapper().putGroupCodes("housebl.JobDiv", List.of());

        then(valueOperations).should().set(eq("cc:housebl.JobDiv"), anyString(), any(Duration.class));
    }

    @Test
    void putGroupCodes_ttl_is24Hours() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        ArgumentCaptor<Duration> ttlCaptor = ArgumentCaptor.forClass(Duration.class);

        adapterWithRealMapper().putGroupCodes("Bound", List.of());

        then(valueOperations).should().set(anyString(), anyString(), ttlCaptor.capture());
        assertThat(ttlCaptor.getValue()).isEqualTo(Duration.ofHours(24));
    }

    @Test
    void putGroupCodes_nullLabelKo_serializedAsNull() throws Exception {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        CommonCodeSummary noKo = new CommonCodeSummary(1L, "BlType", "ORIGINAL", "Original", null, 0, true);
        adapterWithRealMapper().putGroupCodes("BlType", List.of(noKo));

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        then(valueOperations).should().set(anyString(), jsonCaptor.capture(), any(Duration.class));

        JsonNode first = objectMapper.readTree(jsonCaptor.getValue()).get(0);
        assertThat(first.get("labelKo").isNull()).isTrue();
    }

    @Test
    void putGroupCodes_redisException_doesNotPropagate() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        willThrow(new RuntimeException("Redis down"))
                .given(valueOperations).set(anyString(), anyString(), any(Duration.class));

        // 예외가 전파되지 않아야 한다 (warn 삼킴)
        adapterWithRealMapper().putGroupCodes("Bound", List.of());

        then(valueOperations).should().set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void putGroupCodes_emptyList_stillCallsRedis() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        adapterWithRealMapper().putGroupCodes("Bound", List.of());

        then(valueOperations).should().set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void putGroupCodes_redisTemplateThrows_doesNotCallValueOps() {
        willThrow(new RuntimeException("template error")).given(redisTemplate).opsForValue();

        // 예외가 전파되지 않아야 한다
        adapterWithRealMapper().putGroupCodes("Bound", List.of());

        then(valueOperations).should(never()).set(anyString(), anyString(), any(Duration.class));
    }
}
