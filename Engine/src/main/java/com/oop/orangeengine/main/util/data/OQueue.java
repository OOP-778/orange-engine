package com.oop.orangeengine.main.util.data;


import com.oop.orangeengine.main.util.data.list.OLinkedList;

import java.io.Serializable;
import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;

public class OQueue<T> extends ConcurrentLinkedQueue<T> implements Serializable {}
