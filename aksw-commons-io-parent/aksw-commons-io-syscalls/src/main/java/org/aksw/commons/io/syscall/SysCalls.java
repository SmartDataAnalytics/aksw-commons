package org.aksw.commons.io.syscall;

import java.util.Arrays;
import java.util.List;

import org.aksw.commons.io.syscall.sort.SysSort;

import com.google.common.base.Strings;


public class SysCalls {

	public static List<String> createDefaultSortSysCall(SysSort cmdSort) {
		List<String> result = Arrays.asList("/usr/bin/sort", "-t", "\t");
		if(cmdSort.unique) {
			result.add("-u");
		}
	
		if(cmdSort.reverse) {
			result.add("-r");
		}

		if(cmdSort.randomSort) {
			result.add("-R");
		} else {
			result.add("-h");
		}
		
		if(!Strings.isNullOrEmpty(cmdSort.temporaryDirectory)) {
			result.add("-T");
			result.add(cmdSort.temporaryDirectory);
		}
		
		if(!Strings.isNullOrEmpty(cmdSort.bufferSize)) {
			result.add("-S");
			result.add(cmdSort.bufferSize);
		}
		
		if(cmdSort.parallel > 0) {
			result.add("--parallel");
			result.add("" + cmdSort.parallel);
		}
	
		return result;
	}

}
