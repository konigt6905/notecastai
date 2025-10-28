package com.notecastai.notecast.infrastructure.repo;

import com.notecastai.notecast.domain.NoteCastEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteCastDao extends JpaRepository<NoteCastEntity, Long>, JpaSpecificationExecutor<NoteCastEntity> {

}