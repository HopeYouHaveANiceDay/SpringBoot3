package Java.SpringBoot3.service;

import Java.SpringBoot3.domain.Customer;
import Java.SpringBoot3.domain.Invoice;
import Java.SpringBoot3.domain.Stats;
import Java.SpringBoot3.dto.UserDTO;
import org.springframework.data.domain.Page;

/*這段程式碼定義了一個 CustomerService 介面，它描述了系統中「客戶 (Customer)」與「發票 (Invoice)」相關的功能。
介面只定義方法簽名，不包含具體的實作，通常會由一個 Service 類別去實作這些方法。*/

// 宣告一個介面 CustomerService，用來規範客戶與發票的服務功能。
public interface CustomerService {

// Customer functions ////
    Customer createCustomer(Customer customer); //建立一個新的客戶。
    Customer updateCustomer(Customer customer); //更新客戶資料。

    //分頁查詢客戶清單，回傳 Page<Customer>，適合大量資料時使用。
    Page<Customer> getCustomers(int page, int size);

    //取得所有客戶，不分頁。
    Iterable<Customer> getCustomers();
    Customer getCustomer(Long id);//根據客戶 ID 查詢單一客戶。

    //根據客戶名稱進行搜尋，並支援分頁。
    Page<Customer> searchCustomers(String name, int page, int size);


// Invoice functions /////
    Invoice createInvoice(Invoice invoice); //建立一張新的發票。
    Page<Invoice> getInvoices(int page, int size); //分頁查詢所有發票。

    //將一張發票新增到指定客戶 (透過客戶 ID)。
    void addInvoiceToCustomer(Long id, Invoice invoice);

    //return a invoice
    Invoice getInvoice(Long id);

    Stats getStats();
}
