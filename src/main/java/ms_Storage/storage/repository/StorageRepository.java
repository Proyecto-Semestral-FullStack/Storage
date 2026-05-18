package ms_Storage.storage.repository;

import ms_Storage.storage.model.ArchivoMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StorageRepository  extends JpaRepository<ArchivoMedia,Long> {

}
