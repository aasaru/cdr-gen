package com.cdr.gen;

import com.cdr.gen.util.IOUtils;
import org.joda.time.*;
import org.joda.time.format.*;
import org.json.simple.*;

import java.util.*;

public class SimCurrentLocation {

  private Map<LocalDateTime, CellInfo> locationChangesMap = new TreeMap<>();

  private SimCurrentLocation() {}

  public static SimCurrentLocation createFromFile(String locationsFile, String cellMappingsFile) {
	  CellMappings cellMappings = CellMappings.createFromFile(cellMappingsFile);
	  SimCurrentLocation simCurrentLocation = new SimCurrentLocation();
    JSONArray jsonArray = IOUtils.loadJsonArray(locationsFile);

	  for (Object aJsonArray : jsonArray) {
		  JSONObject obj = (JSONObject) aJsonArray;

		  String timeOfDay = (String) obj.get("time");
		  Long dayOfMonth = (Long) obj.get("day");
		  String cellId = (String) obj.get("cellId");

		  LocalDateTime dateOfEvent = getCellChangeDateTime(timeOfDay, dayOfMonth);

		  CellInfo cellInfo = cellMappings.getCellInfo(cellId);

		  simCurrentLocation.locationChangesMap.put(dateOfEvent, cellInfo);
	  }
		return simCurrentLocation;
  }

	private static LocalDateTime getCellChangeDateTime(String time, Long day) {
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-d HH:mm:ss");

		DateTimeFormatter yyyyMM = DateTimeFormat.forPattern("yyyy-MM");
		String currentYearDashMonth = yyyyMM.print(now);
		String previousMonthYearMohth = yyyyMM.print(now.minusMonths(1));

  	String currentMonthDateTimeFormat = String.format("%s-%s %s", currentYearDashMonth, day, time);
		String previousMonthDateTimeFormat = String.format("%s-%s %s", previousMonthYearMohth, day, time);

		LocalDateTime dateInThisMonth = LocalDateTime.parse(currentMonthDateTimeFormat, dateTimeFormatter);
		LocalDateTime dateInPreviousMonth = LocalDateTime.parse(previousMonthDateTimeFormat, dateTimeFormatter);

		return (dateInThisMonth.isBefore(now)) ? dateInThisMonth : dateInPreviousMonth;
	}

	public CellInfo getCallCellInfo(DateTime callDate) {

    LocalDateTime lastChangeBeforeCall = locationChangesMap.keySet().stream()
      .reduce((first, second) -> second.isBefore(callDate.toLocalDateTime()) ? second : first)
      .orElse(null);

    return locationChangesMap.get(lastChangeBeforeCall);
  }

}
