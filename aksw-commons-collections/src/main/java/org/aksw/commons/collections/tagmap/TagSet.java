package org.aksw.commons.collections.tagmap;

import java.util.Collection;

public interface TagSet<T>
    extends Collection<T>
{
    TagSet<T> getSuperItemsOf(T proto, boolean strict);
    TagSet<T> getSubItemsOf(T proto, boolean strict);
}



