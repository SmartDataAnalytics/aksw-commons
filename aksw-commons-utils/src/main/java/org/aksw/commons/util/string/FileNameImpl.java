package org.aksw.commons.util.string;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Iterables;

public class FileNameImpl
	implements FileName
{
	protected String baseName;
	protected String contentPart;
	protected List<String> encodingParts;
	
	public FileNameImpl(String baseName, String contentPart, List<String> encodingParts) {
		super();
		this.baseName = baseName;
		this.contentPart = contentPart;
		this.encodingParts = encodingParts;
	}

	public static FileName create(String baseName, String contentPart, List<String> encodingParts) {
		return new FileNameImpl(baseName, contentPart, encodingParts);
	}

	@Override
	public String getBaseName() {
		return baseName;
	}

	@Override
	public String getContentPart() {
		return contentPart;
	}

	@Override
	public List<String> getEncodingParts() {
		return encodingParts;
	}

	@Override
	public String getExtension(boolean precedingDotIfNotEmpty) {
		String result = FileNameUtils.construct(Iterables.concat(Collections.singletonList(contentPart), encodingParts));
		
		if (precedingDotIfNotEmpty && !result.isEmpty()) {
			result = "." + result;
		}
		
		return result;
	}
	
	@Override
	public String toString() {
		String result = FileNameUtils.construct(Iterables.concat(Arrays.asList(baseName, contentPart), encodingParts));
		return result;
	}
	
}
