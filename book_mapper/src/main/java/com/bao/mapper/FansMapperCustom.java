package com.bao.mapper;

import com.bao.my.mapper.MyMapper;
import com.bao.pojo.Fans;
import com.bao.vo.FansVO;
import com.bao.vo.VlogerVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface FansMapperCustom extends MyMapper<Fans> {
    List<VlogerVO> queryMyFollows(@Param("paramMap") Map<String, Object> map);

    List<FansVO> queryMyFans(@Param("paramMap") Map<String, Object> map);
}