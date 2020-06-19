package inescid.util;

import java.util.concurrent.ConcurrentHashMap;

public final class LruCache<K, V> {
	/**
	 * A doubly-linked-list implementation to save objects into the hashmap as
	 * key-value pari.
	 * 
	 * @author sunil
	 *
	 * @param <K>
	 * @param <V>
	 */
	private static class Node<K, V> {
		private V value;
		private K key;
		private Node<K, V> next, prev;

		public Node(K key, V value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public String toString() {
			return value.toString();
		}
	}

	/**
	 * The maximum number of elements that can be cached, should be set during
	 * instantiation time.
	 */
	private final int maxCapacity;
	/**
	 * Use {@linkplain ConcurrentHashMap} here to maintain the cache of objects.
	 * Also this offers thread safe access of the cache.
	 */
	private ConcurrentHashMap<K, Node<K, V>> map;
	/**
	 * A key-value representation of the cache object identified by a cache key.
	 * This is actually a doubly-linked list which maintains the most recently
	 * accessed objects (read/write) at the tail-end and the least read objects at
	 * the head.
	 */
	private Node<K, V> head, tail;

	public LruCache(int maxCapacity) {
		this(16, maxCapacity);
	}

	public LruCache(int initialCapacity, int maxCapacity) {
		this.maxCapacity = maxCapacity;
		if (initialCapacity > maxCapacity)
			initialCapacity = maxCapacity;
		map = new ConcurrentHashMap<>(initialCapacity);
	}

	/**
	 * Removes a node from the head position doubly-linked list.
	 * 
	 * @param node
	 */
	private void removeNode(Node<K, V> node) {
		if (node == null)
			return;
		if (node.prev != null) {
			node.prev.next = node.next;
		} else {
			head = node.next;
		}
		if (node.next != null) {
			node.next.prev = node.prev;
		} else {
			tail = node.prev;
		}
	}

	/**
	 * Offers a node to the tail-end of the doubly-linked list because it was
	 * recently read or written.
	 * 
	 * @param node
	 */
	private void offerNode(Node<K, V> node) {
		if (node == null)
			return;
		if (head == null) {
			head = tail = node;
		} else {
			tail.next = node;
			node.prev = tail;
			node.next = null;
			tail = node;
		}
	}

	/**
	 * Adds a new object to the cache. If the cache size has reached it's capacity,
	 * then the least recently accessed object will be evicted.
	 * 
	 * @param key
	 * @param value
	 */
	public void put(K key, V value) {
		if (map.contains(key)) {
			Node<K, V> node = map.get(key);
			node.value = value;
			removeNode(node);
			offerNode(node);
		} else {
			if (map.size() == maxCapacity) {
				System.out.println("maxCapacity of cache reached");
				map.remove(head.key);
				removeNode(head);
			}
			Node<K, V> node = new Node<K, V>(key, value);
			offerNode(node);
			map.put(key, node);
		}
	}

	/**
	 * Fetches an object from the cache (could be null if no such mapping exists).
	 * If the object is found in the cache, then it will be moved to the tail-end of
	 * the doubly-linked list to indicate that it was recently accessed.
	 * 
	 * @param key
	 * @param value
	 */
	public V get(K key) {
		Node<K, V> node = map.get(key);
		removeNode(node);
		offerNode(node);
		return node != null ? node.value : null;
	}

	/**
	 * Utility function to print the cache objects.
	 */
	public void printCache() {
		Node<K, V> curr = head;
		while (curr != null) {
			System.out.print(curr.value + " -> ");
			curr = curr.next;
		}
		System.out.println();
	}

	/**
	 * Runner program to test the LRU cache
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		/**
		 * 1. create LruCache of initial capacity 10 2. insert 10 objects to cache 3.
		 * print the cache objects 4. access the first object and print the cache 5.
		 * insert new objects to cache 6. print the cache and observe that the least
		 * recently used objects are evicted
		 */
// 1. initiate the cache with capacity 10
		LruCache<String, String> cache = new LruCache<String, String>(10);
// 2. insert 10 objects to cache
		for (int i = 1; i <= 10; i++) {
			cache.put(String.format("key-%d", i), String.format("value-%d", i));
		}
// 3. print the cache objects
		System.out.println("printing cache:");
		cache.printCache();
// 4. access the first object and print the cache
		cache.get("key-1");
		System.out.println("printing cache after accessing key-1:");
		cache.printCache();
// 5. insert new objects to cache
		for (int i = 11; i <= 15; i++) {
			cache.put(String.format("key-%d", i), String.format("value-%d", i));
		}
// 6. print the cache and observe that the least recently used objects are evicted
		System.out.println("printing cache after adding new objects:");
		cache.printCache();
	}
}