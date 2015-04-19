package liquid.order.repository;

import liquid.order.domain.OrderHistory;
import org.springframework.data.repository.CrudRepository;

/**
 *  
 * User: tao
 * Date: 11/12/13
 * Time: 11:11 PM
 */
public interface OrderHistoryRepository extends CrudRepository<OrderHistory, Long> {
}
