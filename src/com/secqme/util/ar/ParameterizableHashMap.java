package com.secqme.util.ar;

import com.secqme.domain.model.ar.Parameterizable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by edward on 20/07/2015.
 */
public class ParameterizableHashMap<K, V> extends HashMap<K, V> implements Parameterizable<K, V> {

    @Override
    public void clearParam() {
        this.clear();
    }

    @Override
    public void putParam(K key, V value) {
        this.put(key, value);
    }

    @Override
    public V removeParam(K key) {
        return this.remove(key);
    }

    @Override
    public Map<K, V> getParams() {
        return this;
    }
}
