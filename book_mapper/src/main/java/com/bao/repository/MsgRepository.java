package com.bao.repository;

import com.bao.mo.MessageMO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MsgRepository extends MongoRepository<MessageMO, String> {

    // 通过实现 Repository, 自定义条件查询
    List<MessageMO> getAllByToUserIdEqualsAndFromUserIdNotOrderByCreateTimeDesc(String toUserId, String fromUserId, Pageable pageable);

}
