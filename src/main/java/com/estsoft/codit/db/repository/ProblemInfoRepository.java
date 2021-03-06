package com.estsoft.codit.db.repository;

import com.estsoft.codit.db.vo.ProblemInfoVo;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProblemInfoRepository {
  @Autowired
  private SqlSession sqlSession;

  public List<ProblemInfoVo> getList(){ return sqlSession.selectList("probleminfo.selectAll"); }
  public List<ProblemInfoVo> getListByRecruitId(int recruitId){ return sqlSession.selectList("probleminfo.selectListByReqruitId", recruitId); }
  public void insert(ProblemInfoVo problemInfoVo){
    sqlSession.insert("probleminfo.insert", problemInfoVo);
  }

}
