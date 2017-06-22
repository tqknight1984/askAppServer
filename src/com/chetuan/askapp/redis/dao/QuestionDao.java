package com.chetuan.askapp.redis.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.Jedis;

import com.chetuan.askapp.model.ModelPrt.Request;
import com.chetuan.askapp.model.ModelPrt.Responce.ResponceItem.QuestionListRST;
import com.chetuan.askapp.model.Question;
import com.chetuan.askapp.redis.RedisManager;
import com.chetuan.askapp.util.Bean2Map;

public class QuestionDao  extends BaseRedisDao{
	
	
	/**
	 * 
	 * 方法描述：发布问题
	 * @author  WuYaJun                
	 * @date   2016年1月25日 下午3:50:26
	 * @param qs
	 * @return
	 *
	 */
	public static boolean askQuestion(Question qs){
		boolean flag = true;
		
		Integer id = null;
		Jedis jedis =  RedisManager.getJedisObject();
		try {
	    	//生成问题主键
	    	id = jedis.incr(qusetion_primarykey_id).intValue();
	    	//问题回答数目
//	    	jedis.set(question_answer_num_int+id,"0");
	    	qs.setId(String.valueOf(id));
	    	Map<String,String> map = Bean2Map.toMap(qs);
	    	jedis.hmset(qusetion_detail_Map + id, map);
	    	jedis.zadd(qusetion_list_user_List+qs.userid,id,""+id);//用户提的问题列表
	    	jedis.zadd(qusetion_list_List,id,""+id);//问题列表集合
	    } catch(Exception e){
	    	e.printStackTrace();
	    	id = null;
		 }finally {
		    	RedisManager.recycleJedisOjbect(jedis);
		 }
		
		return flag;
	}
	/**
	 * 
	 * 方法描述：首页获取问题列表
	 * @author  WuYaJun                
	 * @date   2016年1月25日 下午3:50:37
	 * @param start
	 * @param end
	 * @return
	 *
	 */
	public static  	List<QuestionListRST> getQuestionList(int start,int end){
		
		List<QuestionListRST> rsList =new ArrayList<QuestionListRST>();
//		QuestionListRST qlist = QuestionListRST.newBuilder()
		
		Jedis jedis =  RedisManager.getJedisObject();
		
		//问题集合，按主键从大到小排序（相当于按时间降序）
		Set<String> questionSet = jedis.zrevrange(qusetion_list_List, start, end);
		
		Iterator<String> it = questionSet.iterator();
		while(it.hasNext()){

			String questionid= it.next();
			
			Map<String,String> qmap = jedis.hgetAll(qusetion_detail_Map+questionid);
			
			
			System.out.println(qmap);
			
			String  userid= qmap.get("userid");
			String questionconetnt =  qmap.get("questionconetnt");
			String time =  qmap.get("addtime");
			String status = qmap.get("status");
			
			Map<String,String> umap = jedis.hgetAll(user_detail_Map+userid);
			
			String username = umap.get("name");
			
			QuestionListRST qlist = QuestionListRST.newBuilder()
																.setQuestionid(questionid)
																.setUserId(userid)
																.setTime(time)
																.setAnswernum(0)
																.setQuestioncontent(questionconetnt)
																.setUsername(username)
																.setStatus(status)
																.build();
			rsList.add(qlist);
		}
		
		return rsList;
	}
	
	
	public static void main(String[] args) {
		Jedis jedis =  RedisManager.getJedisObject();
		
//		jedis.zrem("ask.question.list.userid:6" ,"3");
		jedis.del("ask.question.list.userid:6");
		jedis.del("ask.question.list");
		System.out.println(jedis.zrevrange("ask.question.list", 0, -1));
		System.out.println(jedis.zrevrange("ask.question.list", 0, 3));
		System.out.println(jedis.zrevrange("ask.question.list", 4, 7));
//		jedis.set("3334","-1");
		
	}
}
