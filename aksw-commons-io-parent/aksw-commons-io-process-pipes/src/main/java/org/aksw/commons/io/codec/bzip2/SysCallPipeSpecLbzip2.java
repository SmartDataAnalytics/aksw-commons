package org.aksw.commons.io.codec.bzip2;

import org.aksw.commons.io.process.pipe.SysCallPipeSpec;

public class SysCallPipeSpecLbzip2
	implements SysCallPipeSpec
{
	@Override
	public String[] cmdStreamToStream() { return new String[]{"/usr/bin/lbzip2", "-c" };}
}
