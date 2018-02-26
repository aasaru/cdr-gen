package com.cdr.gen;

import com.cdr.gen.util.IOUtils;

import java.util.*;

public class CellMappings {

	private Map<String, CellInfo> mappingTable = new HashMap<>();

	public static CellMappings createFromFile(String filePath) {
		CellMappings cellMappings = new CellMappings();

		Map<String, Object> cellMap = IOUtils.loadMapFromJson(filePath);


		for (Map.Entry<String, Object> cellId : cellMap.entrySet()) {

			Map<String, String> cellAliases = (Map<String, String>) cellId.getValue();

			CellInfo cellInfo = CellInfo.aCellInfo()
				.withMccCode(cellAliases.get("MCC"))
				.withTadigCode(cellAliases.get("TADIG"))
				.build();

			cellMappings.mappingTable.put(cellId.getKey(), cellInfo);

		}
		return cellMappings;
	}

	public CellInfo getCellInfo(String cellId) {
		CellInfo cellInfo = mappingTable.get(cellId);

		if (cellInfo == null) {
			throw new IllegalArgumentException("No cellinfo for cellId " + cellId);
		}
		return cellInfo;
	}

}
