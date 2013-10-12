package liquid.persistence.repository;

import liquid.persistence.domain.Leg;
import liquid.persistence.domain.Route;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;

/**
 * TODO: Comments.
 * User: tao
 * Date: 10/11/13
 * Time: 12:17 AM
 */
public interface LegRepository extends CrudRepository<Leg, Long> {
    Collection<Leg> findByRoute(Route route);

    List<Leg> findByRouteAndTransMode(Route route, int transMode);
}
