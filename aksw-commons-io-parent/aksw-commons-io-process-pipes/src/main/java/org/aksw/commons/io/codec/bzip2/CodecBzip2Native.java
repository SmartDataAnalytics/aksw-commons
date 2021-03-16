package org.aksw.commons.io.codec.bzip2;

import java.io.InputStream;
import java.io.OutputStream;

import org.aksw.commons.io.codec.registry.api.Codec;
import org.aksw.commons.io.process.pipe.PipeTransform;
import org.aksw.commons.lambda.throwing.ThrowingFunction;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

public class CodecBzip2Native
	implements Codec
{
	@Override
	public PipeTransform encoder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PipeTransform decoder() {
		ThrowingFunction<InputStream, InputStream> ixform = in -> new BZip2CompressorInputStream(in, true);
		ThrowingFunction<OutputStream, OutputStream> oxform = out -> new BZip2CompressorOutputStream(out);
		
		
		// return new BZip2CompressorInputStream()
		// BZip2CompressorOutputStream
		
		// TODO Auto-generated method stub
		return null;
	}

}
