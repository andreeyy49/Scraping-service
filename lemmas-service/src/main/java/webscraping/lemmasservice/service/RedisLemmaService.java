package webscraping.lemmasservice.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import webscraping.lemmasservice.model.Lemma;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RedisLemmaService {
    private final String LEMMA_SET_KEY = "lemmas:set";
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisLemmaService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isLemmaInSet(String lemma) {
        Boolean result = redisTemplate.opsForSet().isMember(LEMMA_SET_KEY, lemma);
        return result != null && result;
    }

    public void addLemmaToSet(String lemma) {
        redisTemplate.opsForSet().add(LEMMA_SET_KEY, lemma);
    }

    public void saveLemmaToCache(String lemma, Lemma lemmaObject) {
        redisTemplate.opsForValue().set(lemma, lemmaObject);
    }

    public Map<String, Lemma> getLemmaFromCache(List<String> lemmaKeys) {
        List<Object> cachedLemmas = redisTemplate.opsForValue().multiGet(lemmaKeys);
        Map<String, Lemma> result = new HashMap<>();
        for (int i = 0; i < cachedLemmas.size(); i++) {
            if (cachedLemmas.get(i) != null) {
                result.put(lemmaKeys.get(i), (Lemma) cachedLemmas.get(i));
            }
        }
        return result;
    }

}
