package com.estsoft.codit.web.controller;

import com.estsoft.codit.db.vo.*;
import com.estsoft.codit.web.service.RecruitService;

import com.estsoft.codit.web.util.ApplicantStatVo;
import com.estsoft.codit.web.util.ProblemStatVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;


@Controller
@RequestMapping("/recruit/{recruitId}")
public class RecruitController {

  @Autowired
  RecruitService recruitService;

  @RequestMapping("main")
  public String main(@PathVariable("recruitId") int id, Model model) {

    RecruitVo recruitVo = recruitService.getRecruitVo(id);

    //get server time
    long time = System.currentTimeMillis();
    SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String current_date = dayTime.format(new Date(time));
    model.addAttribute("recruitVo", recruitVo);

    if (recruitVo.getFromDate() == null || current_date.compareTo(recruitVo.getFromDate()) < 0) {   //ready recruit

      return "recruit/ready/recruit-ready-main";

    } else if (current_date.compareTo(recruitVo.getFromDate()) > 0) {// expired or on-going recruit

      if (current_date.compareTo(recruitVo.getToDate()) > 0) {//expired recruit
        //enable expired flag
      }

      // 3. on-going recruit
      return "recruit/started/recruit-started-main";

  } else {
      return "redirect:/";
    }

  }

  @RequestMapping("/appregform")
  public String applicantregisterform(@PathVariable("recruitId") int id, Model model) {
    model.addAttribute("recruitVo", recruitService.getRecruitVo(id));
    return "recruit/ready/recruit-ready-appregform";
  }

  /**
   * get excel file and save parsed data as applicant into DB
   * */
  @RequestMapping("/appreg")
  public String applicantregister(@PathVariable("recruitId") int id, @RequestParam("excel-file") MultipartFile file, Model model) {

    System.out.println("=================fileupload Debug================");
    String filePath = recruitService.saveMultiPartFile(file);
    List<ApplicantVo> list = null;

    if(filePath != null) {
      list = recruitService.parseExcel(filePath, id);

      if(list != null)
        recruitService.insertApplicantList(list);

      model.addAttribute("applicantList", list);
    }
    model.addAttribute("recruitVo", recruitService.getRecruitVo(id));
    return "recruit/ready/recruit-ready-appregform";
  }

  @RequestMapping(value = "setrecruitdate", method= RequestMethod.POST)
  public String setRecruitDate(@PathVariable("recruitId") int id, @RequestParam("fromDate") String fromDate, @RequestParam("toDate") String toDate){

    //get server time
    long time = System.currentTimeMillis();
    SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String currentDate = dayTime.format(new Date(time));

    //invalid DateSetting
    if(fromDate.compareTo(toDate) > 0 || fromDate.compareTo(currentDate) < 0)
      return "redirect:main";

    recruitService.setRecruitDate(id, fromDate, toDate);
    return "redirect:main";
  }

  /**
   * todo ajax 처리
   * */
  @RequestMapping("sendticket")
//  @ResponseBody
  public String sendTickets(@PathVariable("recruitId") int id, Model model){

    boolean applicantFlag = recruitService.isApplicantRegistered( id );
    boolean dateSetFlag = recruitService.isRecruitDateSet( id );
    boolean probSelectFlag = recruitService.isProblemSelected( id );

//    model.addAttribute("applicantFlag", applicantFlag);
//    model.addAttribute("dateSetFlag", dateSetFlag);
//    model.addAttribute("probSelectFlag", probSelectFlag);



//    Map<String, Object> map = new HashMap<String, Object>();
//    map.put("applicantFlag", applicantFlag);
//    map.put("dateSetFlag", dateSetFlag);
//    map.put("probSelectFlag", probSelectFlag);

    if(applicantFlag == true && dateSetFlag == true && probSelectFlag == true)
      recruitService.sendTickets(id);
//    return map;
    return "redirect:main";
  }

  @RequestMapping("probselectform")
  public String problemSelectForm( @PathVariable("recruitId") int id, Model model){

    //view prob info
    List<ProblemInfoVo> problemInfoVoList = recruitService.getProblemInfoVoList();

    //todo cart list view
    RecruitVo recruitVo = recruitService.getRecruitVo(id);
    model.addAttribute("recruitVo", recruitVo);
    model.addAttribute("problemInfoVoList", problemInfoVoList);

    return "recruit/ready/recruit-ready-probselectform";
  }


  @RequestMapping("/selectproblem")
  public String selectProblem(@PathVariable("recruitId") int id , Model model,  @RequestParam(value= "probIdList") int [] probIdList){

    //todo if carts are already stored delete carts.

    for ( int i : probIdList){
      CartVo vo = new CartVo();
      vo.setProblemInfoId( i );
      vo.setRecruitId(id);
      recruitService.insertCart( vo );
    }
    RecruitVo recruitVo = recruitService.getRecruitVo(id);
    model.addAttribute("recruitVo", recruitVo);
    return "recruit/ready/recruit-ready-main";
  }

  //// TODO: 2016-07-20 emailform으로 들어가는 method를 짰습니다

  @RequestMapping("/writeemailform")
  public String writeEmailForm(@PathVariable("recruitId") int id , Model model) {
    model.addAttribute("recruitVo", recruitService.getRecruitVo(id));
    return "recruit/ready/recruit-ready-writeemailform";
  }

  @RequestMapping("/saveemail")
  public String saveEmail(@PathVariable("recruitId") int id , @RequestParam("emailFormat") String emailFormat) {
    recruitService.saveEmail(id, emailFormat);
    return "redirect:main";
  }

  @RequestMapping("/applicantstatform")
  public String applicantStatForm(@PathVariable("recruitId") int id , Model model) {

    List<ApplicantStatVo> applicantStatVoList = recruitService.getApplicantStatVoList(id);
    List<ProblemInfoVo> problemInfoVoList = recruitService.getProblemInfoList(id);

    model.addAttribute("applicantStatList", applicantStatVoList);
    model.addAttribute("problemInfoList", problemInfoVoList);
    model.addAttribute("recruitId", id);
    return "recruit/started/recruit-started-appstatform";
  }


  @RequestMapping("/ajax-applicantresultdetail")
  @ResponseBody
  public Object applicantResultDetail(@PathVariable("recruitId") int id,
                                @RequestParam("applicantId") int applicantId,
                                @RequestParam("problemInfoId") int problemInfoId) {
    //todo 통과율
    List<ResultVo> resultList= recruitService.getResultList(applicantId, problemInfoId);
    System.out.println(resultList);
    return resultList;
  }

  @RequestMapping("/problemstatform")
  public String problemStatForm(@PathVariable("recruitId") int id , Model model) {
    List<ProblemStatVo> problemStatVoList = recruitService.getProblemStatVoList(id);
    model.addAttribute("problemStatList", problemStatVoList);
    model.addAttribute("recruitId", id);
    return "recruit/started/recruit-started-probstatform";
  }
}
