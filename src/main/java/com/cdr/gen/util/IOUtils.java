package com.cdr.gen.util;

import com.cdr.gen.CDRGen;
import com.google.common.io.Files;
import org.apache.log4j.Logger;
import org.json.simple.*;
import org.json.simple.parser.*;

import java.io.*;
import java.nio.charset.*;
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
			}
			else {
				if (file.equals(CDRGen.DEFAULT_CONFIG_FILE)) {
					file = "src/main/resources" + file;
				}

				configStr = Files.toString(new File(file), Charset.defaultCharset());
			}

			return (JSONObject) parser.parse(configStr);
		}
		catch (IOException ex) {
			LOG.error("Unable to read config file '" + file + "'.", ex);
		}
		catch (ParseException ex) {
			LOG.error("Error parsing the config file '" + file + "'.", ex);
		}
		return null;
	}

	public static Map<String, Object> loadMapFromJson(String file) {
		String fileContents = getFileContents(file);

		return parseJsonMap(fileContents);
	}

	public static JSONArray loadJsonArray(String file) {
		String fileContents = getFileContents(file);
		return parseJsonArray(fileContents);
	}

	private static JSONArray parseJsonArray(String fileContents) {
		JSONParser parser = new JSONParser();
		try {
			return (JSONArray) parser.parse(fileContents);
		}
		catch (ParseException e) {
			throw new IllegalArgumentException("Unable to parse file contents to JSON Array ", e);
		}
	}

	private static Map<String, Object> parseJsonMap(String fileContents) {
		JSONParser parser = new JSONParser();
		try {
			return (Map<String, Object>) parser.parse(fileContents);
		}
		catch (ParseException e) {
			throw new IllegalArgumentException("Unable to parse file contents to JSON map ", e);
		}
	}

	private static String getFileContents(String file) {
		String fileContents;
		try {
			fileContents = Files.toString(new File(file), Charset.defaultCharset());
		}
		catch (IOException e) {
			throw new IllegalArgumentException("Unable to open locations file " + file, e);
		}
		return fileContents;
	}


}
