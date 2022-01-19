import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.google.gson.*;
import org.json.JSONObject;

public class CompareTwoJsonFiles {
  private Gson gson = new Gson();

  public void test() {
    String logFolder = "/Users/xiaodong/projects/github/apiCapture/_log";
    String baseFile = "develop-post-patch-base";

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

    Set<Integer> matched = new HashSet<>();
    int lineNumber = 0;
    for (String line: listBase) {
      lineNumber++;
      boolean found = false;
      for (int i = 0; i < listLast.size(); i++) {
        if (matched.contains(i)) {
          continue;
        } else if (compareTwoLines(line, listLast.get(i))) {
          found = true;
          System.out.println(String.format("line #%d found in latest at line #%d", lineNumber, i+1));
          matched.add(i);
          break;
        }
      }
      if (!found) {
        System.out.println(String.format("line #%d of base not found in latest file", lineNumber));
        System.out.println(line);
        System.out.println(fileBase);
        System.out.println(fileLast);
        return false;
      }
    }

    if (listLast.size() != listBase.size()) {
      System.out.println("oops, they are not the same size!");
      System.out.println(fileBase);
      System.out.println(fileLast);
      return false;
    }

    System.out.println("==== lucky you, they are the same ====");
    System.out.println(fileBase);
    System.out.println(fileLast);
    return true;
  }

  private JsonObject convertToJsonObjectFromStringJson(String data) {
    JsonElement je = gson.fromJson(data, JsonElement.class);
    return je.getAsJsonObject();
  }

  private JsonObject convertToJson(String data) {
    JsonObject jo = convertToJsonObjectFromStringJson(data);
    jo.add("request", convertToJsonObjectFromStringJson(jo.get("request").getAsString()));
    jo.add("response", convertToJsonObjectFromStringJson(jo.get("response").getAsString()));
    return jo;
  }

  private boolean compareTwoLines(String line1, String line2) {
    JsonElement o1 = convertToJson(line1);
    JsonElement o2 = convertToJson(line2);
    boolean b = o1.equals(o2);
    return b;
  }

  private boolean compareTwoLines_save2(String line1, String line2) {
    JsonParser p = new JsonParser();
    JsonElement o1 = p.parse(line1);
    JsonElement o2 = p.parse(line2);
    boolean b = o1.equals(o2);
    return b;
  }

  private boolean compareTwoLines_save(String line1, String line2) {
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
