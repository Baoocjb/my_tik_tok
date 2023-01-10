package com.bao.mapper;

import com.bao.pojo.Comment;
import com.bao.pojo.Vlog;
import com.bao.vo.IndexVlogVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface VlogMapperCustom {
    List<IndexVlogVO> getIndexVlogVOList(@Param("paramMap") Map<String, Object> map);
    List<IndexVlogVO> getIndexVlogVODetail(@Param("paramMap") Map<String, Object> map);
    List<IndexVlogVO> queryMyLikedList(@Param("paramMap") Map<String, Object> map);
    List<IndexVlogVO> queryMyFollowList(@Param("paramMap") Map<String, Object> map);

    void createTempTable();

    void insertList(List<Vlog> list);

    void updateList();

    void delTempTable();
}
