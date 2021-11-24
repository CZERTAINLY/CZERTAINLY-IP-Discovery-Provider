package czertainly.ip.discovery.repository;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import czertainly.ip.discovery.dao.Certificate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface CertificateRepository extends JpaRepository<Certificate, Long>{
	List<Certificate> findAllByDiscoveryId(Long discoveryId, Pageable pagable);
	List<Certificate> findByDiscoveryId(Long discoveryId);
	Optional<Certificate> findById(Long id);
}
