package org.aksw.commons.rx.cache.range;

import java.util.HashMap;
import java.util.Map;

import org.aksw.commons.util.array.ArrayOps;
import org.aksw.commons.util.array.Buffer;

import com.google.common.math.LongMath;

/**
 * A buffer backed by a mapping of page indices (longs) to arrays. All arrays are the same size.
 * The purpose of this class is to provide a tradeoff between for frequent consecutive small inserts at sparse locations.
 * The map accounts for sparseness, whereas consecutive small inserts will usually update the same array.
 * 
 * @author raven
 *
 * @param <T>
 */
public class PagedBuffer<A>
	implements Buffer<A>
{
	protected ArrayOps<A> arrayOps;
	protected int pageSize;
	protected long capacity;
	
	protected Map<Long, A> map;

	public PagedBuffer(ArrayOps<A> arrayOps, int pageSize) {
		this(arrayOps, pageSize, Long.MAX_VALUE, new HashMap<>());
	}

	public PagedBuffer(ArrayOps<A> arrayOps, int pageSize, long capacity, Map<Long, A> map) {
		super();
		this.arrayOps = arrayOps;
		this.pageSize = pageSize;
		this.capacity = capacity;
		this.map = map;
	}
	
	@Override
	public ArrayOps getArrayOps() {
		return arrayOps;
	}
	
	public Map<Long, A> getPageMap() {
		return map;
	}
	
	public static <A> PagedBuffer<A> create(ArrayOps<A> arrayOps, int pageSize) {
		return new PagedBuffer<>(arrayOps, pageSize);
	}
	

	@Override
	public void write(long offsetInBuffer, A arrayWithItemsOfTypeT, int arrOffset, int arrLength) {
		if (LongMath.checkedAdd(offsetInBuffer, arrLength) > capacity) {
			throw new IndexOutOfBoundsException("Put outside of capacity");
		}
		
		long startPage = PageUtils.getPageIndexForOffset(offsetInBuffer, pageSize);
		
		int remainingInSrc = arrLength;
		int indexInPage = PageUtils.getIndexInPage(offsetInBuffer, pageSize);
		
		int srcIndex = arrOffset;
		for (long i = startPage; remainingInSrc > 0; ++i) {
			A buffer = map.computeIfAbsent(i, k -> arrayOps.create(pageSize));
			int remainingInPage = Math.min(pageSize - indexInPage, remainingInSrc);
			arrayOps.copyRaw(arrayWithItemsOfTypeT, srcIndex, buffer, indexInPage, remainingInPage);
			srcIndex += remainingInPage;
			remainingInSrc -= remainingInPage;
			indexInPage = 0;
		}
	}
	
	@Override
	public int readInto(Object tgt, int tgtOffset, long srcOffset, int length) {
		if (LongMath.checkedAdd(srcOffset, length) > capacity) {
			throw new IndexOutOfBoundsException("Put outside of capacity");
		}

		long startPage = PageUtils.getPageIndexForOffset(srcOffset, pageSize);
		
		int remainingInSrc = length;
		int indexInPage = PageUtils.getIndexInPage(srcOffset, pageSize);
		
		int tgtIndex = tgtOffset;
		for (long i = startPage; remainingInSrc > 0; ++i) {
			
			A buffer = map.get(i);
			int remainingInPage = Math.min(pageSize - indexInPage, remainingInSrc);

			if (buffer == null) {
				arrayOps.fillRaw(tgt, tgtIndex, remainingInPage, null);
			} else {
				arrayOps.copyRaw(buffer, indexInPage, tgt, tgtIndex, remainingInPage);
			}
			
			tgtIndex += remainingInPage;
			remainingInSrc -= remainingInPage;
			indexInPage = 0;
		}
		
		return length;
	}
	
	@Override
	public long getCapacity() {
		return capacity;
	}

	
	@Override
	public Object get(long index) {
		long page = PageUtils.getPageIndexForOffset(index, pageSize);
		int indexInPage = PageUtils.getIndexInPage(index, pageSize);

		A buffer = map.get(page);
		Object result = buffer == null ? arrayOps.getDefaultValue() : arrayOps.get(buffer, indexInPage);
		return result;
	}

	@Override
	public void put(long index, Object item) {
		long page = PageUtils.getPageIndexForOffset(index, pageSize);
		int indexInPage = PageUtils.getIndexInPage(index, pageSize);

		A buffer = map.computeIfAbsent(page, x -> arrayOps.create(pageSize));
		arrayOps.set(buffer, indexInPage, item);
	}
}
