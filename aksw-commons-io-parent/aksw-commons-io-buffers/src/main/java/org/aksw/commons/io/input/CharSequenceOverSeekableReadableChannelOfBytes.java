package org.aksw.commons.io.input;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.aksw.commons.util.range.PageHelper;
import org.apache.commons.lang3.ArrayUtils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class CharSequenceOverSeekableReadableChannelOfBytes
    implements CharSequence, PageHelper
{
    protected SeekableReadableChannel<byte[]> seekable;
    protected int length;

    protected int pageSize = 32 * 1024; // 1024 * 1024;
    private static final int DFT_CACHE_SIZE_MAX = 5;

    protected LoadingCache<Long, byte[]> cache;
    protected int recentOffset = Integer.MAX_VALUE;
    protected byte[] recentBuffer = null;

    public CharSequenceOverSeekableReadableChannelOfBytes(SeekableReadableChannel<byte[]> seekable) {
        this(seekable, Integer.MAX_VALUE);
    }

    public CharSequenceOverSeekableReadableChannelOfBytes(SeekableReadableChannel<byte[]> seekable, int length) {
        this.seekable = seekable;
        this.length = length;
        this.cache = CacheBuilder.newBuilder().maximumSize(DFT_CACHE_SIZE_MAX).build(new CacheLoader<Long, byte[]>() {
            @Override
            public byte[] load(Long key) throws Exception {
                return loadPage(key);
            }
        });
    }

    @Override
    public long getPageSize() {
        return pageSize;
    }

    protected byte[] loadPage(long pageId) throws IOException {
        long offset = getPageOffsetForPageId(pageId);
        seekable.position(offset);
        byte[] result = new byte[pageSize];
        int actualSize = ReadableChannels.readFully(seekable, result, 0, pageSize);
        if (actualSize != pageSize) {
            // This case happens only on the last page (and when exceeding it)
            // The cost to create a trimmed copy of that last page should be fairly low
            result = ArrayUtils.subarray(result, 0, actualSize);
        }
        return result;
    }

    protected byte[] getBufferForPageId(long pageId) {
        byte[] result;
        try {
            result = cache.get(pageId);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public char charAt(int index) {
        char result;

        if (index >= length) {
            result = (char)-1;
        } else {
            // Set the recent buffer if needed
            if (!(recentBuffer != null && index >= recentOffset && index < recentOffset + pageSize)) {
                long pageId = getPageIdForOffset(index);
                recentOffset = (int)getPageOffsetForPageId(pageId);
                recentBuffer = getBufferForPageId(pageId);
            }

            int idx = index - recentOffset;
            result = idx < recentBuffer.length
                    ? (char)recentBuffer[idx]
                    : (char)-1;
        }
        return result;
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        // We cannot guarantee that newly opened channels will be closed and
        // it is hard to keep track of resource allocations, therefore unsupported
        throw new UnsupportedOperationException();
//        Seekable clone = seekable.clone();
//        return new CharSequenceFromSeekable(clone, start, end);
    }


//  char result;
//  try {
//      if (index >= length) {
//          result = (char)-1;
//      } else {
//          seekable.position(index);
//          int n = seekable.read(buffer, 0, 1);
//          // seekable.position(p);
//
//          if (n > 0) {
//              result = (char)buffer[0];
//          } else if (n < 0) {
//              result = (char)-1;
//          } else {
//              throw new IllegalStateException("Read 0 bytes");
//          }
//      }
//      return result;
//  } catch (IOException e) {
//      throw new RuntimeException(e);
//  }
}