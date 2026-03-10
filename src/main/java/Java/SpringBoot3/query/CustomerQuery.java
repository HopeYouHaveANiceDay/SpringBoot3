package Java.SpringBoot3.query;

public class CustomerQuery {

    //mapping "total_customers", "total_invoices", "total_billed" in StatsRowMapper.java.
    public static final String STATS_QUERY = "SELECT c.total_customers, i.total_invoices, inv.total_billed FROM (SELECT COUNT(*) total_customers FROM customer) c, (SELECT COUNT(*) total_invoices FROM invoice) i, (SELECT ROUND(SUM(total)) total_billed FROM invoice) inv";
}
