package hashmap;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K,V> implements Map61B<K, V> {
    @Override
    public Iterator<K> iterator() {
        return keySet().iterator();
    }

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Double loadFactor;
    private Collection<Node>[] buckets;
    private Set<K> keySet;
    private int N;

    // You should probably define some more!

    /** Constructors */
    public MyHashMap() {
        buckets=createTable(16);
        loadFactor=0.75;
        this.keySet=new HashSet<>();
    }

    public MyHashMap(int initialSize) {
        buckets=createTable(initialSize);
        loadFactor=0.75;
        this.keySet=new HashSet<>();
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        buckets=createTable(initialSize);
        this.loadFactor=maxLoad;
        this.keySet=new HashSet<>();
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key,value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new LinkedList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        Collection<Node> []table=new Collection[tableSize];
        for (int i = 0; i < table.length; i++) {
            table[i]=this.createBucket();
        }
        return table;
    }
    private void resize(){
        if(N/buckets.length>2){
            Collection<Node>[]newBuckets=createTable(buckets.length*2);
            Iterator<K>it=keySet.iterator();
            while(it.hasNext()){
                K tempKey=it.next();
                V tempVal=get(tempKey);
                int hash=Math.floorMod(tempKey.hashCode(),newBuckets.length);
                newBuckets[hash].add(new Node(tempKey,tempVal));
            }
            this.buckets=newBuckets;
        }
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!
    @Override
    public void clear() {
        buckets=createTable(16);
        this.keySet=new HashSet<>();
        this.N=0;
    }

    @Override
    public boolean containsKey(K key) {
        if(keySet.contains(key)){
            return true;
        }return false;
    }

    @Override
    public V get(K key) {
        int thisHash=Math.floorMod(key.hashCode(),buckets.length);
        Iterator<Node> it=  buckets[thisHash].iterator();
        while(it.hasNext()){
            Node t=it.next();
            if(t.key.equals(key)){
                return t.value;
            }
        }
        return null;
    }

    @Override
    public int size() {
        return this.N;
    }

    @Override
    public void put(K key, V value) {
        int thisHash=Math.floorMod(key.hashCode(),buckets.length);
        if(!containsKey(key)){
            buckets[thisHash].add(new Node(key,value));
            keySet.add(key);
            N++;
            resize();
        }else {
            Iterator<Node> it=  buckets[thisHash].iterator();
            while(it.hasNext()){
                Node t=it.next();
                if(t.key.equals(key)){
                    t.value=value;
                }
            }
        }
    }

    @Override
    public Set<K> keySet() {
        return this.keySet;
    }

    @Override
    public V remove(K key)  {
        int thisHash=Math.floorMod(key.hashCode(), buckets.length);
        Iterator <Node>it=buckets[thisHash].iterator();
        while(it.hasNext()){
            Node temp=it.next();
            while(temp.key.equals(key)){
                it.remove();
                keySet.remove(key);
                this.N--;
                return temp.value;
            }
        }
        return null;
    }

    @Override
    public V remove(K key, V value) {
        int thisHash=Math.floorMod(key.hashCode(), buckets.length);
        Iterator <Node>it=buckets[thisHash].iterator();
        while(it.hasNext()){
            Node temp=it.next();
            while(temp.key.equals(key)&&temp.value.equals(value)){
                it.remove();
                keySet.remove(key);
                this.N--;
                return temp.value;
            }
        }
        return null;
    }


}
