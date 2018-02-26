package com.cdr.gen;

import com.cdr.gen.util.IOUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.apache.commons.text.StrSubstitutor;
import org.apache.log4j.Logger;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.simple.*;

/**
 * This class only loads the configuration file and handles the saving of the population
 * to a file.
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public final class CDRGen {
  private static final Logger LOG = Logger.getLogger(CDRGen.class);

  private static List<String> ALLOWED_PROVIDERS = Arrays.asList("jt","kr");

  public static final String DEFAULT_CONFIG_FILE = "/config.json";
  private static final String DEFAULT_ANUMBERS = "555057,555058";
  private Map<String, Object> config;
  private JSONArray locations;

  public CDRGen() {
    IOUtils.loadConfig(DEFAULT_CONFIG_FILE);
  }

	public CDRGen(String provider) {
  	provider = provider.toLowerCase();
  	if (!ALLOWED_PROVIDERS.contains(provider)) {
  		throw new IllegalArgumentException(String.format("Provider %s not found in allowed providers list %s", provider, ALLOWED_PROVIDERS));
	  }

		config = IOUtils.loadMapFromJson(String.format("src/main/resources/config_%s.json", provider));
	}

  public Map<String, Object> getConfig() {
    return config;
  }

  public String getValueFromConfig(String key) {

    Object value = config.get(key);
    if (value == null) {
      throw new IllegalArgumentException("Not found key " + key + " from config file");
    }
    return String.valueOf(value);
  }

  public void saveToFile(String outputFile, List<Person> customers) {
    DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy");
    DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm:ss");
    DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(getValueFromConfig("cdrDateTimePattern"));

    SimCurrentLocation simCurrentLocation = SimCurrentLocation.createFromFile("src/main/resources/locations.json", "src/main/resources/cellMappings.json");

    try {
      FileWriter fw = new FileWriter(outputFile);
      String newLine = System.getProperty("line.separator");

      for (Person person : customers) {
        for (Call call : person.getCalls()) {

          CellInfo cellInfo = simCurrentLocation.getCallCellInfo(call.getTime().getStart());

          Map<String, String> values = new HashMap<String, String>();
          values.put("id", String.valueOf(call.getId()));
          values.put("sourcePhoneNumber", person.getPhoneNumber());
          values.put("line", String.valueOf(call.getLine()));
          values.put("destPhoneNumber", call.getDestPhoneNumber());
          values.put("originalUnits", "0");
          values.put("calculatedUnits", "0");

          values.put("tadig", cellInfo.getTadigCode());
          values.put("mcc", cellInfo.getMccCode());

          values.put("startDate", call.getTime().getStart().toString(dateFormatter));
          values.put("endDate", call.getTime().getEnd().toString(dateFormatter));
          values.put("startTime", call.getTime().getStart().toString(timeFormatter));
          values.put("endTime", call.getTime().getEnd().toString(timeFormatter));
          values.put("startDateTime", call.getTime().getStart().toString(dateTimeFormatter));
          values.put("endDateTime", call.getTime().getEnd().toString(dateTimeFormatter));

          values.put("bytes", String.valueOf(call.getBytes()));
          values.put("kiloBytes", String.valueOf(call.getKiloBytes()));

          values.put("type", call.getType());
          values.put("cost", String.valueOf(call.getCost()));

          StrSubstitutor sub = new StrSubstitutor(values);

          fw.append(sub.replace(config.get("cdrFilePattern"))).append(newLine);
        }
      }

      fw.close();
    }
    catch (IOException ex) {
      LOG.error("Error while writing the output file.", ex);
    }
  }

  public static void main(String[] args) {
    if (args.length < 5) {
      String exec = new java.io.File(CDRGen.class.getProtectionDomain()
        .getCodeSource().getLocation().getPath()).getName();
      System.out.println("Usage: java -jar " + exec + " <output_file> <provider> <imsi_list> <start_date> <end_date>");
      System.exit(1);
    }

    String provider = args[1];

    String aNumbersCommaSeparatedList = (args.length > 2) ? args[2] : DEFAULT_ANUMBERS;
    List<String> aNumbers = Arrays.asList(aNumbersCommaSeparatedList.split(","));

    String outputFileName = args[0];
    String startDate = args[3];
    String endDate = args[4];

    generateCDRs(provider, aNumbers, outputFileName, startDate, endDate);
    LOG.info("Done.");
  }

  private static void generateCDRs(String provider, List<String> aNumbers, String outputFileName, String startDate, String endDate) {
    CDRGen generator = new CDRGen(provider);
    generator.getConfig().put("startDate", startDate);
    generator.getConfig().put("endDate", endDate);

    Population population = new Population(generator.getConfig(), aNumbers);
    population.create();

    List<Person> customers = population.getPopulation();

    generator.saveToFile(outputFileName, customers);
  }
}
