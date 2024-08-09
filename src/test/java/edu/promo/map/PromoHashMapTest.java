package edu.promo.map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Implementation test PromoHashMapTest")
public class PromoHashMapTest {

    final Map<Integer, Boolean> switches = new PromoHashMap<>(2);
    final Map<String, String> contacts = new PromoHashMap<>(8, 0.25F);
    final Map<Integer, String> threads = new PromoHashMap<>();

    @BeforeEach
    public void setup() {
        switches.put(0, false);
        switches.put(1, true);
        contacts.putAll(Map.of("Tracey Rodgers", "+1 (561) 487-7054", "Ronald Johns", "+1 (049) 057-4975"));
        threads.put(65, "a67");
    }

    @Test
    public void compliance() {
        assertEquals(switches.get(0), false);
        assertEquals(switches.get(1), true);
    }

    @Test
    public void size() {
        assertEquals(switches.size(), 2);
    }

    @Test
    public void contains() {
        assertTrue(contacts.containsKey("Tracey Rodgers"));
        assertTrue(contacts.containsValue("+1 (561) 487-7054"));
    }

    @Test
    public void remove() {
        assertTrue(contacts.containsKey("Tracey Rodgers"));
        contacts.remove("Tracey Rodgers");
        assertFalse(contacts.containsKey("Tracey Rodgers"));
    }

    @ValueSource(ints = 64)
    @ParameterizedTest
    public void execute(int count) {
        ExecutorService executor = Executors.newFixedThreadPool(count);
        for (int index = 0; index < count; index++) {
            int key = index;
            executor.execute(() -> {
                String value = Thread.currentThread().getName();
                threads.put(key, value);
            });
        }
        executor.shutdown();
    }

}
