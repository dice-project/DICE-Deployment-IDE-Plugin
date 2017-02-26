package org.dice.deployments.ui.launchers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;

public class TarGz {

  private static InputStream getInputStream(Path path)
      throws FileNotFoundException {
    FileInputStream fin = new FileInputStream(path.toFile());
    return new BufferedInputStream(fin);
  }

  private class Bundler extends SimpleFileVisitor<Path> {

    private Path toplevel;

    public Bundler(TarArchiveOutputStream output, Path dir) {
      toplevel = dir;
    }

    private String getEntryName(Path path) {
      return prefix.resolve(toplevel.relativize(path)).toString();
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir,
        BasicFileAttributes attrs) throws IOException {
      writeEntry(dir, getEntryName(dir), false);
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
        throws IOException {
      writeEntry(file, getEntryName(file), true);
      return FileVisitResult.CONTINUE;
    }

  }

  private TarArchiveOutputStream output;
  private Path prefix;

  public TarGz(String outputFile, String prefix) throws IOException {
    FileOutputStream fileOut = new FileOutputStream(outputFile);
    BufferedOutputStream buffOut = new BufferedOutputStream(fileOut);
    GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(buffOut);
    output = new TarArchiveOutputStream(gzOut);

    this.prefix = Paths.get(prefix);
  }

  public void close() throws IOException {
    output.close();
  }

  public void writeDir(Path dir) throws IOException {
    Bundler visitor = new Bundler(output, dir);
    Files.walkFileTree(dir, visitor);
  }

  public void writeFile(Path file, String name) throws IOException {
    Path entryPath = name != null ? Paths.get(name) : file.getFileName();
    String entryName = prefix.resolve(entryPath).toString();
    writeEntry(file, entryName, true);
  }

  private void writeEntry(Path resource, String entryName, boolean hasContent)
      throws IOException, FileNotFoundException {
    TarArchiveEntry entry = new TarArchiveEntry(resource.toFile(), entryName);
    output.putArchiveEntry(entry);
    if (hasContent) {
      IOUtils.copy(getInputStream(resource), output);
    }
    output.closeArchiveEntry();
  }

}
