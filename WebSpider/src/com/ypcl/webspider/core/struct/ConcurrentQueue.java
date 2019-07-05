package com.ypcl.webspider.core.struct;

import java.util.HashSet;
import java.util.LinkedList;

public class ConcurrentQueue {
	private static HashSet<String> m_appear = new HashSet<String>();
    private static LinkedList<String> m_queue = new LinkedList<String>();
    private final static ConcurrentQueue instance = new ConcurrentQueue();
    
    private ConcurrentQueue() {
    	
    }
    
    public static ConcurrentQueue getInstance() {
    	return instance;
    }
    
    public synchronized boolean add(String url) {
        if(!m_appear.contains(url)) {
            m_appear.add(url);
            m_queue.addLast(url);
            return true;
        }
        return false;
    }
    
    public synchronized void clear() {
    	m_appear.clear();
    	m_queue.clear();   
    }
    
    public synchronized String fetch() {
        if(!m_queue.isEmpty() ) {
            return m_queue.poll();
        }
        
        return null;
    }
    
    public int size() {
        return m_queue.size();
    }
}
