package com.lzx.record;

public class IntArrayList {

	private int[] data = new int[100];
	private int size = 0;

	public void add(int val) {
		if (data.length == size) {
			grow();
			add(val);
		}
		data[size] = val;
		size++;
	}

	public int get(int index) {
		return data[index];
	}

	public int[] getData() {
		int [] arr = new int[size];
		if (size >= 0) System.arraycopy(data, 0, arr, 0, size);
		return arr;
	}

	public void clear() {
		data = new int[100];
		size = 0;
	}

	public int size() {
		return size;
	}

	private void grow() {
		int[] backup = data;
		data = new int[size*2];
		if (backup.length >= 0) System.arraycopy(backup, 0, data, 0, backup.length);
	}
}
