package Java.SpringBoot3.domain;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT;

@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
/*
@JsonInclude(NON_DEFAULT)
 => with this annotation, only show "stats": {"totalCustomers": 102}

✅ 總結 :
@JsonInclude(NON_DEFAULT) → 只輸出非預設值的欄位。
你的 totalInvoices 和 totalBilled 都是預設值 (0 / 0.0)，所以被排除了。

        "stats": {
            "totalCustomers": 102,
            "totalInvoices": 0,
            "totalBilled": 0.0
 */
public class Stats {
    private int totalCustomers;
    private int totalInvoices;
    private double totalBilled;
}
