package delfos.io.csv.dataset.rating;

import delfos.common.Global;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Jorge Castro Gallardo
 *
 * @version 12-mar-2014
 */
public class CSVReader {

    private final File ratingsCSV;
    private final String stringSeparator;
    private final String fieldSeparator;
    private final String rowSeparator = "\n";

    private final BufferedReader reader;

    private String rawRecord;

    private final Map<String, Integer> headerIndex;
    private boolean headersAlreadyReaded;

    public CSVReader(File csvFile, String stringSeparator, String fieldSeparator) throws FileNotFoundException {
        this.ratingsCSV = csvFile;
        this.stringSeparator = stringSeparator;
        this.fieldSeparator = fieldSeparator;

        reader = new BufferedReader(new FileReader(csvFile));
        headerIndex = new TreeMap<String, Integer>();
    }

    public CSVReader(File csvFile) throws FileNotFoundException {
        this(csvFile, "\"", ",");
    }

    public String[] splitRecord(String rawRecord) {
        if (!rawRecord.contains(stringSeparator)) {
            return rawRecord.split(fieldSeparator);
        } else {

            Matcher ma = Pattern.compile("(?:\\s*(?:\\\"([^\\\"]*)\\\"|([^,]+))\\s*,?)+?").matcher(rawRecord);
            while (ma.find()) {
                if (ma.group(1) == null) {
                    Global.showln(ma.group(2));
                } else {
                    Global.showln(ma.group(1));
                }
            }
        }

        return null;

    }

    public String[] readHeaders() throws IOException {
        if (headersAlreadyReaded) {
            throw new IllegalStateException("Cannot read headers twice.");
        }
        rawRecord = reader.readLine();

        String[] headers = splitRecord(rawRecord);

        for (int i = 0; i < headers.length; i++) {
            headerIndex.put(headers[i], i);
        }

        headersAlreadyReaded = true;
        return headers;
    }

    public boolean readRecord() throws IOException {
        rawRecord = reader.readLine();

        if (rawRecord != null) {
            String[] records = splitRecord(rawRecord);
            return true;
        } else {
            return false;
        }
    }

    public String get(int index) {
        return splitRecord(rawRecord)[index];
    }

    public String get(String headerName) {
        if (headerIndex.containsKey(headerName)) {
            return get(headerIndex.get(headerName));
        } else {
            throw new IndexOutOfBoundsException("Header '" + headerName + "' not exists");
        }
    }

    public String getRawRecord() {
        return rawRecord;
    }

    public void close() throws IOException {
        rawRecord = null;
        reader.close();
    }

}
