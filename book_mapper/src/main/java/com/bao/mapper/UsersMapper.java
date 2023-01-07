package com.bao.mapper;

import com.bao.my.mapper.MyMapper;
import com.bao.pojo.Users;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersMapper extends MyMapper<Users> {
}