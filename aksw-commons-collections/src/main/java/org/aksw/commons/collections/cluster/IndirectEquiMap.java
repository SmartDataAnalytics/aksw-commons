package org.aksw.commons.collections.cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.collections.generator.GeneratorLending;
import org.aksw.commons.collections.generator.GeneratorLendingImpl;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

/**
 * A map-like key-value data structure where keys can be stated as equivalent, and thus become aliases
 * of each other.
 * 
 * Keys can be stated as equivalent using stateEqual (raises exception on conflict) or tryStateEqual (returns the conflict).
 * Clusters can be obtained using getEquivalences() and their associated values can be set using setValue(id, value).
 * 
 * Note, that there is no distinction between null and undef values. In fact, a null value is interpreted
 * as the absence of a value, and such a cluster token will be removed from the tokenToValue map.
 * 
 * @author raven
 *
 * @param <K>
 * @param <V>
 */
public class IndirectEquiMap<K, V> {
	protected Map<K, Integer> keyToToken = new HashMap<K, Integer>();
	protected Multimap<Integer, K> tokenToKeys = LinkedHashMultimap.create(); 
	protected Map<Integer, V> tokenToValue = new HashMap<Integer, V>();
	
	// Read only view
	protected Multimap<Integer, K> tokenToKeysView = Multimaps.unmodifiableMultimap(tokenToKeys);
	

	//protected int nextToken = 0;
	protected GeneratorLending<Integer> gen = GeneratorLendingImpl.createInt();
	
	/**
	 * Export the state as a map of the sets of equivalent keys and their associated values.
	 * Mostly intended for unit testing.
	 * 
	 * @return
	 */
	public Map<Set<K>, V> dump() {
		Map<Set<K>, V> result = tokenToKeys.asMap().entrySet().stream()
			.collect(Collectors.toMap(
					e -> (Set<K>)e.getValue(),
					e -> tokenToValue.get(e.getKey())));
		
		return result;
	}
	
	public Set<K> keySet() {
		return Collections.unmodifiableSet(keyToToken.keySet());
	}
	
	public Multimap<Integer, K> getEquivalences()
	{
		return tokenToKeysView;
	}
	
//	public Map<Integer, V> getValueMap() {
//		return tokenToValue;
//	}
	
	public V getValue(Integer token) {
		return tokenToValue.get(token);
	}
	
	/**
	 * Method to set a cluster's value.
	 * Checks whether the validity of the token, i.e. whether a cluster with that id exists.
	 * 
	 * @param token
	 * @param value
	 * @return
	 */
	public V setValue(Integer token, V value) {
		boolean isValidToken = tokenToKeys.containsKey(token);
		if(!isValidToken) {
			throw new RuntimeException("There is no cluster of keys with id " + token);
		}
		
		putWithoutNull(tokenToValue, token, value);
		return value;
	}
	
	
	public Collection<K> getEquivalences(K key) {
		Integer token = keyToToken.get(key);
		Collection<K> result = tokenToKeys.get(token);
		return result;
	}
	
	
	protected void putKeyToken(K key, int token) {
		keyToToken.put(key, token);
		tokenToKeys.put(token, key);
	}
	
	
	/**
	 * Try to make keys equal. The response determines whether there ary any conflicts.
	 * 
	 * @param a
	 * @param b
	 * @return null on success, otherwise an entry of the key's conflicting values.
	 */
	public Entry<V, V> tryStateEqual(K a, K b) {
		Entry<V, V> result = tryStateEqual(a, b, null, false);
		return result;
	}

	/**
	 * State keys to be equal - raises an exception on conflicting values.
	 * 
	 * 
	 * @param a
	 * @param b
	 */
	public void stateEqual(K a, K b) {
		Entry<V, V> conflict = tryStateEqual(a, b);
		if(conflict != null) {
			throw new RuntimeException("Cannot make " + a + " and " + b + " equal due to conflicting values: " + conflict);
		}
	}


	/**
	 * State keys to be equal and sets their values.
	 * Any conflicts are resolved by setting the cluster values to the provided value. 
	 * 
	 * @param a
	 * @param b
	 * @param value
	 */
	public void stateEqual(K a, K b, V value) {
		tryStateEqual(a, b, value, true);		
	}

	/*
	public Collection<> stateEqual(Collection<K> keys) {
		
	}*/
	
	/**
	 * State keys to be equal and sets their values.
	 * Any conflicts are resolved by setting the cluster values to the provided value. 
	 * 
	 * @param keys
	 * @param value
	 */
	public void stateEqual(Collection<K> keys, V value) {
		int newToken = gen.next(); //++nextToken;
		
		for(K key : keys) {
			Integer oldToken = keyToToken.get(key);
			if(oldToken != null) {
				gen.giveBack(oldToken);
				
				tokenToValue.remove(oldToken);
				tokenToKeys.putAll(newToken, tokenToKeys.get(oldToken));
				tokenToKeys.removeAll(oldToken);
			}
			
			putKeyToken(key, newToken);
		}

		tokenToValue.put(newToken, value);
	}
	
	
	/**
	 * States an equality between keys.
	 * 
	 * if overwrite is true, conflicts can not occur as they are overwritten with value. Return value is always null.
	 * if overwrite is false, in case of conflict the pair of conflicting values is returned
	 * 
	 * Conflicts can be resolved using stateEqual(a, b, value)
	 * 
	 * 
	 * @param a
	 * @param b
	 */
	protected Entry<V, V> tryStateEqual(K a, K b, V value, boolean overwrite) {
		Integer ta = keyToToken.get(a);
		Integer tb = keyToToken.get(b);
		
		if(ta == null) {
			if(tb == null) {
				int token = gen.next(); // ++nextToken;
				
				putKeyToken(a, token);
				putKeyToken(b, token);
				
			} else {
				putKeyToken(a, tb);
			}
		} else {
			if(tb == null) {
				putKeyToken(b, ta);
			} else {
				
				V va = tokenToValue.get(ta);
				V vb = tokenToValue.get(tb);

				if(va != null && vb != null && !va.equals(vb)) {
					// Conflict: Equality stated, but two distinct values
					if(overwrite) {
						va = value;
					} else  {
						return Maps.immutableEntry(va, vb);
					}
				}
				
				if(va == null) {
					va = vb;
				}
				
				// Copy to avoid ConcurrentModificationException
				Collection<K> ka = new ArrayList<K>(tokenToKeys.get(ta));
				Collection<K> kb = new ArrayList<K>(tokenToKeys.get(tb));

				// // Merge the smaller cluster into the larger one
				// Disabled, because it turned out to be more useful to have the merge work deterministically
				// based on the given argument order.
				boolean mergeSmallerClusterIntoLangerOne = false;
				if(mergeSmallerClusterIntoLangerOne) {
					Collection<K> tmp;
					int tt;
					if(kb.size() > ka.size()) {
						tmp = ka;
						ka = kb;
						kb = tmp;
						
						tt = ta;
						ta = tb;
						tb = tt;
					}
				}

				tokenToKeys.removeAll(tb);
				tokenToValue.remove(tb);
				gen.giveBack(tb);
				for(K k : kb) {
					//tokenToKeys.remove(tb, k);
					putKeyToken(k, ta);
				}
//				tokenToValue.remove(tb);


				putWithoutNull(tokenToValue, ta, va);
//				if(va != null) {
//					tokenToValue.put(ta, va);
//				}
			}
		}

		return null;
	}
	
	
	public static <K, V> void putWithoutNull(Map<K, V> map, K key, V value) {
		if(value == null) {
			map.remove(key);
		} else {
			map.put(key, value);
		}
	}
	
	/**
	 * Adds a key.
	 * If it already exists, its associated value is left untouched.
	 * Note, that this behavior is different from put(key, null) - which overwrites the value
	 * with the absence of a value for that key.
	 * 
	 * @param key
	 */
	public void add(K key) {
		addKey(key);
	}

	/**
	 * Puts a new value, overwrites any prior value associated with the key's cluster.
	 * 
	 * 
	 * @param key
	 * @param value
	 */
	public void put(K key, V value) {
		Integer token = addKey(key);

		putWithoutNull(tokenToValue, token, value);
	}

	protected Integer addKey(K key) {
		Integer token = keyToToken.get(key);
		if(token == null) {
			token = gen.next(); // ++nextToken;
			keyToToken.put(key, token);
		}
		tokenToKeys.put(token, key);
		return token;
	}
	
	public V get(K key) {
		Integer token = keyToToken.get(key);
		V result = token == null
				? null
				: tokenToValue.get(token);
		
		return result;
	}


	public boolean isEqual(K a, K b) {
		Integer ta = keyToToken.get(a);
		boolean result = ta != null && ta.equals(keyToToken.get(b));
		return result;
	}
	
	@Override
	public String toString() {
		String result = "[";
		boolean isFirst = true;
		for(Entry<Integer, Collection<K>> entry : tokenToKeys.asMap().entrySet()) {
			if(!isFirst) {
				result += ", ";
			}
			
			result += entry.getValue() + ": " + tokenToValue.get(entry.getKey());
			isFirst = false;
		}
		result += "]";
		return result;
	}
}