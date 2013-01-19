package is2.data;

import java.util.Arrays;

final public class IntIntHash {

    protected int _size;
    protected int _free;
    protected float _loadFactor;
    public int _maxSize;
    protected int _autoCompactRemovesRemaining;
    protected float _autoCompactionFactor;
    public int _set[];
    private int _values[];

    public IntIntHash() {
        this(102877, 0.5F);
    }

    public IntIntHash(int initialCapacity, float loadFactor) {
        _loadFactor = loadFactor;
        _autoCompactionFactor = loadFactor;
        setUp((int) Math.ceil(initialCapacity / loadFactor));
    }

    public int size() {
        return _size;
    }

    public void ensureCapacity(int desiredCapacity) {

        if (desiredCapacity > _maxSize - size()) {
            rehash(PrimeFinder.nextPrime((int) Math.ceil((desiredCapacity + size()) / _loadFactor) + 1));
            computeMaxSize(capacity());
        }
    }

    public void compact() {
        rehash(PrimeFinder.nextPrime((int) Math.ceil(size() / _loadFactor) + 1));
        computeMaxSize(capacity());
        if (_autoCompactionFactor != 0.0F) {
            computeNextAutoCompactionAmount(size());
        }
    }

    public void setAutoCompactionFactor(float factor) {
        if (factor < 0.0F) {
            throw new IllegalArgumentException((new StringBuilder()).append("Factor must be >= 0: ").append(factor).toString());
        } else {
            _autoCompactionFactor = factor;
        }
    }

    public float getAutoCompactionFactor() {
        return _autoCompactionFactor;
    }

    private void computeMaxSize(int capacity) {
        _maxSize = Math.min(capacity - 1, (int) Math.floor(capacity * _loadFactor));
        _free = capacity - _size;
    }

    private void computeNextAutoCompactionAmount(int size) {
        if (_autoCompactionFactor != 0.0F) {
            _autoCompactRemovesRemaining = Math.round(size * _autoCompactionFactor);
        }
    }

    protected final void postInsertHook(boolean usedFreeSlot) {
        if (usedFreeSlot) {
            _free--;
        }
        if (++_size > _maxSize || _free == 0) {
            int newCapacity = _size <= _maxSize ? capacity() : PrimeFinder.nextPrime(capacity() << 1);
            rehash(newCapacity);
            computeMaxSize(capacity());
        }
    }

    protected int calculateGrownCapacity() {
        return capacity() << 1;
    }

    protected int capacity() {
        return _values.length;
    }

    public boolean contains(int val) {
        return index(val) >= 0;
    }

    private int index(int v) {

        int length = _set.length;
        int index = Math.abs((computeHashCode(v) /*
                 * & 2147483647
                 */) % length);

        while (true) {
            // first
            long l = _set[index];
            if (l == 0) {
                //			good++;
                return -1;
            }
            // second 
            if (l == v) {
                return index;
            }
            if (--index < 0) {
                index += length;
            }
        }
        //return -1;
    }

    protected int insertionIndex(long val) {
        int length = _set.length;
        int index = Math.abs((computeHashCode(val) /*
                 * & 2147483647
                 */) % length);
        while (true) {
            if (_set[index] == 0) {
                return index;
            }
            if (_set[index] == val) {
                return -index - 1;
            }
            if (--index < 0) {
                index += length;
            }

        }
    }

    public int computeHashCode(long value) {
        return (int) ((value ^ (value & 0xffffffff00000000L) >>> 32) * 31);//0x811c9dc5 ^ // 29
    }

    protected int setUp(int initialCapacity) {
        int capacity = PrimeFinder.nextPrime(initialCapacity);
        computeMaxSize(capacity);
        computeNextAutoCompactionAmount(initialCapacity);
        _set = new int[capacity];
        _values = new int[capacity];
        return capacity;
    }

    public void put(int key, int value) {
        int index = insertionIndex(key);
        doPut(key, value, index);
    }

    private void doPut(int key, int value, int index) {
        boolean isNewMapping = true;
        if (index < 0) {
            index = -index - 1;
            isNewMapping = false;
        }
        _set[index] = key;
        _values[index] = value;
        if (isNewMapping) {
            postInsertHook(true);
        }

    }

    protected void rehash(int newCapacity) {
        int oldCapacity = _set.length;
        int oldKeys[] = _set;
        int oldVals[] = _values;
        _set = new int[newCapacity];
        _values = new int[newCapacity];
        int i = oldCapacity;

        while (true) {
            if (i-- <= 0) {
                break;
            }
            if (oldVals[i] != 0) {
                int o = oldKeys[i];
                int index = insertionIndex(o);
                _set[index] = o;
                _values[index] = oldVals[i];
            }
        }
    }
    int index = 0;

    public int get(int key) {
        int index = index(key);
        return index >= 0 ? _values[index] : 0;
    }

    public void clear() {
        _size = 0;
        _free = capacity();
        Arrays.fill(_set, 0, _set.length, 0);
        //	Arrays.fill(_values, 0, _values.length, 0);
    }

    public int remove(int key) {
        int prev = 0;
        int index = index(key);
        if (index >= 0) {
            prev = _values[index];
            _values[index] = 0;
            _set[index] = 0;
            _size--;
            if (_autoCompactionFactor != 0.0F) {
                _autoCompactRemovesRemaining--;
                if (_autoCompactRemovesRemaining <= 0) {
                    compact();
                }
            }
        }
        return prev;
    }

    public int[] getValues() {
        int vals[] = new int[size()];
        int v[] = _values;
        int i = v.length;
        int j = 0;
        do {
            if (i-- <= 0) {
                break;
            }
            if (v[i] != 0) {
                vals[j++] = v[i];
            }
        } while (true);
        return vals;
    }

    public int[] keys() {
        int keys[] = new int[size()];
        int k[] = _set;
        //   byte states[] = _states;
        int i = k.length;
        int j = 0;
        do {
            if (i-- <= 0) {
                break;
            }
            if (k[i] != 0) {
                keys[j++] = k[i];
            }
        } while (true);
        return keys;
    }

    /**
     * @param index2
     * @param i
     * @return
     */
    public boolean adjustValue(int key, int i) {
        int index = index(key);
        if (index >= 0) {
            _values[index] += i;
            return true;
        }
        return false;
    }
}