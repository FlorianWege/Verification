package util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javax.annotation.Nonnull;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class IOUtil {
	public static String getResourceAsString(@Nonnull String name) throws IOException, URISyntaxException {
		URI uri = IOUtil.class.getClassLoader().getResource(name).toURI();
		
		Path path = Paths.get(uri);
		
		byte[] bytes = Files.readAllBytes(path);
		
		return new String(bytes);
	}

	private static final Map<File, URL> _urlMap = new LinkedHashMap<>();

	public static @Nonnull Scene inflateFXML(@Nonnull File file, @Nonnull Object controller) throws IOException {
		if (!_urlMap.containsKey(file)) _urlMap.put(file, controller.getClass().getResource(file.toString()));

		URL url = _urlMap.get(file);
		
		if (url == null) throw new IOException(file.toString() + " not found");
		
		FXMLLoader loader = new FXMLLoader(url);
		
		loader.setController(controller);
		
		PrintStream errStream = System.err;
		
		try {
			System.setErr(new PrintStream(new OutputStream() {
				@Override
				public void write(int b) throws IOException {
				}
			}));
			
			loader.load();
		} finally {
			System.setErr(errStream);
		}
		
		Parent root = loader.getRoot();
		
		return new Scene(root);
	}

	public static @Nonnull List<File> getCodeFiles() {
		return Arrays.asList(
			new File("codes/alt.c"),
			new File("codes/div.c"),
			new File("codes/factorial.c"),
			new File("codes/factorial2.c"),
			new File("codes/euclid.c"),
			new File("codes/assign.c"),
			new File("codes/assignNested.c"),
			new File("codes/power.c"),
			new File("codes/swapF.c"),
			new File("codes/swap.c")
		);
	}

	public static class FastByteArrayOutputStream extends OutputStream {
		/**
		 * Buffer and size
		 */
		protected byte[] buf = null;
		protected int size = 0;

		/**
		 * Constructs a stream with buffer capacity size 5K
		 */
		public FastByteArrayOutputStream() {
			this(5 * 1024);
		}

		/**
		 * Constructs a stream with the given initial size
		 */
		public FastByteArrayOutputStream(int initSize) {
			this.size = 0;
			this.buf = new byte[initSize];
		}

		/**
		 * Ensures that we have a large enough buffer for the given size.
		 */
		private void verifyBufferSize(int sz) {
			if (sz > buf.length) {
				byte[] old = buf;
				buf = new byte[Math.max(sz, 2 * buf.length )];
				System.arraycopy(old, 0, buf, 0, old.length);
				old = null;
			}
		}

		public int getSize() {
			return size;
		}

		/**
		 * Returns the byte array containing the written data. Note that this
		 * array will almost always be larger than the amount of data actually
		 * written.
		 */
		public byte[] getByteArray() {
			return buf;
		}

		@Override
		public final void write(byte b[]) {
			verifyBufferSize(size + b.length);
			System.arraycopy(b, 0, buf, size, b.length);
			size += b.length;
		}

		@Override
		public final void write(byte b[], int off, int len) {
			verifyBufferSize(size + len);
			System.arraycopy(b, off, buf, size, len);
			size += len;
		}

		@Override
		public final void write(int b) {
			verifyBufferSize(size + 1);
			buf[size++] = (byte) b;
		}

		public void reset() {
			size = 0;
		}

		/**
		 * Returns a ByteArrayInputStream for reading back the written data
		 */
		public InputStream getInputStream() {
			return new FastByteArrayInputStream(buf, size);
		}

	}

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

		public FastByteArrayInputStream(byte[] buf, int count) {
			this.buf = buf;
			this.count = count;
		}

		@Override
		public final int available() {
			return count - pos;
		}

		@Override
		public final int read() {
			return (pos < count) ? (buf[pos++] & 0xff) : -1;
		}

		@Override
		public final int read(byte[] b, int off, int len) {
			if (pos >= count)
				return -1;

			if ((pos + len) > count)
				len = (count - pos);

			System.arraycopy(buf, pos, b, off, len);
			pos += len;
			return len;
		}

		@Override
		public final long skip(long n) {
			if ((pos + n) > count)
				n = count - pos;
			if (n < 0)
				return 0;
			pos += n;
			return n;
		}

	}

	public static @Nonnull Object deepCopy(@Nonnull Object orig) {
		Object obj = null;

		try {
			// Write the object out to a byte array
			FastByteArrayOutputStream fbos = new FastByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(fbos);
			out.writeObject(orig);
			out.flush();
			out.close();

			// Retrieve an input stream from the byte array and read
			// a copy of the object back in.
			ObjectInputStream in = new ObjectInputStream(fbos.getInputStream());
			obj = in.readObject();
		} catch(IOException | ClassNotFoundException e) {
			ErrorUtil.logE(e);
		}

		return obj;
	}

	public abstract static class Func<A1, R> implements Function<A1, R>, Serializable {

	}

	public abstract static class BiFunc<A1, A2, R> implements BiFunction<A1, A2, R>, Serializable {

	}
}