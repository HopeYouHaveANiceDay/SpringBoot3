/**

In Invoice.java,

public class Invoice {
    @Id //we need a primary key
    @GeneratedValue(strategy = IDENTITY) //讓資料庫自動生成主鍵值（通常是 auto-increment）。
*   private Long id;
*   private String invoiceNumber;
*   private String services; //make it simple. It should be a entity and have a table
    private Date date;
    private String status;
    private double total;

    @ManyToOne // many invoices for one customer. One customer can have many invoices
    @JoinColumn(name = "customer_id", nullable = false)//用於強制執行「NOT NULL」約束的設定, invoice 不能缺 customer_id. //Join these two table (Customer + Invoice)

    // 因為 JPA 的雙向關聯 (bidirectional relationship) 搭配 Jackson 的 JSON 序列化，會導致物件彼此互相參照，形成「無限迴圈 (infinite recursion)」。
    @JsonIgnore //create a loop for customers 用來忽略某些欄位 private Customer customer;
    private Customer customer;
}

 */


//same as the backend "Invoice.java"
export interface Invoice {
    id: number;
    invoiceNumber: string;
    services: string;
    status: string;
    total: number;
    createdAt: Date;
}
