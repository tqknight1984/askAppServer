package com.chetuan.askapp.controller;

import java.util.List;

import com.chetuan.askapp.dispatch.DispatchMethod;
import com.chetuan.askapp.model.ModelPrt.Request;
import com.chetuan.askapp.model.ModelPrt.Request.Questionlist;
import com.chetuan.askapp.model.ModelPrt.Responce.ResponceItem;
import com.chetuan.askapp.model.ModelPrt.Responce.ResponceItem.QuestionListRST;
import com.chetuan.askapp.redis.dao.QuestionDao;
import com.chetuan.askapp.util.ResponceUtil;

/**
 * 
 * 类描述：首页
 * @ClassName:IndexController
 * @author  WuYaJun                
 * @date   2016年1月21日 下午4:10:14
 *
 */
public class IndexController extends BaseController{
	
	private  int size = 4;//每页的问题条数
	
	/**
	 * 
	 * 方法描述：首页获取问题列表
	 * @author  WuYaJun                
	 * @date   2016年1月25日 下午3:51:50
	 * @param request
	 *
	 */
	@DispatchMethod(path="index/questionList")
	public void questionList(Request request){
		
		
		int page =1;//第几页
		
		Questionlist qlist = (Questionlist)request.getQuestionlist();
		
		if(qlist!=null){
			
			page = qlist.getPage();
			
			int start = (page-1)*size;
			int end = page*size-1;
			
			List<QuestionListRST> rstlist = QuestionDao.getQuestionList(start, end);
			int size = rstlist.size();
			ResponceItem responceItem = ResponceUtil.createResponceItem(request.getId(), true, 
					 size == 0?"目前没有问题": "共" + size + "个问题待回答","0000", rstlist);
			dispatchCenter.writeToChannel(responceItem, dispatchCenter.getChannelByDeviceId(request.getDeviceId()));
		}
	}
	
	
}
