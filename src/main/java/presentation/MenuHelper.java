package presentation;

import java.util.ArrayList;
import java.util.List;
import util.ConsoleHelper;

public class MenuHelper {
	private MenuHelper() {
	}

	public static void printHeader(String title) {
		System.out.println();
		String line = repeat("=", Math.max(46, title.length() + 8));
		System.out.println(line);
		System.out.println(centerText(title, line.length()));
		System.out.println(line);
	}

	public static void printOptions(String... options) {
		printOptionCards(options);
	}

	public static int askChoice(int min, int max) {
		return ConsoleHelper.promptIntInRange("Chon: ", min, max);
	}

	public static void printOptionCards(String... options) {
		int max = 0;
		for (String option : options) {
			max = Math.max(max, option.length());
		}
		String border = "+" + repeat("-", max + 2) + "+";
		for (String option : options) {
			System.out.println(border);
			System.out.println("| " + padRight(option, max) + " |");
		}
		System.out.println(border);
	}

	public static void printTable(String[] headers, List<String[]> rows) {
		if (headers == null || headers.length == 0) {
			System.out.println("Khong co cot de hien thi.");
			return;
		}

		int[] widths = new int[headers.length];
		for (int i = 0; i < headers.length; i++) {
			widths[i] = headers[i].length();
		}

		List<String[]> safeRows = new ArrayList<>();
		for (String[] row : rows) {
			if (row == null) {
				continue;
			}
			String[] normalized = new String[headers.length];
			for (int i = 0; i < headers.length; i++) {
				normalized[i] = i < row.length && row[i] != null ? row[i] : "";
				widths[i] = Math.max(widths[i], normalized[i].length());
			}
			safeRows.add(normalized);
		}

		String separator = buildSeparator(widths);
		System.out.println(separator);
		System.out.println(buildRow(headers, widths));
		System.out.println(separator);
		for (String[] row : safeRows) {
			System.out.println(buildRow(row, widths));
		}
		System.out.println(separator);
	}

	private static String buildSeparator(int[] widths) {
		StringBuilder sb = new StringBuilder();
		sb.append("+");
		for (int width : widths) {
			sb.append(repeat("-", width + 2)).append("+");
		}
		return sb.toString();
	}

	private static String buildRow(String[] values, int[] widths) {
		StringBuilder sb = new StringBuilder();
		sb.append("|");
		for (int i = 0; i < widths.length; i++) {
			String value = i < values.length && values[i] != null ? values[i] : "";
			sb.append(" ").append(padRight(value, widths[i])).append(" |");
		}
		return sb.toString();
	}

	private static String centerText(String text, int width) {
		if (text.length() >= width) {
			return text;
		}
		int totalPadding = width - text.length();
		int left = totalPadding / 2;
		int right = totalPadding - left;
		return repeat(" ", left) + text + repeat(" ", right);
	}

	private static String padRight(String value, int width) {
		if (value.length() >= width) {
			return value;
		}
		return value + repeat(" ", width - value.length());
	}

	private static String repeat(String value, int times) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < times; i++) {
			sb.append(value);
		}
		return sb.toString();
	}
}
