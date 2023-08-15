package ru.aloyenz.directforwarder;

public class Pair<U, V> {

    /**
     * The key element of this <code>Pair</code>
     */
    private final U key;

    /**
     * The value element of this <code>Pair</code>
     */
    private final V value;

    /**
     * Constructs a new <code>Pair</code> with the given values.
     *
     * @param key the key element
     * @param value the second value element
     */
    public Pair(U key, V value) {

        this.key = key;
        this.value = value;
    }

    /**
     * Getting a key
     * @return first the key element
     */
    public U getKey() {
        return key;
    }

    /**
     * Getting a value
     * @return first the value element
     */
    public V getValue() {
        return value;
    }
}
