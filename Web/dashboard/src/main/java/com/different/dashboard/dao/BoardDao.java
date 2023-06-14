package com.different.dashboard.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
 
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;
 
import com.different.dashboard.dto.BoardDto;
import com.different.dashboard.dto.DetailDetailDto;
import com.different.dashboard.form.BoardForm;
import com.different.dashboard.form.DetailDetailForm;
import com.different.dashboard.dto.DetailDto;
import com.different.dashboard.form.DetailForm;
 
@Repository
public class BoardDao {
	
    @Resource(name = "sqlSession")
    private SqlSession sqlSession;
 
    private static final String NAMESPACE = "com.different.dashboard.boardMapper";
 
    public List<BoardDto> getBoardList(BoardForm boardForm) throws Exception {
 
        return sqlSession.selectList(NAMESPACE + ".getBoardList");
    }
    
    public BoardDto getStuInfo(String id) throws Exception{
    	
    	Map<String,Object> params = new HashMap<String,Object>();
    	params.put("id", id);
    	return sqlSession.selectOne(NAMESPACE+".getStuInfo",params);
    }
    public List<DetailDto> getDetails(DetailForm detailForm) throws Exception{
    	 return sqlSession.selectList(NAMESPACE + ".getDetails");
    }
    
    public List<DetailDetailDto> getDetailDetails(DetailDetailForm detailDetailForm) throws Exception{
   	 return sqlSession.selectList(NAMESPACE + ".getDetailDetails");
   }
    
    
}
