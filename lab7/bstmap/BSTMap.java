package bstmap;

import java.util.Iterator;
import java.util.Set;

public class BSTMap <K extends Comparable<K>,V>implements Map61B<K,V>{

    Node root;
    public BSTMap(K key,V val){
        root=new Node(key,val);
    }
    public BSTMap(){

    }
    @Override
    public void clear() {
        this.root=null;
    }

    @Override
    public boolean containsKey(K key) {
        Node t=root;
        if(t==null){
            return false;
        }
        while(!t.key.equals(key)){
            int cmp= key.compareTo(t.key);
            if(cmp>0){
                t=t.right;
            } else {
                t=t.left;
            }
            if(t==null){
                return false;
            }
        }
        return true;
    }

    @Override
    public V get(K key) {
        Node t=root;
        if(t==null){
            return null;
        }
        while(!t.key.equals(key)){
            int cmp= key.compareTo(t.key);
            if(cmp>0){
                t=t.right;
            } else {
                t=t.left;
            }
            if(t==null){
                return null;
            }
        }
        return t.val;
    }

    @Override
    public int size() {
        if(root==null){
            return 0;
        }
        return root.size;
    }
    public int size(Node x){
        if(x==null){
            return 0;
        }
        return x.size;
    }

    @Override
    public void put(K key, V value) {
        root=put(root,key,value);
    }
    public Node put(Node x,K key, V value) {
        if (x==null){
            x=new Node(key,value);
            return x;
        }
        int cmp=key.compareTo(x.key);
        if(cmp>0){
            x.right=put(x.right,key,value);
            x.size=size(x.right)+size(x.left)+1;
        }else if (cmp<0){
            x.left=put(x.left,key,value);
            x.size=size(x.right)+size(x.left)+1;
        }
        return x;
    }

    @Override
    public Set<K> keySet()throws UnsupportedOperationException {
        return null;
    }

    @Override
    public V remove(K key) throws UnsupportedOperationException{
        return null;
    }

    @Override
    public V remove(K key, V value) throws UnsupportedOperationException{
        return null;
    }

    @Override
    public Iterator<K> iterator() throws UnsupportedOperationException{
        return null;
    }
    public void PRINT(Node x){
        if(x==null){
            return;
        }
        PRINT(x.left);
        System.out.println(x.val);
        PRINT(x.right);
    }
    private class Node{
        K key;
        V val;
        int size;
        Node left;
        Node right;
        public Node (K key,V val){
            this.key=key;
            this.val=val;
            this.size=1;
            left=null;
            right=null;
        }

    }
}

