package latex;

import java.io.File;

public abstract class LatexObject {
    public static final File outputDir = new File("D:\\MA\\git\\Verification\\paper");

    public abstract void print(LatexStream stream);
}
