package Java.SpringBoot3.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Collection;
import java.util.Date;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT;
import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.IDENTITY;


/*
你看到 Hibernate 在 console 自動執行 create table ...，而你沒有在 schema.sql 裡手動寫建表語句，
原因是 Spring Boot + JPA/Hibernate 會自動幫你生成資料表結構。

為什麼不用 schema.sql ?
1. JPA 實體類別 (@Entity)
        你在 Customer 和 Invoice 類別上加了 @Entity。
        Hibernate 會把這些類別映射成資料庫的表格。

2. 欄位與關聯註解
        @Id, @GeneratedValue, @Column, @OneToMany, @ManyToOne 等註解，Hibernate 會根據它們自動生成對應的欄位與外鍵。
        例如：@ManyToOne + @JoinColumn(name = "customer_id") → Hibernate 會在 invoice 表裡建立 customer_id 外鍵欄位，並加上外鍵約束。

3. Spring Boot 的自動建表策略
        常見設定：
        spring.jpa.hibernate.ddl-auto=update
        在 application.properties 或 application.yml 裡，Spring Boot 預設會使用 Hibernate 的 ddl-auto 功能。
總結
你不需要在 schema.sql 裡手動建表，因為 Hibernate 會根據 JPA 實體自動生成。
這是 Spring Boot + JPA 的便利功能，讓你專注在 Java 實體設計，不必自己維護 SQL 建表語句。
如果你想要完全控制資料庫 schema，可以：
關閉 ddl-auto，自己寫 schema.sql。
或者用 spring.jpa.hibernate.ddl-auto=none，只讓 Hibernate 做 ORM，不自動建表。
*/

@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_DEFAULT)
//Hibernate 只會建有 @Entity 的類別對應的表。
@Entity //get jpa 將該物件映射到資料庫中的資料表。映射對象：對應到資料庫中的一張表，而非僅僅是資料欄位c

// 明確我們希望收集的客戶資訊。
public class Customer {
    @Id //we need a primary key
    @GeneratedValue(strategy = IDENTITY) //讓資料庫自動生成主鍵值（通常是 auto-increment）。
/*
strategy = GenerationType.IDENTITY 表示主鍵值由資料庫本身的「自動遞增（auto-increment）」機制產生，
而不是由 JPA 或 Hibernate 在程式端生成。
這通常用於 MySQL、SQL Server 等支援自動遞增欄位的資料庫。

the IDENTITY strategy means that the primary key column in your database (often named id) is defined as an auto-increment identity column.
 */
    private Long id;
    private String name;
    private String email;
    private String type; // INDIVIDUAL or INSTITUTION
    private String status; // ACTIVE or INACTIVE
    private String address;
    private String phone;
    private String imageUrl;
    private Date createdAt;

    // Customer Invoice JPA Mapping
    // one customer can have many invoices

    // fetch = EAGER → 查詢 Customer 時，會立即載入所有 Invoice。

    // cascade = ALL => if we delete a customer, we also delete the customer's invoices
    //               => 因為 cascade = ALL，這個 Customer 底下的 3 張 Invoice 也會一起被刪除。
    //  在一对多的情况下，一定要注意死循环的问题，实体类中使用@Setter、@Getter。@data双向关联的情况下，会出现栈溢出。
    @OneToMany(mappedBy = "customer", fetch = EAGER, cascade = ALL)
    private Collection<Invoice> invoices;
}
/*
🔹 fetch 的選項
fetch 定義了關聯資料在載入時的行為：
1. FetchType.EAGER
    立即載入：當你查詢 Customer 時，會同時把 invoices 一起查出來。
    優點：使用方便，不需要額外查詢。
    缺點：可能造成效能問題，因為會一次載入大量資料。
2. FetchType.LAZY
    延遲載入：當你查詢 Customer 時，不會馬上查出 invoices，只有在真正呼叫 customer.getInvoices() 時才會去資料庫抓。
    優點：效能較好，避免不必要的查詢。
    缺點：在某些情況（例如 session 已關閉）可能會遇到 LazyInitializationException。


🔹 cascade 的選項
cascade 定義了父物件操作時，是否要連帶影響子物件：
    CascadeType.ALL
    包含所有操作（Persist、Merge、Remove、Refresh、Detach）。
    CascadeType.PERSIST
    當儲存父物件時，自動儲存子物件。
    例：entityManager.persist(customer) 會同時 persist invoices。
    CascadeType.MERGE
    當更新父物件時，自動更新子物件。
    CascadeType.REMOVE
    當刪除父物件時，自動刪除子物件。
    例：刪除 customer 時，相關的 invoices 也會刪掉。
    CascadeType.REFRESH
    當重新載入父物件時，自動重新載入子物件。
    CascadeType.DETACH
    當父物件被 detach 時，子物件也會一起 detach。

📌 總結 :
fetch 常用：EAGER（立即載入）、LAZY（延遲載入）。
cascade 常用：ALL（全部）、PERSIST（新增時）、REMOVE（刪除時）。

 */



/*
其實 在 schema.sql 建表 和 在 Java 類別上加 @Entity 是兩種不同的方式來管理資料庫結構，差異主要在於「誰負責定義與維護表格結構」：

1. 使用 schema.sql 建表
        優點：
        完全掌控表格結構（欄位型別、索引、約束）。
        適合需要嚴格控制的表格，例如 users，因為安全性要求高。
        缺點：
        需要自己維護 SQL，若實體類別改了欄位，必須手動更新 schema.sql。
        容易出現程式碼與資料庫結構不同步的情況。

2. 使用 @Entity 讓 Hibernate 自動建表
        由 Hibernate 根據 JPA 實體自動生成表格：例如你在 Customer 和 Invoice 上加了 @Entity，Hibernate 啟動時就建了 customer 和 invoice 表。
        優點：
        不需要手動寫 SQL，開發速度快。
        程式碼與資料庫結構保持同步（欄位改了，Hibernate 會更新表格）。
        缺點：
        Hibernate 自動生成的表結構比較「通用」，可能不符合最佳化需求（例如索引、唯一約束、欄位型別精細控制）。
        在生產環境中，通常不會完全依賴 Hibernate 自動建表，而是用 migration 工具（Flyway、Liquibase）來管理。
 */