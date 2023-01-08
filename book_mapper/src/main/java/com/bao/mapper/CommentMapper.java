package com.bao.mapper;

import com.bao.my.mapper.MyMapper;
import com.bao.pojo.Comment;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentMapper extends MyMapper<Comment> {

}