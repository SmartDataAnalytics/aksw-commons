package org.aksw.commons.rx.cache.range;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.aksw.commons.path.core.PathOpsStr;
import org.aksw.commons.rx.cache.range.AdvancedRangeCacheNew.Builder;
import org.aksw.commons.rx.lookup.ListPaginator;
import org.aksw.commons.rx.lookup.ListPaginatorFromList;
import org.aksw.commons.store.object.key.api.ObjectStore;
import org.aksw.commons.store.object.key.impl.ObjectStoreImpl;
import org.aksw.commons.store.object.path.impl.ObjectSerializerKryo;
import org.aksw.commons.util.array.ArrayOps;
import org.junit.Test;

import com.esotericsoftware.kryo.pool.KryoPool;
import com.google.common.collect.Range;
import com.google.common.primitives.Ints;

import junit.framework.Assert;

public class TestListPaginatorCache {

	public static ListPaginator<String> createListWithRandomItems(Random rand) {
		List<String> list = new ArrayList<>();
		
		int numItems = rand.nextInt(100000);
		for (int i = 0; i < numItems; ++i) {
			StringBuilder b = new StringBuilder();
			b.append("#" + i + ": ");
			int numChars = rand.nextInt(7) + 1;
			for (int j = 0; j < numChars; ++j) {
				char c = (char)('a' + rand.nextInt(26));
				b.append(c);
			}
			String str = b.toString();
			list.add(str);
		}
		
		return new ListPaginatorFromList<>(list);
	}
	
	@Test
	public void test() {
		Random random = new Random(0);
		
		ListPaginator<String> backend = createListWithRandomItems(random);
		
        KryoPool kryoPool = SmartRangeCacheImpl.createKyroPool(null);
		ObjectStore objectStore = ObjectStoreImpl.create(Path.of("/tmp/aksw-commons-cache-test"), ObjectSerializerKryo.create(kryoPool));		
		org.aksw.commons.path.core.Path<String> objectStoreBasePath = PathOpsStr.newRelativePath("object-store");
		SliceBufferNew<String[]> slice = SliceBufferNew.create(ArrayOps.createFor(String.class), objectStore, objectStoreBasePath, 100, Duration.ofMillis(500));
		
		Builder<String[]> builder = AdvancedRangeCacheNew.Builder.<String[]>create()
			// .setDataSource(SequentialReaderSourceRx.create(ArrayOps.createFor(String.class), backend))
			.setRequestLimit(10000)
			.setSlice(slice)
			.setTerminationDelay(Duration.ofSeconds(10));		
		
		//  SmartRangeCacheNew<String> cache
		ListPaginator<String> frontend = SmartRangeCacheNew.create(backend, builder);
		
		
		for (int i = 0; i < 10; ++i) {
			int size = Ints.saturatedCast(backend.fetchCount(null, null).blockingGet().lowerEndpoint());
			
			int start = random.nextInt(size);
			int end = start + random.nextInt(size - start);
			
			Range<Long> requestRange = Range.closedOpen((long)start, (long)end);

			List<String> expected = backend.fetchList(requestRange);
			List<String> actual = frontend.fetchList(requestRange);
			
			Assert.assertEquals(expected, actual);
		}
		
		createListWithRandomItems(random).apply(Range.atLeast(0l)).forEach(System.out::println);	
		
		
		
	}
}
