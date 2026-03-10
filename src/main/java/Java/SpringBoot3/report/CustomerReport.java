package Java.SpringBoot3.report;

import Java.SpringBoot3.domain.Customer;
import Java.SpringBoot3.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.InputStreamResource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.IntStream.*;
import static org.apache.commons.lang3.time.DateFormatUtils.*;

/*
這段程式碼是一個在 Spring Boot 專案中建立的報表工具類別，名稱是 CustomerReport。
它的用途是利用 Apache POI 套件來產生 Excel 報表，並將客戶資料輸出到試算表中。
 */

@Slf4j //這是 Lombok 提供的註解，用來自動產生 log 物件，方便在程式中記錄日誌。
public class CustomerReport {

    public static final String DATE_FORMATTER = "yyyy-MM-dd hh:mm:ss";
    public int ContentSize;
    //建立一個 Excel 活頁簿（Workbook），代表整個 Excel 檔案。
    private XSSFWorkbook workbook;

    //建立一個 Excel 工作表（Sheet），用來存放資料。
    private XSSFSheet sheet;

    // 儲存一個客戶清單，這些客戶資料會被寫入報表。 a list of customers inside CustomerReport
    private List<Customer> customers;

    //定義 Excel 表格的標題列。
    private static String[] HEADERS = { "ID", "Name", "Email", "Type", "Status", "Address", "Phone", "Created At" };


    /*
    功能解釋
    1. 接收客戶清單
            建構子會接收一個 List<Customer>，並存放到 this.customers。
            這些客戶資料之後會被寫入 Excel 報表。
    2. 建立 Excel 活頁簿
            使用 new XSSFWorkbook() 建立一個新的 Excel 檔案。
            這是 Apache POI 提供的類別，用來操作 .xlsx 格式。
    3. 建立工作表
            使用 workbook.createSheet("Customers") 建立一個名為 "Customers" 的工作表。
            所有客戶資料都會放在這個工作表中。
    4. 設定表頭
            呼叫 setHeaders() 方法，在 Excel 的第一列建立表頭。
            表頭內容來自 HEADERS 陣列，例如：ID, Name, Email, Type, Status, Address, Phone, Created At。
            表頭字型會加粗並設定大小，讓報表更清晰。        */
    public CustomerReport(List<Customer> customers) {
        this.customers = customers;
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Customers");
        setHeaders();
    }

    private void setHeaders() {
/*
        1. 建立表頭列
           使用 sheet.createRow(0) 在 Excel 的第一列（索引 0）建立表頭。 */
        Row headerRow = sheet.createRow(0);   // 在第 0 列建立表頭列

/*
        2. 設定字型樣式
            建立 CellStyle 與 XSSFFont。
            設定字型為粗體、大小 14。
            把字型套用到樣式上。   */
        CellStyle style = workbook.createCellStyle();   // 建立儲存格樣式
        XSSFFont font = workbook.createFont();          // 建立字型物件
        font.setBold(true);                             // 設定字型為粗體
        font.setFontHeight(14);                         // 設定字型大小為 14
        style.setFont(font);                            // 把字型套用到樣式上

/*
        3. 填入表頭文字
            使用 IntStream.range(0, HEADERS.length) 迴圈，依序處理每個表頭欄位。
            建立儲存格並填入 HEADERS 陣列中的文字（例如 "ID", "Name", "Email" 等）。
            套用樣式，讓表頭看起來更清晰。      */
        range(0, HEADERS.length).forEach(index -> {
            Cell cell = headerRow.createCell(index);    // 在表頭列建立儲存格
            cell.setCellValue(HEADERS[index]);          // 設定儲存格文字為 HEADERS 陣列中的值（例如 "ID", "Name", "Email" 等）。
            cell.setCellStyle(style);                   // 套用樣式（粗體、字型大小）
        });
    }

    public InputStreamResource export() {
        return generateReport();
    }

    private InputStreamResource generateReport() {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            CellStyle style = workbook.createCellStyle();
            XSSFFont font = workbook.createFont();
            font.setFontHeight(10);
            style.setFont(font);

/*
         3. 建立資料列
                從第 1 列開始（因為第 0 列是表頭）。
                迴圈遍歷 customers 清單，為每個客戶建立一列。
                每個欄位依序填入：ID, Name, Email, Type, Status, Address, Phone, Created At。         */

            //从第 1 行开始写数据
            int rowIndex = 1;

            //遍历 customers 集合中的每一个 Customer 对象。
            for (Customer customer : customers) {

                //在 Excel 表格中创建一行，并且行号递增。
                Row row = sheet.createRow(rowIndex++);

  //这段代码就是一个 把客户数据导出到 Excel 表格 的过程，每个客户一行，每个属性对应一列。
                row.createCell(0).setCellValue(customer.getId());
                row.createCell(1).setCellValue(customer.getName());
                row.createCell(2).setCellValue(customer.getEmail());
                row.createCell(3).setCellValue(customer.getType());
                row.createCell(4).setCellValue(customer.getStatus());
                row.createCell(5).setCellValue(customer.getAddress());
                row.createCell(6).setCellValue(customer.getPhone());
                /*
                USE SpringDB;
                SET SQL_SAFE_UPDATES = FALSE;
                UPDATE customer SET created_at = CURRENT_TIMESTAMP WHERE id > 2;
                 */
                //在第 7 列写入客户的创建时间，并且用 DATE_FORMATTER 格式化。
                row.createCell(7).setCellValue(format(customer.getCreatedAt(), DATE_FORMATTER));
            }

/*
4. 寫入輸出流
把 Excel 活頁簿的內容寫入 ByteArrayOutputStream。
再把它轉成 InputStreamResource，方便 Spring Boot Controller 回傳給前端下載。

總結流程
建立 Excel 活頁簿 → workbook
寫入記憶體輸出流 → workbook.write(out)
轉成 byte 陣列 → out.toByteArray()
建立輸入流 → new ByteArrayInputStream(...)
包裝成資源 → new InputStreamResource(...)
Controller 回傳給前端 → 使用者下載 Excel 報表
 */
            workbook.write(out);
            this.ContentSize = out.size();
            return new InputStreamResource(new ByteArrayInputStream(out.toByteArray()));

        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("Unable to export a report file");
        }
    }
}
