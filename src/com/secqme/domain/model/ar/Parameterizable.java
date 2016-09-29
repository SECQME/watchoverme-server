package com.secqme.domain.model.ar;

import java.util.Map;

/**
 * Created by edward on 20/05/2015.
 */
public interface Parameterizable<K, V> {
    public void clearParam();
    public void putParam(K key, V value);
    public V removeParam(K key);
    public Map<K, V> getParams();
}
