package zerobase.bud.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.bud.domain.GithubInfo;

@Repository
public interface GithubInfoRepository extends JpaRepository<GithubInfo, Long> {

    Optional<GithubInfo> findByEmail(String email);

}
