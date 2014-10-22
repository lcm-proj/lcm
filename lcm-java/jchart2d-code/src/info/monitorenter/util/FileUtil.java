/*
 * FileUtil.java, helpers for disk I/O.
 * Copyright (C) 2001 - 2011 Achim Westermann.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *  
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * If you modify or optimize the code in a useful way please let me know.
 * Achim.Westermann@gmx.de
 */
package info.monitorenter.util;

import info.monitorenter.util.collections.Entry;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

/**
 * Utility class for file operations.
 * <p>
 * 
 * @author Achim Westermann
 * 
 * @version 1.1
 */
public final class FileUtil extends Object {

  /**
   * Cuts all path information of the String representation of the given URL.
   * <p>
   * 
   * <pre>
   * 
   *  &quot;file//c:/work/programming/anyfile.jar&quot; --&gt; &quot;anyfile.jar&quot;
   *  &quot;http://jamwg.de&quot;                       --&gt; &quot;&quot; // No file part.
   *  &quot;ftp://files.com/directory2/&quot;           --&gt; &quot;&quot; // File part of URL denotes a directory.
   * 
   * </pre>
   * 
   * Assuming, that '/' is the current file separator character.
   * <p>
   * 
   * @param path
   *          the absolute file path you want the mere file name of.
   * 
   * @return the <code>{@link java.util.Map.Entry}</code> consisting of path
   *         information and file name.
   */
  public static Map.Entry<String, String> cutDirectoryInformation(final java.net.URL path) {
    Map.Entry<String, String> ret = null;
    String pre;
    String suf;
    String parse;
    final StringBuffer tmp = new StringBuffer();
    parse = path.toExternalForm();
    if (parse.endsWith("/")) {
      pre = parse;
      suf = "";
    } else {
      final StringTokenizer tokenizer = new StringTokenizer(path.getFile(), "/");
      tmp.append(path.getProtocol());
      tmp.append(":");
      tmp.append(path.getHost());
      pre = "";
      while (tokenizer.hasMoreElements()) {
        tmp.append(pre);
        pre = tokenizer.nextToken();
        tmp.append("/");
      }
      suf = pre;
      pre = tmp.toString();
    }
    ret = new Entry<String, String>(pre, suf);
    return ret;
  }

  /**
   * Cuts the path information of the String that is interpreted as a filename
   * into the directory part and the file part. The current operating system's
   * path separator is used to cut all path information from the String.
   * <p>
   * 
   * <pre>
   * 
   *  &quot;c:/work/programming/anyfile.jar&quot; --&gt; Map.Entry(&quot;c:/work/programming/&quot;,&quot;anyfile.jar&quot;);
   *  &quot;anyfile.jar&quot;                     --&gt; Map.Entry(new File(&quot;.&quot;).getAbsolutePath(),&quot;anyfile.jar&quot;);
   *  &quot;c:/directory1/directory2/&quot;       --&gt; Map.Entry(&quot;c:/directory1/directory2/&quot;,&quot;&quot;);
   *  &quot;c:/directory1/directory2&quot;        --&gt; Map.Entry(&quot;c:/directory1/directory2/&quot;,&quot;&quot;); // directory2 is a dir!
   *  &quot;c:/directory1/file2&quot;             --&gt; Map.Entry(&quot;c:/directory1/&quot;,&quot;file2&quot;);       // file2 is a file!
   *  &quot;c:/&quot;                             --&gt; Map.Entry(&quot;c:/&quot;,&quot;&quot;);
   * 
   * </pre>
   * 
   * Assuming, that '/' is the current file separator character.
   * <p>
   * 
   * <b>If your string is retrieved from an <tt>URL</tt> instance, use
   * <tt>cutDirectoryInformation(URL path)</tt> instead, because URL's do not
   * depend on the operating systems file separator! </b>
   * <p>
   * 
   * @param path
   *          the absolute file path you want the mere file name of.
   * 
   * @return the <code>{@link java.util.Map.Entry}</code> consisting of path
   *         information and file name.
   */
  public static Map.Entry<String, String> cutDirectoryInformation(final String path) {
    final StringBuffer dir = new StringBuffer();
    String file = "";
    final String fileseparator = System.getProperty("file.separator");
    final StringTokenizer tokenizer = new StringTokenizer(path, fileseparator, true);
    final int size = tokenizer.countTokens();
    switch (size) {
      case 0:
        throw new IllegalArgumentException("This cannot be a filename: \"" + path + "\"");
      default:
        String token;
        while (tokenizer.hasMoreElements()) {

          token = tokenizer.nextToken();

          if (tokenizer.hasMoreTokens()) {
            dir.append(token);
          } else {
            if (new File(path).isFile()) {
              file = token;
            } else {
              dir.append(token);
            }
          }
        }
    }

    return new Entry<String, String>(dir.toString(), file);
  }

  /**
   * Cuts a String into the part before the last dot and after the last dot. If
   * only one dot is contained on the first position, it will completely be used
   * as prefix part.
   * <p>
   * 
   * <pre>
   * Map.Entry entry = FileUtil.cutExtension(&quot;A.Very.Strange.Name.txt&quot;);
   * String prefix = (String) entry.getKey(); // prefix is &quot;A.Very.Strange.Name&quot;.
   * String suffix = (String) entry.getValue(); // suffix is &quot;txt&quot;;
   * 
   * entry = FileUtil.cutExtension(&quot;.profile&quot;);
   * String prefix = (String) entry.getKey(); // prefix is &quot;.profile&quot;.
   * String suffix = (String) entry.getValue(); // suffix is &quot;&quot;;
   * 
   * entry = FileUtil.cutExtension(&quot;bash&quot;);
   * String prefix = (String) entry.getKey(); // prefix is &quot;bash&quot;.
   * String suffix = (String) entry.getValue(); // suffix is &quot;&quot;;
   * 
   * </pre>
   * 
   * <p>
   * 
   * 
   * @param filename
   *          A String that is interpreted to be a file name: The last dot ('.')
   *          is interpreted to be the extension delimiter.
   * 
   * @return A <tt> java.util.Map.Entry</tt> instance containing a String for
   *         the filename at the key side and a String for the extension at the
   *         value side.
   */
  public static java.util.Map.Entry<String, String> cutExtension(final String filename) {
    String prefix;
    String suffix = null;
    final StringTokenizer tokenizer = new StringTokenizer(filename, ".");
    int tokenCount = tokenizer.countTokens();
    if (tokenCount > 1) {
      final StringBuffer prefCollect = new StringBuffer();
      while (tokenCount > 1) {
        tokenCount--;
        prefCollect.append(tokenizer.nextToken());
        if (tokenCount > 1) {
          prefCollect.append(".");
        }
      }
      prefix = prefCollect.toString();
      suffix = tokenizer.nextToken();
    } else {
      prefix = filename;
      suffix = "";
    }
    return new Entry<String, String>(prefix, suffix);
  }

  /**
   * Finds a filename based on the given name. If a file with the given name
   * does not exist, <tt>name</tt> will be returned.
   * <p>
   * 
   * Else:
   * 
   * <pre>
   *  &quot;myFile.out&quot;     --&gt; &quot;myFile_0.out&quot;
   *  &quot;myFile_0.out&quot;   --&gt; &quot;myFile_1.out&quot;
   *  &quot;myFile_1.out&quot;   --&gt; &quot;myFile_2.out&quot;
   *  ....
   * </pre>
   * 
   * <p>
   * 
   * The potential extension is preserved, but a number is appended to the
   * prefix name.
   * <p>
   * 
   * @param name
   *          A desired file name.
   * 
   * @return A String that sticks to the naming convention of the given String
   *         but is unique in the directory scope of argument <tt>name</tt>.
   */
  public static String getDefaultFileName(final String name) {
    String result;
    File f = new File(name);
    if (!f.exists()) {
      result = f.getAbsolutePath();
    } else {
      final java.util.Map.Entry<String, String> cut = FileUtil.cutExtension(name);
      final String prefix = cut.getKey();
      final String suffix = cut.getValue();
      int num = 0;
      while (f.exists()) {
        f = new File(prefix + '_' + num + '.' + suffix);
        num++;
      }
      result = f.getAbsolutePath();
    }
    return result;
  }

  /**
   * Creates a new file by the contract of
   * <code>{@link File#createTempFile(String, String, File)} </code>.
   * <p>
   * 
   * The resulting file will have the full path and file name without the
   * extension concatenated with the given suffix (if preserveExtension is true
   * also the suffix of the original file will be added to the end).
   * <p>
   * 
   * 
   * @param source
   *          the source file to take the name of.
   * 
   * @param suffix
   *          the suffix to append.
   * 
   * @param preserveExtension
   *          if true the suffix of the source file will be appended to the
   *          result file.
   * 
   * @return A file derived from the original file name.
   * 
   * @throws IOException
   *           if something goes wrong.
   **/
  public static File deriveFile(final File source, final String suffix,
      final boolean preserveExtension) throws IOException {
    File result = null;
    Map.Entry<String, String> fileNextension = FileUtil.cutExtension(source.getName());
    Map.Entry<String, String> pathNfile = FileUtil
        .cutDirectoryInformation(source.getAbsolutePath());

    String ending = suffix;
    if (preserveExtension) {
      ending += fileNextension.getValue();
    }
    result = File.createTempFile(fileNextension.getKey(), ending, new File(pathNfile.getKey()));
    return result;
  }

  /**
   * Tests whether the given file only contains ASCII characters if interpreted
   * by reading bytes (16 bit).
   * <p>
   * This does not mean that the file is really an ASCII text file. It just
   * might be viewed with an editor showing only valid ASCII characters.
   * <p>
   * 
   * @param f
   *          the file to test.
   * 
   * @return true if all bytes in the file are in the ASCII range.
   * 
   * @throws IOException
   *           on a bad day.
   */
  public static boolean isAllASCII(final File f) throws IOException {
    return FileUtil.isAllASCII(new FileInputStream(f));
  }

  /**
   * Tests wether the given input stream only contains ASCII characters if
   * interpreted by reading bytes (16 bit).
   * <p>
   * This does not mean that the underlying content is really an ASCII text
   * file. It just might be viewed with an editor showing only valid ASCII
   * characters.
   * <p>
   * 
   * @param in
   *          the stream to test.
   * 
   * @return true if all bytes in the given input stream are in the ASCII range.
   * 
   * @throws IOException
   *           on a bad day.
   */
  public static boolean isAllASCII(final InputStream in) throws IOException {
    boolean ret = true;
    int read = -1;
    do {
      read = in.read();
      if (read > 0x7F) {
        ret = false;
        break;
      }

    } while (read != -1);
    return ret;
  }

  /**
   * Tests, whether the content of the given file is identical at character
   * level, when it is opened with both different Charsets.
   * <p>
   * This is most often the case, if the given file only contains ASCII codes
   * but may also occur, when both codepages cover common ranges and the
   * document only contains values m_out of those ranges (like the EUC-CN
   * charset contains all mappings from BIG5).
   * <p>
   * 
   * @param document
   *          the file to test.
   * 
   * @param a
   *          the first character set to interpret the document in.
   * 
   * @param b
   *          the 2nd character set to interpret the document in.
   * 
   * @throws IOException
   *           if something goes wrong.
   * 
   * @return true if both files have all equal contents if they are interpreted
   *         as character data in both given encodings (they may differ at
   *         binary level if both charsets are different).
   */
  public static boolean isEqual(final File document, final Charset a, final Charset b)
      throws IOException {
    boolean ret = true;
    FileInputStream aIn = null;
    FileInputStream bIn = null;
    InputStreamReader aReader = null;
    InputStreamReader bReader = null;
    try {
      aIn = new FileInputStream(document);
      bIn = new FileInputStream(document);
      aReader = new InputStreamReader(aIn, a);
      bReader = new InputStreamReader(bIn, b);
      int readA = -1;
      int readB = -1;
      do {
        readA = aReader.read();
        readB = bReader.read();
        if (readA != readB) {
          // also the case, if one is at the end earlier...
          ret = false;
          break;
        }
      } while ((readA != -1) && (readB != -1));
      return ret;
    } finally {
      if (aReader != null) {
        aReader.close();
      }
      if (bReader != null) {
        bReader.close();
      }
    }
  }

  /**
   * Invokes {@link #readRAM(File)}, but decorates the result with a
   * {@link java.io.ByteArrayInputStream}.
   * <p>
   * This means: The complete content of the given File has been loaded before
   * using the returned InputStream. There are no IO-delays afterwards but
   * OutOfMemoryErrors may occur.
   * <p>
   * 
   * @param f
   *          the file to cache.
   * 
   * @return an input stream backed by the file read into memory.
   * 
   * @throws IOException
   *           if something goes wrong.
   */
  public static InputStream readCache(final File f) throws IOException {
    return new ByteArrayInputStream(FileUtil.readRAM(f));
  }

  /**
   * Reads the content of the given File into an array.
   * <p>
   * This method currently does not check for maximum length and might cause a
   * java.lang.OutOfMemoryError. It is only intended for
   * performance-measurements of data-based algorithms that want to exclude
   * I/O-usage.
   * <p>
   * 
   * @param f
   *          the file to read.
   * 
   * @throws IOException
   *           if something goes wrong.
   * 
   * @return the contents of the given file.
   * 
   */
  public static byte[] readRAM(final File f) throws IOException {
    final int total = (int) f.length();
    final byte[] ret = new byte[total];
    final InputStream in = new FileInputStream(f);
    try {
      int offset = 0;
      int read = 0;
      do {
        read = in.read(ret, offset, total - read);
        if (read > 0) {
          offset += read;
        }
      } while ((read != -1) && (offset != total));
      return ret;
    } finally {
      in.close();
    }
  }

  /**
   * Removes the duplicate line breaks in the given file.
   * <p>
   * 
   * Be careful with big files: In order to avoid having to write a tmpfile
   * (cannot read and directly write to the same file) a StringBuffer is used
   * for manipulation. Big files will cost all RAM and terminate VM hard.
   * <p>
   * 
   * @param f
   *          the file to remove duplicate line breaks in.
   */
  public static void removeDuplicateLineBreaks(final File f) {
    final String sep = StringUtil.getNewLine();
    if (!f.exists()) {
      System.err.println("FileUtil.removeDuplicateLineBreak(File f): " + f.getAbsolutePath()
          + " does not exist!");
    } else {
      if (f.isDirectory()) {
        System.err.println("FileUtil.removeDuplicateLineBreak(File f): " + f.getAbsolutePath()
            + " is a directory!");
      } else {
        // real file
        FileInputStream inStream = null;
        BufferedInputStream in = null;
        FileWriter out = null;
        try {
          inStream = new FileInputStream(f);
          in = new BufferedInputStream(inStream, 1024);
          StringBuffer result = new StringBuffer();
          int tmpread;
          while ((tmpread = in.read()) != -1) {
            result.append((char) tmpread);
          }
          String tmpstring;
          final StringTokenizer toke = new StringTokenizer(result.toString(), sep, true);
          result = new StringBuffer();
          int breaks = 0;
          while (toke.hasMoreTokens()) {
            tmpstring = toke.nextToken().trim();
            if (tmpstring.equals("") && (breaks > 0)) {
              breaks++;
              // if(breaks<=2)result.append(sep);
              continue;
            }
            if (tmpstring.equals("")) {
              tmpstring = sep;
              breaks++;
            } else {
              breaks = 0;
            }
            result.append(tmpstring);
          }
          // delete original file and write it new from tmpfile.
          f.delete();
          f.createNewFile();
          out = new FileWriter(f);
          out.write(result.toString());
        } catch (final FileNotFoundException e) {
          // does never happen.
        } catch (final IOException g) {
          g.printStackTrace(System.err);
        } finally {
          if (in != null) {
            try {
              in.close();
            } catch (final IOException e) {
              e.printStackTrace();
            }
          }
          if (out != null) {
            try {
              out.flush();
              out.close();
            } catch (final IOException e) {
              e.printStackTrace();
            }
          }
        }
      }
    }
  }

  /** Needed for localization. */
  private static final ResourceBundle m_bundle = ResourceBundle.getBundle("messages");

  /**
   * Utility class constructor.
   * <p>
   */
  private FileUtil() {
    // nop
  }

  /**
   * Returns the formatted file size to Bytes, KB, MB or GB depending on the
   * given value.
   * <p>
   * 
   * @param filesize
   *          in bytes
   * 
   * @param locale
   *          the locale to translate the result to (e.g. in France they us
   * 
   * @return the formatted filesize to Bytes, KB, MB or GB depending on the
   *         given value.
   */
  public static String formatFilesize(final long filesize, final Locale locale) {

    String result;
    final long filesizeNormal = Math.abs(filesize);

    if (Math.abs(filesize) < 1024) {
      result = MessageFormat.format(m_bundle.getString("GUI_FILEUTIL_FILESIZE_BYTES_1"),
          new Object[] {new Long(filesizeNormal) });
    } else if (filesizeNormal < 1048576) {
      // 1048576 = 1024.0 * 1024.0
      result = MessageFormat.format(m_bundle.getString("GUI_FILEUTIL_FILESIZE_KBYTES_1"),
          new Object[] {new Double(filesizeNormal / 1024.0) });
    } else if (filesizeNormal < 1073741824) {
      // 1024.0^3 = 1073741824
      result = MessageFormat.format(m_bundle.getString("GUI_FILEUTIL_FILESIZE_MBYTES_1"),
          new Object[] {new Double(filesize / 1048576.0) });
    } else {
      result = MessageFormat.format(m_bundle.getString("GUI_FILEUTIL_FILESIZE_GBYTES_1"),
          new Object[] {new Double(filesizeNormal / 1073741824.0) });
    }
    return result;
  }
}
