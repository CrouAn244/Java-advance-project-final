package util;

import java.util.Scanner;
import java.util.function.Function;

public class ConsoleHelper {
	private static final Scanner SCANNER = new Scanner(System.in);
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_CYAN = "\u001B[36m";

	private ConsoleHelper() {
	}

	public static String prompt(String label) {
		System.out.print(label);
		return SCANNER.nextLine();
	}

	public static String promptPassword(String label) {
		java.io.Console console = System.console();
		if (console != null) {
			char[] raw = console.readPassword(label);
			return raw == null ? "" : new String(raw);
		}
		return prompt(label);
	}

	public static String promptNonBlank(String label) {
		return promptWithValidation(label, value -> {
			if (value == null || value.trim().isEmpty()) {
				throw new IllegalArgumentException("Khong duoc de trong.");
			}
			return value.trim();
		});
	}

	public static String promptWithValidation(String label, Function<String, String> validator) {
		while (true) {
			String value = prompt(label);
			try {
				return validator.apply(value);
			} catch (IllegalArgumentException e) {
				System.out.println("Canh bao: " + e.getMessage());
			}
		}
	}

	public static int promptIntInRange(String label, int min, int max) {
		while (true) {
			String raw = prompt(label);
			try {
				int value = Integer.parseInt(raw.trim());
				if (value >= min && value <= max) {
					return value;
				}
			} catch (NumberFormatException ignored) {
			}
			System.out.println("Vui long nhap so tu " + min + " den " + max + ".");
		}
	}

	public static int promptPositiveInt(String label) {
		while (true) {
			String raw = prompt(label);
			try {
				int value = Integer.parseInt(raw.trim());
				if (value > 0) {
					return value;
				}
			} catch (NumberFormatException ignored) {
			}
			System.out.println("Canh bao: Vui long nhap so nguyen duong.");
		}
	}

	public static int promptNonNegativeInt(String label) {
		while (true) {
			String raw = prompt(label);
			try {
				int value = Integer.parseInt(raw.trim());
				if (value >= 0) {
					return value;
				}
			} catch (NumberFormatException ignored) {
			}
			System.out.println("Canh bao: Vui long nhap so nguyen khong am.");
		}
	}

	public static void waitForEnter() {
		System.out.print("Nhan Enter de tiep tuc...");
		SCANNER.nextLine();
	}
}
