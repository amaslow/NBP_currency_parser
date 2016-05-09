package pl.parser.nbp;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class MainClass {

    public static void main(String[] args) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        int year = 0;
        int month = 0;
        int day = 0;
        List filenames = new ArrayList();
        List<Double> kupno = new ArrayList<Double>();
        List<Double> sprzedaz = new ArrayList<Double>();

        if (args.length == 0) {
            System.out.println("Prawidlowe uzycie jest: 'java pl.parser.nbp.MainClass <kod_waluty np.USD> <data_poczatkowa np.2013-01-28> <data_koncowa np.2013-01-30>'");
        } else {
            String waluta = args[0];
            String dataStart = args[1];
            String dataStop = args[2];

            try {
                Date startDate = format.parse(dataStart);
                Date stopDate = format.parse(dataStop);

                while (startDate.before(new Date(stopDate.getTime() + (1000 * 60 * 60 * 24)))) {
                    SimpleDateFormat dy = new SimpleDateFormat("yyyy");
                    year = Integer.parseInt(dy.format(startDate));
                    SimpleDateFormat dm = new SimpleDateFormat("MM");
                    month = Integer.parseInt(dm.format(startDate));
                    SimpleDateFormat dd = new SimpleDateFormat("dd");
                    day = Integer.parseInt(dd.format(startDate));
                    filenames.add(fileName(year, month, day));
                    startDate = new Date(startDate.getTime() + (1000 * 60 * 60 * 24));
                }

                for (int i = 0; i < filenames.size(); i++) {
                    if (filenames.get(i) != null) {
                        String kursKupna = getkurs(waluta, filenames.get(i).toString())[0];
                        kupno.add(Double.parseDouble(kursKupna.replaceAll(",", ".")));
                        String kursSprzedazy = getkurs(waluta, filenames.get(i).toString())[1];
                        sprzedaz.add(Double.parseDouble(kursSprzedazy.replaceAll(",", ".")));
                    }
                }

                DecimalFormat f = new DecimalFormat("0.0000");
                double sredniKursKupna = calculateAverage(kupno);
                System.out.println(f.format(sredniKursKupna));

                double sredniKursSprzedazy = calculateAverage(sprzedaz);
                double sum = 0.00;
                for (int s = 0; s < sprzedaz.size(); s++) {
                    double square = Math.pow(sprzedaz.get(s) - sredniKursSprzedazy, 2);
                    sum = sum + square;
                }
                double dev = Math.sqrt(sum / sprzedaz.size());
                System.out.println(f.format(dev));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String fileName(int year, int montH, int daY) throws Exception {
        String filename = null;
        URL urlDir = null;
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String yearShort = Integer.toString(year).substring(2, 4);
        String month = String.format("%02d", montH);
        String day = String.format("%02d", daY);

        try {
            if (year == currentYear) {
                urlDir = new URL("http://www.nbp.pl/kursy/xml/dir.txt");
            } else {
                urlDir = new URL("http://www.nbp.pl/kursy/xml/dir" + Integer.toString(year) + ".txt");
            }

            Scanner s = new Scanner(urlDir.openStream());

            while (s.hasNextLine()) {

                String str = s.nextLine();

                if (str.indexOf("c001z" + yearShort + month + day) > -1) {
                    filename = "c001z" + yearShort + month + day;
                } else if (str.substring(5, 11).equals(yearShort + month + day) && str.substring(0, 1).equals("c")) {
                    filename = str.toString();
                }
            }

            if (filename != null) {
                return (filename);
            } else {
                if (daY > 0) {
                    fileName(year, montH, daY - 1);
                } else {
                    if (montH > 0) {
                        fileName(year, montH - 1, 31);
                    } else {
                        fileName(year - 1, 12, 31);
                    }
                }
            }

        } catch (MalformedURLException e) {
            System.out.println("Malformed URL: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("I/O Error: " + e.getMessage());
        }
        return (filename);
    }

    private static String[] getkurs(String waluta, String filename) throws Exception {

        URL url = new URL("http://www.nbp.pl/kursy/xml/" + filename + ".xml");
        URLConnection mainConnection = url.openConnection();

        Document doc = parseXML(mainConnection.getInputStream());
        NodeList descNodes = doc.getElementsByTagName("pozycja");

        for (int i = 0; i < descNodes.getLength(); i++) {
            Element kodElmnt = (Element) descNodes.item(i);

            NodeList kodWaluty = kodElmnt.getElementsByTagName("kod_waluty");
            Element kodElement = (Element) kodWaluty.item(0);
            kodWaluty = kodElement.getChildNodes();

            if (kodWaluty.item(0).getNodeValue().equals(waluta)) {
                NodeList kursKupna = kodElmnt.getElementsByTagName("kurs_kupna");
                kodElement = (Element) kursKupna.item(0);
                kursKupna = kodElement.getChildNodes();

                NodeList kursSprzedazy = kodElmnt.getElementsByTagName("kurs_sprzedazy");
                kodElement = (Element) kursSprzedazy.item(0);
                kursSprzedazy = kodElement.getChildNodes();

                return new String[]{kursKupna.item(0).getNodeValue(), kursSprzedazy.item(0).getNodeValue()};
            }
        }
        return null;
    }

    private static double calculateAverage(List<Double> values) {
        Double sum = 0.00;
        if (!values.isEmpty()) {
            for (Double mark : values) {
                sum += mark;
            }
            return sum.doubleValue() / values.size();
        }
        return sum;
    }

    private static Document parseXML(InputStream stream) throws Exception {
        DocumentBuilderFactory objDocumentBuilderFactory = null;
        DocumentBuilder objDocumentBuilder = null;
        Document doc = null;
        try {
            objDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
            objDocumentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            objDocumentBuilder = objDocumentBuilderFactory.newDocumentBuilder();
            doc = objDocumentBuilder.parse(stream);
        } catch (Exception ex) {
            throw ex;
        }
        return doc;
    }
}
