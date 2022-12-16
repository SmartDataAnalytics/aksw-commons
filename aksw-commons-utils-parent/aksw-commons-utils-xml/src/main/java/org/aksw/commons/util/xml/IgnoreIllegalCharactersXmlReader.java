package org.aksw.commons.util.xml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.xml.sax.InputSource;

//Source: http://stackoverflow.com/questions/2897085/filtering-illegal-xml-characters-in-java
public class IgnoreIllegalCharactersXmlReader extends Reader {

	private final BufferedReader underlyingReader;
	private StringBuilder buffer = new StringBuilder(4096);
	private boolean eos = false;

	public IgnoreIllegalCharactersXmlReader(final InputStream is)
			throws UnsupportedEncodingException {
		underlyingReader = new BufferedReader(
				new InputStreamReader(is, "UTF-8"));
	}

	private void fillBuffer() throws IOException {
		final String line = underlyingReader.readLine();
		if (line == null) {
			eos = true;
			return;
		}
		buffer.append(line);
		buffer.append('\n');
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		if (buffer.length() == 0 && eos) {
			return -1;
		}
		int satisfied = 0;
		int currentOffset = off;
		while (false == eos && buffer.length() < len) {
			fillBuffer();
		}
		while (satisfied < len && buffer.length() > 0) {
			char ch = buffer.charAt(0);
			final char nextCh = buffer.length() > 1 ? buffer.charAt(1) : '\0';
			if (ch == '&' && nextCh == '#') {
				final StringBuilder entity = new StringBuilder();
				// Since we're reading lines it's safe to assume entity is all
				// on one line so next char will/could be the hex char
				int index = 0;
				char entityCh = '\0';
				// Read whole entity
				while (entityCh != ';') {
					entityCh = buffer.charAt(index++);
					entity.append(entityCh);
				}
				// if it's bad get rid of it and clean it from the buffer and
				// point to next valid char
				if (entity.toString().equals("&#2;")) {
					buffer.delete(0, entity.length());
					continue;
				}
			}

				if(true) {throw new RuntimeException("We need XMLChar.isValid here"); }
			if(true) {
				//if (XMLChar.isValid(ch)) {
				satisfied++;
				cbuf[currentOffset++] = ch;
			}
			buffer.deleteCharAt(0);
		}
		return satisfied;
	}

	@Override
	public void close() throws IOException {
		underlyingReader.close();
	}

	public static <T> T unmarshallXml(Class<T> clazz, InputStream in)
			throws JAXBException, UnsupportedEncodingException {
		
		String className = clazz.getPackage().getName();
		JAXBContext context = JAXBContext.newInstance(className);	
		Unmarshaller unmarshaller = context.createUnmarshaller();
	
		Reader reader = new IgnoreIllegalCharactersXmlReader(in);
	
		//Reader reader = new InputStreamReader(inputStream,"UTF-8");
		 
		InputSource is = new InputSource(reader);
		is.setEncoding("UTF-8");
		
		Object result = unmarshaller.unmarshal(is); //file);
		return (T)result;
	}
	

	/*
	public static void main(final String[] args) {
        final File file = new File(
    <XML>);
        final File outFile = new File(file.getParentFile(), file.getName()
    .replace(".xml", ".cleaned.xml"));
        Reader r = null;
        Writer w = null;
        try {
            r = new IgnoreIllegalCharactersXmlReader(new FileInputStream(file));
            w = new OutputStreamWriter(new FileOutputStream(outFile),"UTF-8");
            IOUtils.copyLarge(r, w);
            w.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(r);
            IOUtils.closeQuietly(w);
        }
    }
    */
}
