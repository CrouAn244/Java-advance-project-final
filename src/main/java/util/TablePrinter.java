package util;

import java.util.List;

public final class TablePrinter {
	private TablePrinter() {
	}

	public static void printTable(String[] headers, int[] widths, List<String[]> rows) {
		if (headers == null || widths == null || headers.length == 0 || headers.length != widths.length) {
			throw new IllegalArgumentException("Headers va widths khong hop le.");
		}

		String divider = buildDivider(widths);
		String rowFormat = buildRowFormat(widths);

		System.out.println(divider);
		System.out.printf(rowFormat, toFittedCells(headers, widths));
		System.out.println(divider);

		for (String[] row : rows) {
			if (row == null) {
				continue;
			}
			System.out.printf(rowFormat, toFittedCells(row, widths));
		}

		System.out.println(divider);
	}

	private static String buildDivider(int[] widths) {
		StringBuilder sb = new StringBuilder("+");
		for (int width : widths) {
			sb.append(repeat('-', width + 2)).append('+');
		}
		return sb.toString();
	}

	private static String buildRowFormat(int[] widths) {
		StringBuilder sb = new StringBuilder();
		for (int width : widths) {
			sb.append("| %-").append(width).append("s ");
		}
		sb.append("|%n");
		return sb.toString();
	}

	private static Object[] toFittedCells(String[] row, int[] widths) {
		Object[] fitted = new Object[widths.length];
		for (int i = 0; i < widths.length; i++) {
			String value = i < row.length ? row[i] : "";
			fitted[i] = fit(value, widths[i]);
		}
		return fitted;
	}

	private static String fit(String value, int width) {
		String safe = value == null ? "" : value;
		if (safe.length() <= width) {
			return safe;
		}
		if (width <= 3) {
			return safe.substring(0, width);
		}
		return safe.substring(0, width - 3) + "...";
	}

	private static String repeat(char c, int times) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < times; i++) {
			sb.append(c);
		}
		return sb.toString();
	}
}
