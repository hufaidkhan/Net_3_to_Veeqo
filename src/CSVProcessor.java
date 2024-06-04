import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.text.ParseException;

public class CSVProcessor {
    public static void main(String[] args) {
        // Input folder is where the program is located
        String inputFolder = System.getProperty("user.dir");
        // Output folder is the default Windows download folder
        String outputFolder = System.getProperty("user.home") + "\\Downloads";

        // Default columns to extract (0-based index)
        int[] defaultColumns = {2, 11, 12, 13, 14, 15, 16, 17, 18, 39, 49, 57, 19, 40, 41, 48}; // Example: Extracting columns 11, 13, 14, 15, 16, 17, 18, 39, 49, 57

        // Define column headers for extracted columns
        String[] columnHeaders = {
                "number", "shipping_address_first_name", "shipping_address_last_name", "shipping_address_company" , "shipping_address_address1", "shipping_address_address2",
                "shipping_address_city", "shipping_address_state", "shipping_address_zip", "shipping_address_country", "sku",
                "customer_phone", "quantity", "price_per_unit", " due date"
        };

        try {
            processCSV(inputFolder, outputFolder, defaultColumns, columnHeaders);
            System.out.println("CSV processing complete.");
        } catch (IOException e) {
            System.err.println("Error processing CSV file: " + e.getMessage());
        }
    }

    private static void processCSV(String inputFolder, String outputFolder, int[] requiredColumns, String[] columnHeaders)
            throws IOException {
        // Create the output folder if it doesn't exist
        File outputDir = new File(outputFolder);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // Find the input files in the input folder
        File inputDir = new File(inputFolder);
        File[] inputFiles = inputDir.listFiles((dir, name) -> name.endsWith(".csv"));

        if (inputFiles != null && inputFiles.length > 0) {
            for (File inputFile : inputFiles) {
                String inputFileName = inputFile.getName();
                String outputFileName = "Net32ToVeeqo " + new SimpleDateFormat("ddMMMyyyy").format(new Date());
                String outputFilePath = outputFolder + "/" + outputFileName + ".csv";

                try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                     BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
                    boolean firstLine = true; // Flag to skip the first line
                    while (reader.ready()) {
                        String line = reader.readLine();
                        if (firstLine) {
                            firstLine = false;
                            // Write column headers for extracted columns
                            writer.write(String.join(",", columnHeaders));
                            writer.newLine();
                            continue; // Skip the first line
                        }
                        String[] parts = line.split(",");
                        List<String> extractedColumns = new ArrayList<>();
                        for (int columnIndex : requiredColumns) {
                            if (columnIndex < parts.length) {
                                if (columnIndex == 14 && parts[columnIndex].equals("null")) {
                                    extractedColumns.add(""); // Replace "null" with empty string
                                } else if (columnIndex == 11) {
                                    String[] nameParts = parts[columnIndex].split(" ");
                                    if (nameParts.length > 1) {
                                        extractedColumns.add(nameParts[0]); // First part of the name
                                        extractedColumns.add(String.join(" ", Arrays.copyOfRange(nameParts, 1, nameParts.length))); // Last part of the name
                                    } else {
                                        extractedColumns.add(parts[columnIndex]); // First name (same as full name)
                                        extractedColumns.add(""); // Empty column for last name
                                    }
                                } else if (columnIndex == 48) {
                                    // Extract date in MM/dd/yyyy format
                                    String[] dateTimeParts = parts[columnIndex].split(" ");
                                    String date = extractDate(dateTimeParts[0]);
                                    extractedColumns.add(date); // Add extracted date
                                } else {
                                    extractedColumns.add(parts[columnIndex]); // Add column as is
                                }
                            }
                        }
                        writer.write(String.join(",", extractedColumns));
                        writer.newLine();
                    }
                }

                // Delete the input file after processing
                if (!inputFile.delete()) {
                    System.out.println("Failed to delete input file: " + inputFile.getAbsolutePath());
                }
            }
        } else {
            System.out.println("No input files found in the input folder: " + inputFolder);
        }
    }

    private static String extractDate(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");

            // Debug output
            System.out.println("Original date: " + dateStr);

            String formattedDate = outputFormat.format(inputFormat.parse(dateStr));

            // Debug output
            System.out.println("Formatted date: " + formattedDate);

            return formattedDate;
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }
}
