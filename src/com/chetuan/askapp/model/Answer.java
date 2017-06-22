package com.chetuan.askapp.model;

//回答
public class Answer {

	/**
	 * 问题主键
	 */
	private String questionid="";
	/**
	 * 回答主键
	 */
	private String aID="";
	/**
	 * 回答内容
	 */
	private String answercontent="";
	/**
	 * 用户id
	 */
	private String userId="";
	/**
	 * 回答时间
	 */
	private String time="";
	/**
	 * 状态
	 */
	private String status="";
	
	public String getQuestionid() {
		return questionid;
	}
	public void setQuestionid(String questionid) {
		this.questionid = questionid;
	}
	public String getaID() {
		return aID;
	}
	public void setaID(String aID) {
		this.aID = aID;
	}
	public String getAnswercontent() {
		return answercontent;
	}
	public void setAnswercontent(String answercontent) {
		this.answercontent = answercontent;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

}
