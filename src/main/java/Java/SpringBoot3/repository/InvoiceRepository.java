package Java.SpringBoot3.repository;

import Java.SpringBoot3.domain.Invoice;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface InvoiceRepository extends PagingAndSortingRepository<Invoice, Long> , ListCrudRepository<Invoice, Long> {
}
