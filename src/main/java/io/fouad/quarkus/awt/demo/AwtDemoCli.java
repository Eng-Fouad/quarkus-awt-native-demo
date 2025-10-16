package io.fouad.quarkus.awt.demo;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import io.quarkus.logging.Log;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;

@Command
public class AwtDemoCli implements Runnable {

    enum Task {
        SHOW_SYSTEM_TRAY_ICON,
        CREATE_QR_IMAGE,
        CREATE_PDF_FILE,
        CREATE_EXCEL_FILE,
    }

    @Parameters(paramLabel = "TASK", description = "Task to run")
    Task task;


    @Override
    public void run() {
        switch (task) {
            case SHOW_SYSTEM_TRAY_ICON -> showSystemTrayIcon();
            case CREATE_QR_IMAGE -> createQrImage();
            case CREATE_PDF_FILE -> createPdfFile();
            case CREATE_EXCEL_FILE -> createExcelFile();
            default -> throw new IllegalArgumentException("Unknown task: " + task);
        }
    }

    private static void showSystemTrayIcon() {
        if (!SystemTray.isSupported() || GraphicsEnvironment.isHeadless()) {
            Log.warn("System tray is not supported on this platform");
            return;
        }

        BufferedImage image;
        try {
            image = ImageIO.read(URI.create("https://quarkus.io/assets/images/brand/quarkus_icon_512px_default.png").toURL());
        } catch (IOException e) {
            Log.error("Failed to load icon image", e);
            return;
        }

        var systemTray = SystemTray.getSystemTray();
        var trayIcon = new TrayIcon(image.getScaledInstance(systemTray.getTrayIconSize().width, -1, Image.SCALE_SMOOTH));

        // add action when the user clicks on the tray icon
        trayIcon.addActionListener(_ -> Log.info("Clicked on the tray icon"));

        var popupMenu = new PopupMenu();
        var miPrintHelloWorld = new MenuItem("Print Hello World");
        miPrintHelloWorld.addActionListener(_ -> Log.info("Hello World"));
        popupMenu.add(miPrintHelloWorld);

        popupMenu.addSeparator();

        var miPrintGoodBye = new MenuItem("Print Good Bye");
        miPrintGoodBye.addActionListener(_ -> Log.info("Good Bye"));
        popupMenu.add(miPrintGoodBye);

        trayIcon.setPopupMenu(popupMenu);

        try {
            systemTray.add(trayIcon);
        } catch (AWTException e) {
            Log.error("Unable to add the tray icon", e);
        }

        try {
            Thread.sleep(60_000); // wait for a minute
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createQrImage() {
        var hints = new HashMap<EncodeHintType, Object>();
        hints.put(EncodeHintType.MARGIN, 0);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L); // L = Low (7% of data bytes can be restored)
        hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());

        final int DEFAULT_QR_CODE_WIDTH_PIXELS = 256;
        final int DEFAULT_QR_CODE_HEIGHT_PIXELS = 256;
        final int DEFAULT_QR_CODE_ON_PIXEL_COLOR = 0xFF000000; // black
        final int DEFAULT_QR_CODE_OFF_PIXEL_COLOR = 0xFFFFFFFF; // white

        try {
            var bitMatrix = new QRCodeWriter().encode(UUID.randomUUID().toString(),
                            BarcodeFormat.QR_CODE, DEFAULT_QR_CODE_WIDTH_PIXELS, DEFAULT_QR_CODE_HEIGHT_PIXELS, hints);
            var byteArrayOutputStream = new ByteArrayOutputStream();
            try (var imageOutputStream = new MemoryCacheImageOutputStream(byteArrayOutputStream)) {
                var matrixToImageConfig = new MatrixToImageConfig(DEFAULT_QR_CODE_ON_PIXEL_COLOR,
                                                                  DEFAULT_QR_CODE_OFF_PIXEL_COLOR);
                var bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix, matrixToImageConfig);
                ImageIO.write(bufferedImage, "PNG", imageOutputStream);
                String fileName = "%s.png".formatted(System.currentTimeMillis());
                var filePath = Path.of(fileName);
                Files.write(filePath, byteArrayOutputStream.toByteArray());
                Log.infof("Saved QR code image to: %s", filePath.toAbsolutePath());
            }
        } catch (Throwable t) {
            Log.error("Failed to generate QR code image", t);
        }
    }

    private static void createPdfFile() {
        try (var pdDocument = new PDDocument()) {
            var pdPage = new PDPage();
            pdDocument.addPage(pdPage);
            var pdPageContentStream = new PDPageContentStream(pdDocument, pdPage);
            pdPageContentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
            pdPageContentStream.beginText();
            pdPageContentStream.newLineAtOffset(100, 700);
            pdPageContentStream.showText("Hello, Apache PDFBox!");
            pdPageContentStream.newLineAtOffset(0, -20); // Move to the next line
            pdPageContentStream.showText("This is a simple example of creating a PDF with Java.");
            pdPageContentStream.endText();
            pdPageContentStream.close();

            String fileName = "%s.pdf".formatted(System.currentTimeMillis());
            var filePath = Path.of(fileName);
            pdDocument.save(filePath.toFile());
            Log.infof("Saved PDF file to: %s", filePath.toAbsolutePath());

        } catch (IOException e) {
            Log.error("Failed to generate PDF file", e);
        }
    }

    private static void createExcelFile() {
        try (var workbook = new XSSFWorkbook()) {

            var sheet = workbook.createSheet("Employee Data");
            var headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("ID");
            headerRow.createCell(1).setCellValue("Name");
            headerRow.createCell(2).setCellValue("Department");
            headerRow.createCell(3).setCellValue("Salary");

            var dataRow1 = sheet.createRow(1);
            dataRow1.createCell(0).setCellValue(101);
            dataRow1.createCell(1).setCellValue("Alice Smith");
            dataRow1.createCell(2).setCellValue("HR");
            dataRow1.createCell(3).setCellValue(60000.00);

            var dataRow2 = sheet.createRow(2);
            dataRow2.createCell(0).setCellValue(102);
            dataRow2.createCell(1).setCellValue("Bob Johnson");
            dataRow2.createCell(2).setCellValue("IT");
            dataRow2.createCell(3).setCellValue(75000.00);

            var dataRow3 = sheet.createRow(3);
            dataRow3.createCell(0).setCellValue(103);
            dataRow3.createCell(1).setCellValue("Charlie Brown");
            dataRow3.createCell(2).setCellValue("Sales");
            dataRow3.createCell(3).setCellValue(55000.00);

            String fileName = "%s.xlsx".formatted(System.currentTimeMillis());
            var filePath = Path.of(fileName);
            try (var fileOut = new FileOutputStream(filePath.toFile())) {
                workbook.write(fileOut);
                Log.infof("Saved Excel file to: %s", filePath.toAbsolutePath());
            }
        } catch (IOException e) {
            Log.error("Failed to generate Excel file", e);
        }
    }
}