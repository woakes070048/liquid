package liquid.user.db.repository;

import liquid.user.domain.Group;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by Tao Ma on 3/18/15.
 */
public interface GroupRepository extends CrudRepository<Group, Long> {
}
