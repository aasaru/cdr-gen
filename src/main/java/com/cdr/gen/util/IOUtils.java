package com.cdr.gen.util;

import com.cdr.gen.CDRGen;
import com.google.common.io.Files;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class IOUtils {
  private static final Logger LOG = Logger.getLogger(IOUtils.class);


  public static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
    
    public static String convertStreamToString(InputStream is, String charsetName) {
        Scanner s = new Scanner(is, charsetName).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

  public static Map<String, Object> loadConfig(String file) {
      try {
          JSONParser parser = new JSONParser();
          String configStr;

          if (JavaUtils.isJar() && file.equals(CDRGen.DEFAULT_CONFIG_FILE)) {
              InputStream is = CDRGen.class.getResourceAsStream(file);
              configStr = convertStreamToString(is);
          } else {
              if (file.equals(CDRGen.DEFAULT_CONFIG_FILE))
                  file = "src/main/resources" + file;

              configStr = Files.toString(new File(file), Charset.defaultCharset());
          }

          return (JSONObject) parser.parse(configStr);
      } catch (IOException ex) {
          LOG.error("Unable to read config file '" + file + "'.", ex);
      } catch (ParseException ex) {
          LOG.error("Error parsing the config file '" + file + "'.", ex);
      }
    return null;
  }
}
