package com.different.dashboard.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
 
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;
 
import com.different.dashboard.dto.ResultDto;
import com.different.dashboard.form.ResultForm;

@Repository
public class ResultDao {

	@Resource(name="sqlSession")
	private SqlSession sqlSession;
	
	private static final String NAMESPACE="com.different.dashboard.boardMapper";
	

	public List<ResultDto> getResultList(String id) throws Exception{
		// TODO Auto-generated method stub
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("id", id);
		return sqlSession.selectList(NAMESPACE+".getResultList",params);
	}
	
	public List<ResultDto> getScorePerDay(String id) throws Exception{
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("id", id);
		return sqlSession.selectList(NAMESPACE+".getScorePerDay",params);
	}
			
}
