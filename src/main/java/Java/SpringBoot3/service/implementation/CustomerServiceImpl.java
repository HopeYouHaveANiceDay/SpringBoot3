package Java.SpringBoot3.service.implementation;

import Java.SpringBoot3.domain.Customer;
import Java.SpringBoot3.domain.Invoice;
import Java.SpringBoot3.domain.Stats;
import Java.SpringBoot3.repository.CustomerRepository;
import Java.SpringBoot3.repository.InvoiceRepository;
import Java.SpringBoot3.rowmapper.StatsRowMapper;
import Java.SpringBoot3.service.CustomerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

import static Java.SpringBoot3.query.CustomerQuery.STATS_QUERY;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.springframework.data.domain.PageRequest.*;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;
    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public Customer createCustomer(Customer customer) {
        customer.setCreatedAt(new Date());
        return customerRepository.save(customer);
    }

    @Override
    public Customer updateCustomer(Customer customer) {
        return customerRepository.save(customer);
    }



//分頁查詢客戶清單，回傳 Page<Customer>，適合大量資料時使用。
/*  @Override
      表示這個方法是覆寫 (override) 介面 CustomerService 裡定義的 getCustomers(int page, int size) 方法。
      這樣可以確保方法簽名與介面一致。 */
    @Override

/*  回傳型別是 Page<Customer>，代表一個分頁結果，裡面包含多筆 Customer 資料。
      參數：
        page → 第幾頁 (從 0 開始)。
        size → 每頁要顯示幾筆資料。  */
    public Page<Customer> getCustomers(int page, int size) {

/*  呼叫 customerRepository.findAll(...)，這是 Spring Data JPA 提供的分頁查詢方法。
        PageRequest.of(page, size) → 建立一個分頁請求物件，指定要查詢的頁碼與每頁大小。
        最後回傳一個 Page<Customer>，裡面包含查詢結果。 */
        return customerRepository.findAll(of(page, size));
    }

    @Override
    public Iterable<Customer> getCustomers() {
        return customerRepository.findAll();
    }

    @Override
    public Customer getCustomer(Long id) {
        return customerRepository.findById(id).get();
    }

    @Override
    public Page<Customer> searchCustomers(String name, int page, int size) {
        //find all the customers by that name as a page
        return customerRepository.findByNameContaining(name, of(page, size));
    }



    //用來建立新的發票 (Invoice)。
    @Override
/*  參數：invoice → 要建立的發票物件。
    回傳型別：Invoice → 儲存完成後的發票物件（通常會包含資料庫自動生成的主鍵 ID）。  */
    public Invoice createInvoice(Invoice invoice) {
/*  設定發票編號 :
        使用 Apache Commons Lang 的 RandomStringUtils 工具，生成一個 隨機的 8 位字母與數字組合。
        .toUpperCase() → 把字串轉成大寫。
        這樣可以確保每張發票都有唯一的編號，例如：A1B2C3D4。  */
        invoice.setInvoiceNumber(randomAlphanumeric(8).toUpperCase());
/* 儲存發票 :
        呼叫 Spring Data JPA 的 invoiceRepository.save() 方法，把發票存入資料庫。
        儲存後，會回傳一個完整的 Invoice 物件，通常包含資料庫生成的主鍵 ID。   */
        return invoiceRepository.save(invoice);
/*  執行後：
        系統會自動生成一個隨機的發票編號，例如 X9T4B7Q2。
        把發票存入資料庫。
        回傳的 savedInvoice 物件會包含：
            自動生成的 id (主鍵)。
            自動生成的 invoiceNumber。
            其他你設定的欄位。  */
    }

    @Override
    public Page<Invoice> getInvoices(int page, int size) {
        return invoiceRepository.findAll(of(page, size));
    }

    //用來把一張發票 (Invoice) 指派給某個客戶 (Customer)。
    @Override
/* 參數：
    id → 客戶的主鍵 ID。
    invoiceId → 發票的主鍵 ID。
    回傳型別是 void，代表這個方法只做操作，不回傳結果。*/
    public void addInvoiceToCustomer(Long id, Invoice invoice) {
        invoice.setInvoiceNumber(randomAlphanumeric(8).toUpperCase());

        // find the customer. give us the customer
        //使用 customerRepository 根據 ID 查詢客戶。
        Customer customer = customerRepository.findById(id).get();

        //使用 invoiceRepository 根據 ID 查詢發票。
        //Invoice invoice = invoiceRepository.findById(invoiceId).get();

/*  設定關聯 :
        call the invoice, set the customer
        把查到的 Customer 設定到這張 Invoice 的 customer 欄位。
        這樣就建立了「發票屬於某個客戶」的關聯。   */
        invoice.setCustomer(customer);

/*  把更新後的 Invoice 儲存回資料庫。
    這樣就完成了「把發票指派給客戶」的操作。     */
        invoiceRepository.save(invoice);
    }

    @Override
    public Invoice getInvoice(Long id) {
        return invoiceRepository.findById(id).get();
    }

    @Override
    public Stats getStats() {
        return jdbc.queryForObject(STATS_QUERY, Map.of(), new StatsRowMapper());
    }
}
