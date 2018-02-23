package com.cdr.gen;

import com.cdr.gen.util.IOUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.apache.commons.text.StrSubstitutor;
import org.apache.log4j.Logger;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * This class only loads the configuration file and handles the saving of the population
 * to a file.
 * @author Maycon Viana Bordin <mayconbordin@gmail.com>
 */
public final class CDRGen {
    private static final Logger LOG = Logger.getLogger(CDRGen.class);
    public static final String DEFAULT_CONFIG_FILE = "/config.json";
    private static final String DEFAULT_ANUMBERS = "555057,555058";
    private Map<String, Object> config;
    
    public CDRGen() {
        IOUtils.loadConfig(DEFAULT_CONFIG_FILE);
    }

    public CDRGen(String configFile) {
        config = IOUtils.loadConfig(configFile);
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

        try {
            FileWriter fw = new FileWriter(outputFile);
            String newLine = System.getProperty("line.separator");
            
            for (Person p : customers) {
                for (Call c : p.getCalls()) {

                  Map<String, String> values = new HashMap<String, String>();
                  values.put("id", String.valueOf(c.getId()));
                  values.put("sourcePhoneNumber", p.getPhoneNumber());
                  values.put("line", String.valueOf(c.getLine()));
                  values.put("destPhoneNumber", c.getDestPhoneNumber());
                  values.put("originalUnits", "0");
                  values.put("calculatedUnits", "0");

                  values.put("startDate", c.getTime().getStart().toString(dateFormatter));
                  values.put("endDate", c.getTime().getEnd().toString(dateFormatter));
                  values.put("startTime", c.getTime().getStart().toString(timeFormatter));
                  values.put("endTime", c.getTime().getEnd().toString(timeFormatter));
                  values.put("startDateTime", c.getTime().getStart().toString(dateTimeFormatter));
                  values.put("endDateTime", c.getTime().getEnd().toString(dateTimeFormatter));

                  values.put("bytes", String.valueOf(c.getBytes()));
                  values.put("kiloBytes", String.valueOf(c.getKiloBytes()));

                  values.put("type", c.getType());
                  values.put("cost", String.valueOf(c.getCost()));

                  StrSubstitutor sub = new StrSubstitutor(values);

                  fw.append(sub.replace(config.get("cdrFilePattern"))).append(newLine);
                }
            }

            fw.close();
        } catch (IOException ex) {
            LOG.error("Error while writing the output file.", ex);
        } 
    }
    
    public static void main( String[] args ) {
        if (args.length == 0) {
            String exec = new java.io.File(CDRGen.class.getProtectionDomain()
                    .getCodeSource().getLocation().getPath()).getName();
            System.out.println("Usage: java -jar " + exec + " <output_file> [<config_file>]");
            System.exit(1);
        }

        String configFile = (args.length > 1) ? args[1] : DEFAULT_CONFIG_FILE;

        String aNumbersCommaSeparatedList = (args.length > 2) ? args[2] : DEFAULT_ANUMBERS;
        List<String> aNumbers = Arrays.asList(aNumbersCommaSeparatedList.split(","));

        CDRGen generator = new CDRGen(configFile);
        
        Population population = new Population(generator.getConfig(), aNumbers);
        population.create();

        SimCurrentLocation simCurrentLocation = new SimCurrentLocation();
        
        List<Person> customers = population.getPopulation();
        generator.saveToFile(args[0], customers);
        LOG.info("Done.");
    }
}
