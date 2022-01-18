import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CompareTwoJsonFiles {
  public void test() {
    String logFolder = "/Users/xiaodong/projects/github/apiCapture/_log";
    String baseFile = "develop-post-base";

    try {
      File dir = new File(logFolder);
      File[] list = dir.listFiles(File::isFile);
      long last = Long.MIN_VALUE;
      File lastFile = null;
      for (File file: list) {
        if (file.lastModified() > last) {
          lastFile = file;
          last = file.lastModified();
        }
      }

      impl(String.format("%s/%s", logFolder, baseFile), lastFile.getPath());
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  private boolean impl(String fileBase, String fileLast) {
    List<String> listBase = readFile(fileBase);
    List<String> listLast = readFile(fileLast);

    int lineNumber = 0;
    for (String line: listBase) {
      lineNumber++;
      boolean found = false;
      for (int i = 0; i < listLast.size(); i++) {
        if (compareTwoLines(line, listLast.get(i))) {
          // found match, remove it
          listLast.remove(i);
          found = true;
          System.out.println(String.format("line #%d found in latest at line #%d", lineNumber, i+1));
          break;
        }
      }
      if (!found) {
        System.out.println(String.format("line #%d of base not found in latest file", lineNumber));
        return false;
      }
    }

    if (listLast.size() > 0) {
      System.out.println("oops, the latest file have more entries!");
      return false;
    }

    System.out.println("==== lucky you, they are the same ====");
    System.out.println(fileBase);
    System.out.println(fileLast);
    return true;
  }

  private boolean compareTwoLines(String line1, String line2) {
    return line1.equals(line2);
  }

  private List<String> readFile(String filename) {
    List<String> list = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
      String line;
      while ((line = br.readLine()) != null) {
        list.add(line);
      }
    } catch (Exception e) {
      System.out.println(e);
    }
    return list;
  }

  public static void main(String[] args) {
    CompareTwoJsonFiles c = new CompareTwoJsonFiles();
    c.test();
  }
}
