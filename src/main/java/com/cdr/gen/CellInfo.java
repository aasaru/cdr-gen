package com.cdr.gen;

public class CellInfo {

	String tadigCode;
	String mccCode;

	public String getTadigCode() {
		return tadigCode;
	}

	public String getMccCode() {
		return mccCode;
	}

	public static CellInfoBuilder aCellInfo() {
		return new CellInfoBuilder();
	}

	public static final class CellInfoBuilder {
		String tadigCode;
		String mccCode;

		private CellInfoBuilder() {
		}

		public CellInfoBuilder withTadigCode(String tadigCode) {
			this.tadigCode = tadigCode;
			return this;
		}

		public CellInfoBuilder withMccCode(String mccCode) {
			this.mccCode = mccCode;
			return this;
		}

		public CellInfo build() {
			CellInfo cellInfo = new CellInfo();
			cellInfo.mccCode = this.mccCode;
			cellInfo.tadigCode = this.tadigCode;
			return cellInfo;
		}
	}
}
