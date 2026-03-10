package Java.SpringBoot3.resource;

import Java.SpringBoot3.domain.Customer;
import Java.SpringBoot3.domain.HttpResponse;
import Java.SpringBoot3.domain.Invoice;
import Java.SpringBoot3.domain.User;
import Java.SpringBoot3.dto.UserDTO;
import Java.SpringBoot3.report.CustomerReport;
import Java.SpringBoot3.service.CustomerService;
import Java.SpringBoot3.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.stylesheets.LinkStyle;


import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static java.time.LocalDateTime.now;
import static java.util.Map.*;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.*;

@RestController//宣告這是一個 RESTful Controller，回傳的物件會自動轉換成 JSON。
@RequestMapping(path = "/customer")//設定基礎路徑為 /customer，所有方法的路徑都會以此為前綴。
@RequiredArgsConstructor//Lombok 註解，會自動建立建構子，並注入 final 欄位的依賴。

public class CustomerResource {
    //這兩個 Service 是依賴注入的，分別用來處理客戶相關邏輯與使用者相關邏輯。
    private final CustomerService customerService;
    private final UserService userService;

    @GetMapping("/list")
/*  @AuthenticationPrincipal UserDTO user
       => Spring Security 提供的註解，會自動注入目前登入的使用者資訊 (UserDTO)。

    @RequestParam Optional<Integer> page, @RequestParam Optional<Integer> size
       => 從 URL 查詢參數取得分頁資訊，例如：/customer/list?page=0&size=10。
       => 使用 Optional 包裝，避免參數缺失時出錯。        */
    public ResponseEntity<HttpResponse> getCustomers(@AuthenticationPrincipal UserDTO user, @RequestParam Optional<Integer> page, @RequestParam Optional<Integer> size) {
        return ResponseEntity.ok(
                HttpResponse.builder()
                        .timeStamp(now().toString())
/*  1. userService.getUserByEmail(user.getEmail())
    作用：透過目前登入的使用者 (@AuthenticationPrincipal UserDTO user) 取得他的 email，再呼叫 userService 去查詢完整的使用者資料。
    流程：
    1. Spring Security 會把登入的使用者資訊注入到 user 物件。
    2. user.getEmail() 取得該使用者的 email。
    3. userService.getUserByEmail(...) 根據 email 查詢資料庫或其他來源，回傳完整的使用者資料。
    目的：讓回應 JSON 裡包含目前登入的使用者資訊。

2. customerService.getCustomers(page.orElse(0), size.orElse(10))
    作用：呼叫 customerService 的方法，取得客戶清單，並支援分頁。
    流程：
    1. 從 URL 查詢參數取得 page 與 size，例如 /customer/list?page=1&size=20。
    2. 如果參數不存在，Optional.orElse(...) 會給預設值：page=0、size=10。
    3. 呼叫 customerService.getCustomers(...)，內部會用 PageRequest.of(page, size) 去查詢分頁的客戶清單。
    目的：讓回應 JSON 裡包含客戶清單，並且支援分頁顯示。
*/
                        .data(of("user", userService.getUserByEmail(user.getEmail()),
                                "page", customerService.getCustomers(page.orElse(0), size.orElse(10)),
                                "stats", customerService.getStats()))
                        .message("Customers retrieved")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
        /*
In inspect of Chrome

(1) Object
      data:
        customers: {content: Array(2), pageable: {…}, totalPages: 1, totalElements: 2, last: true, …}
        user: {id: 4, firstName: 'Katie', lastName: 'FAN', email: 'KatieFan@gmail.com', address: '1234567890 Main Street', …}

Inside of our data in Chrome inspect, we have customers and user.
the user is the user , but the customers is the page , not the actually customers, inside of the content, that we have the actually customers, change the "customer" to "page" in HttpResponse.builder() .data(of...) CustomerResource.java

(2) Object
      data:
        page: {content: Array(2), pageable: {…}, totalPages: 1, totalElements: 2, last: true, …}
        user: {id: 4, firstName: 'Katie', lastName: 'FAN', email: 'KatieFan@gmail.com', address: '1234567890 Main Street', …}

Inside the page, it contains the page information , also the content is the customers

         */
    }

    @PostMapping("/create")
/*  @AuthenticationPrincipal UserDTO user
        => Spring Security 會自動注入目前登入的使用者資訊 (UserDTO)。
    @RequestBody Customer customer
        => 從 HTTP 請求的 body 讀取 JSON，並轉換成 Customer 物件。 */
    public ResponseEntity<HttpResponse> createCustomer(@AuthenticationPrincipal UserDTO user, @RequestBody Customer customer) {
/*  ResponseEntity.created(URI.create(""))
        => 建立一個 HTTP 201 (Created) 的回應，表示資源已成功建立。
        => （這裡 URI.create("") 是佔位用，通常會填上新資源的 URI。）    */
        return ResponseEntity.created(URI.create(""))
                .body(
                        HttpResponse.builder()
                                .timeStamp(now().toString())
// "user" → 透過 userService.getUserByEmail(user.getEmail()) 取得目前登入的使用者資訊。
//"customer" → 呼叫 customerService.createCustomer(customer) 建立新的客戶，並回傳建立後的客戶物件。
                                .data(of("user", userService.getUserByEmail(user.getEmail()),
                                        "customer", customerService.createCustomer(customer)))
                                .message("Customer created")
                                .status(CREATED)
                                .statusCode(CREATED.value())
                                .build());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<HttpResponse> getCustomer(@AuthenticationPrincipal UserDTO user, @PathVariable("id") Long id) {
        return ResponseEntity.ok(
                HttpResponse.builder()
                        .timeStamp(now().toString())
/*  @Override
    public Customer getCustomer(Long id) {
        return customerRepository.findById(id).get(); }                                                   */
                        .data(of("user", userService.getUserByEmail(user.getEmail()),
                                "customer", customerService.getCustomer(id)))
                        .message("Customer retrieved")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    // the name, page and size they wanna search for
    @GetMapping("/search")
    public ResponseEntity<HttpResponse> searchCustomer(@AuthenticationPrincipal UserDTO user, Optional<String> name, @RequestParam Optional<Integer> page, @RequestParam Optional<Integer> size) throws InterruptedException {
        //TimeUnit.SECONDS.sleep(3);
        // throw new RuntimeException("Oops something bad happened");
        return ResponseEntity.ok(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", userService.getUserByEmail(user.getEmail()),
                                "page", customerService.searchCustomers(name.orElse(""), page.orElse(0), size.orElse(10))))
                        .message("Customers retrieved")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @PutMapping("/update")
/*  @RequestBody Customer customer
      => 從 HTTP 請求的 body 讀取 JSON，並轉換成 Customer 物件。這個物件包含要更新的客戶資料。 */
    public ResponseEntity<HttpResponse> updateCustomer(@AuthenticationPrincipal UserDTO user, @RequestBody Customer customer) {
        return ResponseEntity.ok(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", userService.getUserByEmail(user.getEmail()),
                                "customer", customerService.updateCustomer(customer)))
//"customer" → 呼叫 customerService.updateCustomer(customer) (1)更新客戶資料，並(2)回傳更新後的客戶物件。
                        .message("Customer updated")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @PostMapping("/invoice/created")
    public ResponseEntity<HttpResponse> createInvoice(@AuthenticationPrincipal UserDTO user, @RequestBody Invoice invoice) {
        return ResponseEntity.created(URI.create(""))
                .body(
                    HttpResponse.builder()
                            .timeStamp(now().toString())
                            .data(of("user", userService.getUserByEmail(user.getEmail()),
                                    "invoice", customerService.createInvoice(invoice)))
                            .message("Invoice created")
                            .status(CREATED)
                            .statusCode(CREATED.value())
                            .build());
    }


    // whatever we create a new invoice, we need to navigate to a page
    // And then we need to load all customers, and we select one of them, and then add them to the invoice
    //we not create just get the information
    @GetMapping("/invoice/new")
    public ResponseEntity<HttpResponse> newInvoice(@AuthenticationPrincipal UserDTO user) {
        return ResponseEntity.ok(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", userService.getUserByEmail(user.getEmail()),
                                "customers", customerService.getCustomers()))
                        .message("Customers retrieved")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @GetMapping("/invoice/list")
    public ResponseEntity<HttpResponse> getInvoices(@AuthenticationPrincipal UserDTO user, @RequestParam Optional<Integer> page, @RequestParam Optional<Integer> size) {
        return ResponseEntity.ok(
                HttpResponse.builder()
                        .timeStamp(now().toString())

//                        .data(of("user", userService.getUserByEmail(user.getEmail()),
//                                "invoices", customerService.getInvoices(page.orElse(0), size.orElse(10))))

                        .data(of("user", userService.getUserByEmail(user.getEmail()),
                                "page", customerService.getInvoices(page.orElse(0), size.orElse(10))))
                        .message("Invoice retrieved")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }


    //取得指定發票 (Invoice) 的 API
    //回傳型別是 ResponseEntity<HttpResponse>，表示 HTTP 回應包裝了一個自訂的 HttpResponse 物件。
    @GetMapping("/invoice/get/{id}")
    public ResponseEntity<HttpResponse> getInvoice(@AuthenticationPrincipal UserDTO user, @PathVariable("id") Long id) {
        //呼叫 customerService.getInvoice(id)，根據發票 ID 從資料庫取得發票物件。
        Invoice invoice = customerService.getInvoice(id);
        return ResponseEntity.ok(
                HttpResponse.builder()
                        .timeStamp(now().toString())

//                      .data(of("user", userService.getUserByEmail(user.getEmail()),
//                            "invoice", customerService.getInvoice(id)))

                        /*
                        "user" → 目前登入的使用者資訊（透過 email 查詢）。
                        "invoice" → 查詢到的發票物件。
                        "customer" → 該發票所屬的客戶。
                         */
                        .data(of("user", userService.getUserByEmail(user.getEmail()),
                                "invoice", invoice, "customer", invoice.getCustomer())) //get the customer of the invoice
                        .message("Invoice retrieved")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }


// 這個 API 的設計是用來 把一張發票 (Invoice) 加到某一個特定客戶 (Customer)，而不是多個客戶。
// {id} 是 路徑參數，代表某一個客戶的主鍵 ID。
@PostMapping("/invoice/addtocustomer/{id}")
public ResponseEntity<HttpResponse> addInvoiceToCustomer(@AuthenticationPrincipal UserDTO user, @PathVariable("id") Long id, @RequestBody Invoice invoice) {
    customerService.addInvoiceToCustomer(id, invoice);
    return ResponseEntity.ok(
            HttpResponse.builder()
                    .timeStamp(now().toString())

//                  .data(of("user", userService.getUserByEmail(user.getEmail()),
//                         "customer", customerService.getCustomer(id)))

                    .data(of("user", userService.getUserByEmail(user.getEmail()),
                            "customers", customerService.getCustomers()))
                    .message(String.format("Invoice added to customer with ID: %s", id))
                    .status(OK)
                    .statusCode(OK.value())
                    .build());
    }


    //讓使用者可以透過 API 下載 Excel 報表。
    @GetMapping("/download/report")
    public ResponseEntity<Resource> downloadReport() throws InterruptedException {
        List<Customer> customers = new ArrayList<>();

/*      1. customerService.getCustomers()
            從資料庫或服務層取得所有客戶資料，放入 customers 清單。*/
        customerService.getCustomers().iterator().forEachRemaining(customers::add);

/*      2. 建立 CustomerReport 物件，並把客戶清單傳入。
            在建構子裡會建立 Excel 活頁簿、工作表、表頭。 */
        CustomerReport report = new CustomerReport(customers);

/*      3. HttpHeaders headers = new HttpHeaders();
            建立 HTTP 回應的標頭。
            headers.add("File-Name", "customer-report.xlsx"); → 自訂檔案名稱。
            headers.add(CONTENT_DISPOSITION, "attachment;File-Name=customer-report.xlsx"); → 告訴瀏覽器這是一個附件檔案，需要下載。*/
        HttpHeaders headers = new HttpHeaders();
        headers.add("File-Name", "customer-report.xlsx");

        InputStreamResource inputStreamResource = report.export();

       // add the following code => shows the percentage of the downloading progress
        headers.add(HttpHeaders.CONTENT_LENGTH,String.valueOf(report.ContentSize));

        //the browser know need to download something
        headers.add(CONTENT_DISPOSITION, "attachment;File-Name=customer-report.xlsx");

/*      4. ResponseEntity.ok()
            建立一個 HTTP 200 OK 的回應。
            .contentType(MediaType.parseMediaType("application/vnd.ms-excel")) → 指定回應的 MIME Type 為 Excel。
            .headers(headers) → 加上檔案下載的標頭。
            .body(report.export()) → 把 report.export() 的結果（InputStreamResource）作為回應的內容。 */
        return ResponseEntity.ok().contentType(parseMediaType("application/vnd.ms-excel"))
                .headers(headers).body(report.export());
    }
}