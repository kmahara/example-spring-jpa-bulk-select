package example.repository;

import org.springframework.data.repository.CrudRepository;
import example.entity.Data;

public interface DataRepository extends CrudRepository<Data, Long> {
}
