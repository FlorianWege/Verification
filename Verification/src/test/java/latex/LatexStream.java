package latex;

import util.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class LatexStream {
    private final PrintStream _stream;

    private int _nestDepth = 0;

    public void println(@Nullable String s) {
        _stream.println(StringUtil.repeat("\t", _nestDepth) + s);
    }

    public void close() {
        _stream.close();
    }

    public void begin() {
        _nestDepth++;
    }

    public void end() {
        _nestDepth--;
    }

    public LatexStream(@Nonnull PrintStream stream) {
        _stream = stream;
    }

    public LatexStream(@Nonnull File file) throws FileNotFoundException {
        assert(file.getParentFile().mkdirs() || file.getParentFile().exists());

        _stream = new PrintStream(new FileOutputStream(file));
    }
}