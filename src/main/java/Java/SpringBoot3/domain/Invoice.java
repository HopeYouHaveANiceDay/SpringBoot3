package Java.SpringBoot3.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Date;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT;
import static jakarta.persistence.GenerationType.*;

@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_DEFAULT)
@Entity //get jpa 將該物件映射到資料庫中的資料表。映射對象：對應到資料庫中的一張表，而非僅僅是資料欄位c

public class Invoice {
    @Id //we need a primary key
    @GeneratedValue(strategy = IDENTITY) //讓資料庫自動生成主鍵值（通常是 auto-increment）。
    private Long id;
    private String invoiceNumber; //HF0QXVO7
    private String services; //1 Oil Change $200 //make it simple. It should be an entity and have a table
    private Date date;
    private String status; //PENDING, OVERDUE, PAID
    private double total; //250

    @ManyToOne // many invoices for one customer. One customer can have many invoices

    //customer_id = 17 in the invoice table
    @JoinColumn(name = "customer_id", nullable = false)//用於強制執行「NOT NULL」約束的設定, invoice 不能缺 customer_id. //Join these two table (Customer + Invoice)

    // 因為 JPA 的雙向關聯 (bidirectional relationship) 搭配 Jackson 的 JSON 序列化，會導致物件彼此互相參照，形成「無限迴圈 (infinite recursion)」。
    @JsonIgnore //create a loop for customers 用來忽略某些欄位 private Customer customer;
    private Customer customer;
}

/*
因為 JPA 的雙向關聯 (bidirectional relationship) 搭配 Jackson 的 JSON 序列化，會導致物件彼此互相參照，形成「無限迴圈 (infinite recursion)」。
為什麼會有迴圈
1. 雙向關聯
     在你的程式裡：
        Customer 有一個 Collection<Invoice> → 表示一個客戶可以有很多發票。
        Invoice 又有一個 Customer → 表示每張發票都屬於某個客戶。
     這樣就形成了「雙向關聯」。

2. Jackson 序列化
        當 Jackson 把 Customer 轉成 JSON 時，它會把 invoices 集合也序列化。
        每個 Invoice 又包含一個 Customer 欄位，Jackson 會再去序列化這個 Customer。
        這個 Customer 又有 invoices 集合 → Jackson 又會序列化 Invoice → 又回到 Customer。
        如此反覆，形成 無限遞迴。    */