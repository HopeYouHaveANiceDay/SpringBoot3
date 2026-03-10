package Java.SpringBoot3.repository;


import Java.SpringBoot3.domain.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;


/* 這段程式碼定義了一個 CustomerRepository 介面，它是 Spring Data JPA 的資料存取層 (Repository)，用來操作 Customer 實體在資料庫中的 CRUD 與查詢功能。*/

// since we use jpa, so we need to extend the class

/* 宣告一個介面 CustomerRepository。
    繼承了兩個 Spring Data 提供的介面：
    (1) PagingAndSortingRepository<Customer, Long>
            提供分頁 (paging) 與排序 (sorting) 的功能。
            第一個泛型 Customer 表示操作的實體類別。
            第二個泛型 Long 表示主鍵型別。
    (2) ListCrudRepository<Customer, Long>
            提供基本的 CRUD (新增、查詢、更新、刪除) 功能，並且回傳 List 型別，方便操作。*/
public interface CustomerRepository extends PagingAndSortingRepository<Customer, Long>, ListCrudRepository<Customer, Long> {


/* 這是一個 自訂查詢方法，Spring Data JPA 會根據方法名稱自動生成 SQL。
    findByNameContaining → 查詢 Customer 的 name 欄位中包含指定字串的資料。
    Page<Customer> → 回傳分頁結果。
    Pageable pageable → 傳入分頁與排序的參數，例如第幾頁、每頁幾筆、排序方式。  */
    Page<Customer> findByNameContaining(String name, Pageable pageable);
}

/*
總結
CustomerRepository 是 Spring Data JPA 的 Repository 介面，用來存取 Customer 資料。
    繼承 PagingAndSortingRepository → 支援分頁與排序。
    繼承 ListCrudRepository → 支援基本 CRUD 操作。
    定義 findByNameContaining → 可以依照名字模糊搜尋客戶，並支援分頁。
這樣的設計讓你不用自己寫 SQL，Spring Data JPA 會根據方法名稱自動生成查詢邏輯。
*/