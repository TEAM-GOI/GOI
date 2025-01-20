package ssafy.GeniusOfInvestment.game.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ssafy.GeniusOfInvestment._common.entity.Information;

import java.util.List;

@Repository
public interface InformationRepository extends JpaRepository<Information, Long> {
    List<Information> findByAreaIdAndYear(Long id, int year);
    //List<Information> findByAreaIdAndYear(Long id, int year, List<Long> list);
}
