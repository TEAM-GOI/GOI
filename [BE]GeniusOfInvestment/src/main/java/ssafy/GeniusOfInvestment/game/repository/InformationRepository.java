package ssafy.GeniusOfInvestment.game.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ssafy.GeniusOfInvestment._common.entity.Information;

import java.util.List;

public interface InformationRepository extends JpaRepository<Information, Long> {
    List<Information> findByAreaId(Long id);
}
