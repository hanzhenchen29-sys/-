package org.example.demo1.service.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.demo1.mapper.RepairCategoryMapper;
import org.example.demo1.pojo.entity.RepairCategory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CategoryCacheService {
    private static final String ENABLED_KEY = "category:enabled:list";
    private static final long TTL_MINUTES = 10;

    private final StringRedisTemplate redisTemplate;
    private final RepairCategoryMapper repairCategoryMapper;
    private final ObjectMapper objectMapper;

    public CategoryCacheService(StringRedisTemplate redisTemplate,
                                RepairCategoryMapper repairCategoryMapper,
                                ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.repairCategoryMapper = repairCategoryMapper;
        this.objectMapper = objectMapper;
    }

    private final Object rebuildLock = new Object();

    public List<RepairCategory> getEnabledCategories() {
        String cached = redisTemplate.opsForValue().get(ENABLED_KEY);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, new TypeReference<>() {
                });
            } catch (JsonProcessingException e) {
                redisTemplate.delete(ENABLED_KEY);
            }
        }
        // 本地互斥，防止缓存击穿：多线程同时 miss 时只允许一个线程重建缓存
        synchronized (rebuildLock) {
            cached = redisTemplate.opsForValue().get(ENABLED_KEY);
            if (cached != null) {
                try {
                    return objectMapper.readValue(cached, new TypeReference<>() {
                    });
                } catch (JsonProcessingException e) {
                    redisTemplate.delete(ENABLED_KEY);
                }
            }
            List<RepairCategory> categories = repairCategoryMapper.findEnabled();
            try {
                redisTemplate.opsForValue().set(ENABLED_KEY, objectMapper.writeValueAsString(categories), TTL_MINUTES, TimeUnit.MINUTES);
            } catch (JsonProcessingException ignored) {
                // 缓存失败不影响主流程
            }
            return categories;
        }
    }

    public void evictEnabledCache() {
        redisTemplate.delete(ENABLED_KEY);
    }
}
