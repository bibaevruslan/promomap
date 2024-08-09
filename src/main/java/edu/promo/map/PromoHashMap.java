package edu.promo.map;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.ArrayList;

import java.util.Objects;
import java.util.Arrays;

public class PromoHashMap<K, V> implements Map<K, V> {

    private static final int DEFAULT_INITIAL_CAPACITY = 24;
    private static final int POSITIVE_MASK = 0x7FFFFFFF;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private int size;
    private int threshold;
    private final float loadFactor;
    private Node<K, V>[] table;

    public PromoHashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    public PromoHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    @SuppressWarnings("unchecked")
    public PromoHashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal capacity: " + initialCapacity);
        }
        if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Illegal load: " + loadFactor);
        }
        if (initialCapacity == 0)
            initialCapacity = 1;
        this.loadFactor = loadFactor;
        this.table = (Node<K, V>[]) new Node[initialCapacity];
        this.threshold = (int) Math.min(initialCapacity * loadFactor, MAX_ARRAY_SIZE + 1);
    }


    static class Node<K, V> implements Map.Entry<K, V> {

        K key;
        V value;
        final int hash;
        Node<K, V> next;

        Node(K key, V value, int hash, Node<K, V> next) {
            this.key = key;
            this.value = value;
            this.hash = hash;
            this.next = next;
        }

        @Override
        public K getKey() {
            return this.key;
        }

        @Override
        public V getValue() {
            return this.value;
        }

        @Override
        public final V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        public final int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }

        @Override
        public String toString() {
            return "(%s=%s, hash=%d, next=%s)".formatted(key, value, hash, next);
        }

    }

    @Override
    public synchronized int size() {
        return this.size;
    }

    @Override
    public synchronized boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public synchronized boolean containsKey(Object key) {
        Objects.requireNonNull(key);
        Node<K, V>[] nodes = this.table;
        int hash = key.hashCode();
        int index = (hash & POSITIVE_MASK) % nodes.length;
        for (Node<K, V> node = nodes[index]; node != null; node = node.next) {
            if ((node.hash == hash) && node.key.equals(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized boolean containsValue(Object value) {
        Node<K, V>[] nodes = table;
        for (int index = nodes.length; index-- > 0; ) {
            for (Node<K, V> node = nodes[index]; node != null; node = node.next) {
                if (node.value.equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public synchronized V get(Object key) {
        Node<K, V>[] nodes = table;
        int hash = key.hashCode();
        int index = (hash & POSITIVE_MASK) % nodes.length;
        for (Node<K, V> node = nodes[index]; node != null; node = node.next) {
            if ((node.hash == hash) && node.key.equals(key)) {
                return node.value;
            }
        }
        return null;
    }

    @Override
    public synchronized V put(K key, V value) {
        Objects.requireNonNull(value);
        Node<K, V>[] nodes = table;
        int hash = key.hashCode();
        int index = (hash & POSITIVE_MASK) % nodes.length;
        Node<K, V> node = nodes[index];
        for (; node != null; node = node.next) {
            if ((node.hash == hash) && node.key.equals(key)) {
                V old = node.value;
                node.value = value;
                return old;
            }
        }
        this.addEntry(hash, key, value, index);
        return null;
    }

    private void addEntry(int hash, K key, V value, int index) {
        Node<K, V>[] nodes = this.table;
        if (this.size >= threshold) {
            this.resize();
            nodes = this.table;
            hash = key.hashCode();
            index = (hash & POSITIVE_MASK) % nodes.length;
        }
        Node<K, V> node = nodes[index];
        nodes[index] = new Node<>(key, value, hash, node);
        this.size++;
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        int currentCapacity = this.table.length;
        Node<K, V>[] currentTable = this.table;

        int newCapacity = (currentCapacity << 1) + 1;
        if (newCapacity - MAX_ARRAY_SIZE > 0) {
            if (currentCapacity == MAX_ARRAY_SIZE)
                return;
            newCapacity = MAX_ARRAY_SIZE;
        }
        Node<K, V>[] newMap = (Node<K, V>[]) new Node[newCapacity];

        this.threshold = (int) Math.min(newCapacity * loadFactor, MAX_ARRAY_SIZE + 1);
        this.table = newMap;

        for (int index = currentCapacity; index-- > 0;) {
            for (Node<K, V> old = currentTable[index]; old != null ;) {
                Node<K, V> node = old;
                old = old.next;

                int newIndex = (node.hash & POSITIVE_MASK) % newCapacity;
                node.next = newMap[newIndex];
                newMap[newIndex] = node;
            }
        }
    }

    @Override
    public synchronized V remove(Object key) {
        Node<K, V>[] nodes = this.table;
        int hash = key.hashCode();
        int index = (hash & POSITIVE_MASK) % nodes.length;
        Node<K, V> node = nodes[index];
        for (Node<K, V> previous = null; node != null; previous = node, node = node.next) {
            if ((node.hash == hash) && node.key.equals(key)) {
                if (previous != null) {
                    previous.next = node.next;
                } else {
                    nodes[index] = node.next;
                }
                this.size--;
                V oldValue = node.value;
                node.value = null;
                return oldValue;
            }
        }
        return null;
    }

    @Override
    public synchronized void putAll(Map<? extends K, ? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet())
            this.put(entry.getKey(), entry.getValue());
    }

    @Override
    public synchronized void clear() {
        if (this.size > 0) {
            Arrays.fill(this.table, null);
            this.size = 0;
        }
    }

    @Override
    public Set<K> keySet() {
        final Set<K> keys = new HashSet<>();
        Node<K, V>[] nodes = this.table;
        for (int index = nodes.length; index-- > 0; ) {
            for (Node<K, V> node = nodes[index]; node != null; node = node.next) {
                keys.add(node.key);
            }
        }
        return keys;
    }

    @Override
    public Collection<V> values() {
        final Collection<V> values = new ArrayList<>();
        Node<K, V>[] nodes = this.table;
        for (int index = nodes.length; index-- > 0; ) {
            for (Node<K, V> node = nodes[index]; node != null; node = node.next) {
                values.add(node.value);
            }
        }
        return values;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        final Set<Entry<K, V>> entries = new HashSet<>();
        Node<K, V>[] nodes = this.table;
        for (int index = nodes.length; index-- > 0; ) {
            for (Node<K, V> node = nodes[index]; node != null; node = node.next) {
                entries.add(node);
            }
        }
        return entries;
    }

}
