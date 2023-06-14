package com.different.dashboard.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
 
import com.different.dashboard.dto.BoardDto;
import com.different.dashboard.dto.DetailDetailDto;
import com.different.dashboard.form.BoardForm;
import com.different.dashboard.form.DetailDetailForm;
import com.different.dashboard.service.BoardService;
import com.different.dashboard.dto.ResultDto;
import com.different.dashboard.form.ResultForm;
import com.different.dashboard.dto.DetailDto;
import com.different.dashboard.form.DetailForm;

@Controller
@RequestMapping(value = "/board")
public class BoardController {
 
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
    @Autowired
    private BoardService boardService;
 
    //占싻삼옙
    @RequestMapping( value = "/search")
    public String search(HttpServletRequest request, HttpServletResponse response) throws Exception{
        
        return "board/search";
    }
 
    @RequestMapping(value = "/getSearch")
    @ResponseBody
    public List<BoardDto> getSearch(HttpServletRequest request, HttpServletResponse response, BoardForm boardForm) throws Exception {

        List<BoardDto> boardList = boardService.getBoardList(boardForm);
 
        return boardList;
    }
    
    
    //占싻삼옙 占싻쏙옙占쏙옙占�
    @RequestMapping(value="/student")
    public String student(@RequestParam String id, Model model) throws Exception{
    	
    	List<ResultDto> resultList = boardService.getResultList(id);
    	BoardDto boardList = boardService.getStuInfo(id);
    	List<ResultDto> scoreDay = boardService.getScorePerDay(id);
    	model.addAttribute("score",scoreDay);
    	model.addAttribute("list",resultList);
    	model.addAttribute("stuInfo",boardList);
    	return "board/student";  	
    }
    
    //占싻삼옙 占싹븝옙 占싻쏙옙占쏙옙占�
    @RequestMapping(value="/detail")
    public String detail(HttpServletRequest request, HttpServletResponse response) throws Exception{
    	
    	return "board/detail";
    	
    }

    @RequestMapping(value = "/getDetail")
    @ResponseBody
    public List<DetailDto> getDetail(HttpServletRequest request, HttpServletResponse response, DetailForm detailForm) throws Exception {

        List<DetailDto> detailList = boardService.getDetails(detailForm);
 
        return detailList;
    }
    
    @RequestMapping(value = "/getDetailDetail")
    @ResponseBody
    public List<DetailDetailDto> getDetailDetail(HttpServletRequest request, HttpServletResponse response, DetailDetailForm detailDetailForm) throws Exception {

        List<DetailDetailDto> detailDetailList = boardService.getDetailDetails(detailDetailForm);
 
        return detailDetailList;
    }
    
}
