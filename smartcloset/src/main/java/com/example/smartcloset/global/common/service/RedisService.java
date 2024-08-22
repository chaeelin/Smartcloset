package com.example.smartcloset.global.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final StringRedisTemplate stringRedisTemplate;

    public void addReportCount(Long commentId) {
        stringRedisTemplate.opsForValue().increment("reportCount:" + commentId);
    }

    public Set<String> getAllKeys() {
        return stringRedisTemplate.keys("reportCount:*");
    }

    public Map<Long, Integer> getReportCount(List<Long> onlyKeys) {
        Map<Long, Integer> commentIdAndReportCount = new HashMap<>();
        for (Long key : onlyKeys) {
            int reportCount = Integer.parseInt(Objects.requireNonNull(
                    stringRedisTemplate.opsForValue().get("reportCount:" + key)));
            commentIdAndReportCount.put(key, reportCount);
        }
        return commentIdAndReportCount;
    }
}
