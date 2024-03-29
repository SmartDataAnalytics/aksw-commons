package org.aksw.commons.util.entity;

import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 * A basic java implementation of the EntityInfo interface
 *
 * @author raven
 *
 */
public class EntityInfoImpl
    implements EntityInfo
{
    protected List<String> contentEncodings;
    protected String contentType;
    protected String charset;
    protected Set<String> languageTags;
    protected Set<String> conformsTo;

    protected Long byteSize;
    protected Long uncompressedByteSize;

    public EntityInfoImpl(String contentType) {
        this(Collections.emptyList(), contentType);
    }

    public EntityInfoImpl(List<String> contentEncodings, String contentType) {
        this(contentEncodings, contentType, null);
    }

    public EntityInfoImpl(List<String> contentEncodings, String contentType, String charset) {
        this(contentEncodings, contentType, charset, Collections.emptySet());
    }

    public EntityInfoImpl(List<String> contentEncodings, String contentType, String charset, Set<String> languageTags) {
        this(contentEncodings, contentType, charset, languageTags, Collections.emptySet());
    }

    public EntityInfoImpl(List<String> contentEncodings, String contentType,
            String charset, Set<String> languageTags, Set<String> conformsTo) {
        super();
        this.contentEncodings = contentEncodings;
        this.contentType = contentType;
        this.charset = charset;
        this.languageTags = languageTags;
        this.conformsTo = conformsTo;
    }

    @Override
    public List<String> getContentEncodings() {
        return contentEncodings;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getCharset() {
        return charset;
    }

    @Override
    public Set<String> getLanguageTags() {
        return languageTags;
    }

    @Override
    public Set<String> getConformsTo() {
        return conformsTo;
    }

    public Long getByteSize() {
        return byteSize;
    }

    public void setByteSize(Long byteSize) {
        this.byteSize = byteSize;
    }

    public Long getUncompressedByteSize() {
        return uncompressedByteSize;
    }

    public void setUncompressedByteSize(Long uncompressedByteSize) {
        this.uncompressedByteSize = uncompressedByteSize;
    }

    @Override
    public String toString() {
        return "EntityInfoImpl [contentEncodings=" + contentEncodings + ", contentType=" + contentType + ", charset="
                + charset + ", languageTags=" + languageTags + ", conformsTo=" + conformsTo + ", byteSize=" + byteSize
                + ", uncompressedByteSize=" + uncompressedByteSize + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((byteSize == null) ? 0 : byteSize.hashCode());
        result = prime * result + ((charset == null) ? 0 : charset.hashCode());
        result = prime * result + ((conformsTo == null) ? 0 : conformsTo.hashCode());
        result = prime * result + ((contentEncodings == null) ? 0 : contentEncodings.hashCode());
        result = prime * result + ((contentType == null) ? 0 : contentType.hashCode());
        result = prime * result + ((languageTags == null) ? 0 : languageTags.hashCode());
        result = prime * result + ((uncompressedByteSize == null) ? 0 : uncompressedByteSize.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EntityInfoImpl other = (EntityInfoImpl) obj;
        if (byteSize == null) {
            if (other.byteSize != null)
                return false;
        } else if (!byteSize.equals(other.byteSize))
            return false;
        if (charset == null) {
            if (other.charset != null)
                return false;
        } else if (!charset.equals(other.charset))
            return false;
        if (conformsTo == null) {
            if (other.conformsTo != null)
                return false;
        } else if (!conformsTo.equals(other.conformsTo))
            return false;
        if (contentEncodings == null) {
            if (other.contentEncodings != null)
                return false;
        } else if (!contentEncodings.equals(other.contentEncodings))
            return false;
        if (contentType == null) {
            if (other.contentType != null)
                return false;
        } else if (!contentType.equals(other.contentType))
            return false;
        if (languageTags == null) {
            if (other.languageTags != null)
                return false;
        } else if (!languageTags.equals(other.languageTags))
            return false;
        if (uncompressedByteSize == null) {
            if (other.uncompressedByteSize != null)
                return false;
        } else if (!uncompressedByteSize.equals(other.uncompressedByteSize))
            return false;
        return true;
    }
}
