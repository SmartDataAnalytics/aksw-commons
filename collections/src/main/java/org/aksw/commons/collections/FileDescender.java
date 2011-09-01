package org.aksw.commons.collections;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 8/12/11
 *         Time: 5:17 PM
 */
public class FileDescender
	implements Descender<File>
{
	private FileFilter filter;

	public FileDescender() {
		this.filter = new FileFilter() {
			public boolean accept(File pathname) {
				return true;
			}};
	}

	public FileDescender(FileFilter filter) {
		this.filter = filter;
	}

	@SuppressWarnings("unchecked")
	public Collection<File> getDescendCollection(File item) {
        if(!item.isDirectory()) {
            return new ArrayList<File>();
        }

		File[] files = filter != null ? item.listFiles(filter) : item.listFiles();
		Collection<File> tmp = files == null ? Collections.<File>emptyList() : Arrays.asList(files);

        //System.out.println(tmp);
        return new ArrayList<File>(tmp);
	}
}