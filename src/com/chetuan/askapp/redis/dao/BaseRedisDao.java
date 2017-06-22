/**
 *
 * @(#) BaseRdisDao.java
 * @Package com.chetuan.askapp.redis.dao
 * 
 * Copyright © Chetuan Corporation. All rights reserved.
 *
 */

package com.chetuan.askapp.redis.dao;

/**
 * 类描述：公用redisDao
 * @ClassName:BaseRdisDao
 * @author  WuYaJun                
 * @date   2016年1月21日 下午5:41:23
 *
 *
 */
public class BaseRedisDao {
	/**
	 * 问题主键
	 */
	public static final String   qusetion_primarykey_id = "ask.question.id.max";
	/**
	 * 问题信息Map
	 */
	public static final String   qusetion_detail_Map = "ask.question.id:";
	/**
	 * 用户的问题列表
	 */
	public static final String   qusetion_list_user_List = "ask.question.list.userid:";
	/**
	 * 所有问题列表
	 */
	public static final String   qusetion_list_List = "ask.question.list";
	/**
	 * 用户信息Map
	 */
	public static final String   user_detail_Map  = "ask.user.id:";
	/**
	 * 问题回答数目
	 */
	public static final String   question_answer_num_int  = "ask.question.answernum.id:";
}
