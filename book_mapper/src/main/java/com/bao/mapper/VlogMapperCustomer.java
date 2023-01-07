package com.bao.mapper;

import com.bao.vo.IndexVlogVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface VlogMapperCustomer {
    List<IndexVlogVO> getIndexVlogVOList(@Param("paramMap") Map<String, Object> map);
    List<IndexVlogVO> getIndexVlogVODetail(@Param("paramMap") Map<String, Object> map);
    List<IndexVlogVO> queryMyLikedList(@Param("paramMap") Map<String, Object> map);
}
