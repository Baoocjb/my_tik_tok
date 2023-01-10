package com.bao.mapper;

import com.bao.pojo.Comment;
import com.bao.vo.CommentVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface CommentMapperCustom {
    List<CommentVO> queryFirstLevelComments(@Param("paramMap") Map<String, Object> map);

    CommentVO queryTheFatherComment(@Param("fatherCommentId") String fatherCommentId);

    void createTempTable();

    void insertList(List<Comment> list);

    void updateList();

    void delTempTable();

}