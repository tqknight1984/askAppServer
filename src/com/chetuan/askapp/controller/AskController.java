package com.chetuan.askapp.controller;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.chetuan.askapp.dispatch.DispatchMethod;
import com.chetuan.askapp.model.ModelPrt.Request;
import com.chetuan.askapp.model.ModelPrt.Request.Askquestion;
import com.chetuan.askapp.model.ModelPrt.Responce.ResponceItem;
import com.chetuan.askapp.model.Question;
import com.chetuan.askapp.redis.dao.QuestionDao;
import com.chetuan.askapp.util.ResponceUtil;
/**
 * 
 * 类描述：问题相关
 * @ClassName:AskController
 * @author  WuYaJun                
 * @date   2016年1月25日 下午3:49:52
 *
 */
public class AskController extends BaseController{
	/**
	 * 问题状态 1 未解决 2已解决
	 */
	private static final String QS_STATUS_UNANSWER="1";
	private UserController userController;
	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
	
	private UserController getUserController() {
		if(userController == null)
		{
			userController = (UserController) dispatchCenter.getController(UserController.class);
		}
		return userController;
	}
	/**
	 * 
	 * 方法描述：发布问题
	 * @author  WuYaJun                
	 * @date   2016年1月25日 下午3:50:06
	 * @param request
	 *
	 */
	@DispatchMethod(path = "ask/askQuestion")
	public void askQuestion(Request request){
		
		ResponceItem responceItem = null;
		
		Askquestion askq = request.getAskquestion();
		
		String userid = request.getUserId();
		
		if(askq!=null){
			//获取系统当前时间
			String current = df.format(new Date());
			String questcontent  = askq.getQuestioncontent();
			Question qs = new Question();
			qs.setQuestionconetnt(questcontent); 
			qs.setUserid(userid);
			qs.setAddtime(current);
			qs.setStatus(QS_STATUS_UNANSWER);
//			if(getUserController().isUserLogin(userid, request.getTempToken())){//登录状态
				
				//发布提问的问题
				boolean re =QuestionDao.askQuestion(qs);
				if(re){
					responceItem = ResponceUtil.createResponceItem(request.getId(), true, "问题发布成功","0000");
				}else{
					responceItem = ResponceUtil.createResponceItem(request.getId(), false, "问题发布失败","1111");
				}
//			}else{
//				    responceItem = ResponceUtil.createResponceItem(request.getId(), false, "请先登录，在发布问题","2222");
//			}
		}
		if (responceItem != null) {
			dispatchCenter.writeToChannel(responceItem, dispatchCenter.getChannelByDeviceId(request.getDeviceId()));
		}
	}
	
	@DispatchMethod(path="ask/answerQuestion")
	public void answerQuestion(Request request){
		
	}

}
