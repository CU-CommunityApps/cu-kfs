/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.core.api.util.cache;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Container class to simplify getting both a deepCopied object and its size returned from a single call to deepCopy
 */
/* Cornell Customization: backport redis*/
public class CopiedObject<T extends Serializable> {

    private byte[] content;
    private int size;
    private int oldSize;

    public CopiedObject() {
        oldSize = -1;
    }

    public CopiedObject(T cacheableObject) {
        oldSize = -1;
        setContent(cacheableObject);
    }

    /**
     * @return current value of bytes.
     */
    public int getSize() {
        return size;
    }

    /**
     * @return current value of cacheableObject.
     */
    public T getContent() {
        T copy = null;
        if (content != null) {
            ObjectInputStream ois = null;
            try {
                FastByteArrayInputStream deserializer = new FastByteArrayInputStream(content, size);
                ois = new ObjectInputStream(deserializer);
                copy = (T) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new CacheException("unable to complete getContent()", e);
            } finally {
                try {
                    if (ois != null) {
                        ois.close();
                    }
                } catch (IOException e) {
                    // ignoring this IOException, since the streams are going to be abandoned now anyway
                }
            }
        }
        return copy;
    }

    public void setContent(T cacheableObject) {
        int copySize = 0;
        if (cacheableObject != null) {
            ObjectOutputStream oos = null;
            try {
                FastByteArrayOutputStream serializer = new FastByteArrayOutputStream();
                oos = new ObjectOutputStream(serializer);
                oos.writeObject(cacheableObject);

                if (content != null) {
                    oldSize = size;
                }
                size = serializer.getSize();
                content = serializer.getByteArray();
            } catch (IOException e) {
                throw new CacheException("unable to complete deepCopy from src '" + cacheableObject.toString() + "'",
                        e);
            } finally {
                try {
                    if (oos != null) {
                        oos.close();
                    }
                } catch (IOException e) {
                    // ignoring this IOException, since the streams are going to be abandoned now anyway
                }
            }
        }
    }

    /**
     * @return current value of oldSize.
     */
    public int getOldSize() {
        return oldSize;
    }

    /**
     * ByteArrayOutputStream implementation that doesn't synchronize methods and doesn't copy the data on toByteArray().
     */
    private static class FastByteArrayOutputStream extends OutputStream {
        /**
         * Buffer and size
         */
        protected byte[] buf = null;
        protected int size = 0;

        /**
         * Constructs a stream with buffer capacity size 5K
         */
        FastByteArrayOutputStream() {
            this(5 * 1024);
        }

        /**
         * Constructs a stream with the given initial size
         */
        FastByteArrayOutputStream(int initSize) {
            this.size = 0;
            this.buf = new byte[initSize];
        }

        /**
         * Ensures that we have a large enough buffer for the given size.
         */
        private void verifyBufferSize(int sz) {
            if (sz > buf.length) {
                byte[] old = buf;
                buf = new byte[Math.max(sz, 2 * buf.length)];
                System.arraycopy(old, 0, buf, 0, old.length);
                old = null;
            }
        }

        public int getSize() {
            return size;
        }

        /**
         * @return the byte array containing the written data. Note that this array will almost always be larger than
         *         the amount of data actually written.
         */
        public byte[] getByteArray() {
            return buf;
        }

        public final void write(byte b[]) {
            verifyBufferSize(size + b.length);
            System.arraycopy(b, 0, buf, size, b.length);
            size += b.length;
        }

        public final void write(byte b[], int off, int len) {
            verifyBufferSize(size + len);
            System.arraycopy(b, off, buf, size, len);
            size += len;
        }

        public final void write(int b) {
            verifyBufferSize(size + 1);
            buf[size++] = (byte) b;
        }

        public void reset() {
            size = 0;
        }

        /**
         * @return a ByteArrayInputStream for reading back the written data
         */
        public InputStream getInputStream() {
            return new FastByteArrayInputStream(buf, size);
        }
    }

    /**
     * ByteArrayInputStream implementation that does not synchronize methods.
     */
    private static class FastByteArrayInputStream extends InputStream {
        /**
         * Our byte buffer
         */
        protected byte[] buf = null;

        /**
         * Number of bytes that we can read from the buffer
         */
        protected int count = 0;

        /**
         * Number of bytes that have been read from the buffer
         */
        protected int pos = 0;

        FastByteArrayInputStream(byte[] buf, int count) {
            this.buf = buf;
            this.count = count;
        }

        public final int available() {
            return count - pos;
        }

        public final int read() {
            return pos < count ? buf[pos++] & 0xff : -1;
        }

        public final int read(byte[] b, int off, int len) {
            if (pos >= count) {
                return -1;
            }

            if (pos + len > count) {
                len = count - pos;
            }

            System.arraycopy(buf, pos, b, off, len);
            pos += len;
            return len;
        }

        public final long skip(long n) {
            if (pos + n > count) {
                n = count - pos;
            }
            if (n < 0) {
                return 0;
            }
            pos += n;
            return n;
        }
    }
}
